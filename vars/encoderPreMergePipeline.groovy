import utils.GlobalVars

def call(Map config = [:]){

    def buildImage
    def buildImageName
    def encoderAppImage
    def encoderJenkinsBuildDir = 'x265-jenkins-build'
    def encoderServerScript = 'server.py'
    def dockerRegistryName = 'anea11/encoding'
    def appVersion

    node {
        try {
            stage('Checkout') {
                checkout scm
            }

            stage('Build app') {

                buildImageName = "encoder-build-image:${env.BUILD_ID}"
                buildImage = docker.build("${buildImageName}", '-f docker/Dockerfile-jenkins-agent .')
                sh "mkdir ${encoderJenkinsBuildDir}"
/*
                buildImage.inside {
                    sh """
                        cd ${encoderJenkinsBuildDir}
                        cmake ../source &&
                        make -j4
                    """
                }
                */
            }

            stage ('Unit tests') {

                buildImage.inside {
                    sh '''
                        cd source/unit-tests
                        ./run_tests.sh 1
                    '''
                }
            }

            stage('Build app Docker image') {

                def encoderAppImageName = "${dockerRegistryName}:${env.BUILD_ID}"
                encoderAppImage = docker.build( encoderAppImageName,
                                                '--no-cache ' +
                                                '-f docker/Dockerfile-jenkins-app-image ' +
                                                "--build-arg X265_APP_ARTIFACT_DIR=${encoderJenkinsBuildDir} " +
                                                "--build-arg SERVER_SCRIPT=${encoderServerScript} .")

            }

            stage ('Upload app artifacts') {

                def versionString = readFile 'version.txt'
                appVersion = "v${versionString}-${BRANCH_NAME}-b${BUILD_ID}"

                def encoderAppArtifactName = "${GlobalVars.ENCODER_APP_NAME}-${appVersion}.tar.gz"

                sh "tar cvzf ${encoderAppArtifactName} ${encoderJenkinsBuildDir}"

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
                sh "docker rmi -f \$(docker images ${dockerRegistryName} -q)"
            }
            catch(Exception e) {}
        }
    }
}
