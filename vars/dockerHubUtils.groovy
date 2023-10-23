import utils.GlobalVars

def pushImage(Map config = [:]){

    docker.withRegistry(GlobalVars.DOCKERHUB_REGISTRY_URL, GlobalVars.DOCKERHUB_CREDENTIALS_ID)
    {
        config.image.push config.tag
    }
}