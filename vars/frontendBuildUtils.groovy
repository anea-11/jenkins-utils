import utils.GlobalVars

def buildAppDockerImage(){
    def frontendAppImageName = "${GlobalVars.ENCODING_DOCKER_REGISTRY}:${env.BUILD_ID}"

    def frontendAppImage = docker.build( frontendAppImageName,
                                        '--no-cache ' +
                                        '-f docker/Dockerfile .')
    return frontendAppImage
}
