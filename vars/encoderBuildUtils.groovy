import utils.GlobalVars

def buildApp(Map config = [:]){

    sh """
        cd ${GlobalVars.ENCODER_JENKINS_BUILD_DIR}
        cmake ../source &&
        make -j4
    """
}

def buildAndRunUnitTests(Map config = [:]){
     sh '''
        cd source/unit-tests
        ./run_tests.sh 1
    '''
}

def buildAppDockerImage(Map config = [:]){
    def encoderAppImageName = "${GlobalVars.ENCODING_DOCKER_REGISTRY}:${env.BUILD_ID}"
    def encoderServerScript = 'server.py'

    def encoderAppImage = docker.build( encoderAppImageName,
                                        '--no-cache ' +
                                        '-f docker/Dockerfile-jenkins-app-image ' +
                                        "--build-arg X265_APP_ARTIFACT_DIR=${GlobalVars.ENCODER_JENKINS_BUILD_DIR} " +
                                        "--build-arg SERVER_SCRIPT=${encoderServerScript} .")
    return encoderAppImage
}

def tarBuildArtifacts(Map config = [:]){
    def encoderAppArtifacts = "${GlobalVars.ENCODER_APP_NAME}-${config.version}.tar.gz"
    sh "tar cvzf ${encoderAppArtifacts} ${GlobalVars.ENCODER_JENKINS_BUILD_DIR}"
    return encoderAppArtifacts
}