def call(Map config = [:]){

    def buildImage
    def buildImageName
    def encoderAppImage
    def encoderAppImageName
    def encoderJenkinsBuildDir = 'x265-jenkins-build'
    def encoderServerScript = 'server.py'
    def encoderAppName = 'encoder-x265'
    def dockerRegistryName = 'anea11/encoding'
    def dockerRegistryUrl = 'https://index.docker.io/v1/'
    def dockerRegistryCredentials = 'dockerhub_credentials'
    def appVersion
    def nexusEncoderAppSnapshotRepoURL= 'http://18.156.155.64:8081/repository/encoder-app-snapshot/'

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

                encoderAppImageName = "${dockerRegistryName}:${env.BUILD_ID}"
                encoderAppImage = docker.build("${encoderAppImageName}",
                                                '--no-cache ' +
                                                '-f docker/Dockerfile-jenkins-app-image ' +
                                                "--build-arg X265_APP_ARTIFACT_DIR=${encoderJenkinsBuildDir} " +
                                                "--build-arg SERVER_SCRIPT=${encoderServerScript} .")

            }

            stage ('Upload app artifacts') {

                def versionString = readFile 'version.txt'
                appVersion = "v${versionString}-${BRANCH_NAME}-b${BUILD_ID}"

                def encoderAppArtifactName = "${encoderAppName}-${appVersion}.tar.gz"

                sh "tar cvzf ${encoderAppArtifactName} ${encoderJenkinsBuildDir}"

                nexusUtils.upload(  artifact: "${encoderAppArtifactName}", 
                                nexusRepositoryURL:"${nexusEncoderAppSnapshotRepoURL}")
            }

            stage ('Push app docker image') {
                docker.withRegistry("${dockerRegistryUrl}", "${dockerRegistryCredentials}")
                {
                    def imageTag = "encoder-${appVersion}"
                    encoderAppImage.push "${imageTag}"
                }
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
