import utils.GlobalVars
import utils.Version

def call(Map config = [:]){

    def buildImage
    def buildImageName
    def encoderAppImage
    def appVersion

    node {
        try {
            stage('Checkout') {
                checkout scm

                def versionString = readFile 'version.txt'
                appVersion = new Version(versionString)
            }

            stage('Bump version') {
                def lastCommitAuthor = sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim()

                if (lastCommitAuthor != "Jenkins") {
                    appVersion.bumpVersion()
                    writeFile file: 'version.txt', text: "${appVersion}"

                    withCredentials([usernameColonPassword(
                        credentialsId: GlobalVars.JENKINS_GITHUB_CREDENTIALS_ID,
                        variable: 'GITHUB_USERPASS')]) {
                            sh """
                                git remote set-url origin https://${GITHUB_USERPASS}@github.com/anea-11/x265.git
                            """
                    }

                    sh """
                        git checkout master
                        git config user.email "jenkins-@tutanota.com"
                        git config user.name "Jenkins"
                        git add version.txt
                        git commit -m "CICD_VERSION_BUMP PATCH"
                        git push
                    """

                    echo "Jenkins automatically bumped app version to ${appVersion}"
                }
                else {
                    echo "Skipping automatic version bump, because it was already bumped in the last commit"
                }
            }

            stage('Build app') {

                buildImageName = "encoder-build-image:${env.BUILD_ID}"
                buildImage = docker.build("${buildImageName}", '-f docker/Dockerfile-jenkins-agent .')
                sh "mkdir ${GlobalVars.ENCODER_JENKINS_BUILD_DIR}"

                buildImage.inside {
                    encoderBuildUtils.buildApp()
                }
            }

            stage ('Unit tests') {

                buildImage.inside {
                   encoderBuildUtils.buildAndRunUnitTests()
                }
            }

            stage('Build app Docker image') {
                encoderAppImage = encoderBuildUtils.buildAppDockerImage()
            }

            stage('Sanity container tests') {
                echo 'Mimicking sanity container tests'
            }

            stage ('Upload app artifacts') {
                def encoderAppArtifact = encoderBuildUtils.tarBuildArtifacts(version: appVersion)
                nexusUtils.upload(artifact: encoderAppArtifact)
            }

            stage ('Push app docker image') {
                def imageTag = "${GlobalVars.ENCODER_APP_NAME}-${appVersion}"
                dockerHubUtils.pushImage(image: encoderAppImage, tag: imageTag)
            }
        }

        catch(Exception e) {
            currentBuild.result = 'FAILURE'
            error("Pipeline failed: ${e.message}")
        }

        finally {
            cleanWs()
            try {
                sh "docker rmi -f ${buildImageName}"
                sh "docker rmi -f \$(docker images ${GlobalVars.ENCODING_DOCKER_REGISTRY} -q)"
            }
            catch(Exception e) {}
        }
    }
}
