import utils.GlobalVars

def call(Map config = [:]){

    def buildImage
    def buildImageName
    def encoderAppImage
    def encoderServerScript = 'server.py'
    def appVersion

    node {
        try {
            stage('Checkout') {
                checkout scm
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

            stage ('Upload app artifacts') {

                def versionString = readFile 'version.txt'
                appVersion = "v${versionString}-${BRANCH_NAME}-b${BUILD_ID}"

                def encoderAppArtifactName = "${GlobalVars.ENCODER_APP_NAME}-${appVersion}.tar.gz"

                sh "tar cvzf ${encoderAppArtifactName} ${GlobalVars.ENCODER_JENKINS_BUILD_DIR}"

                nexusUtils.upload(artifact: "${encoderAppArtifactName}")
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
