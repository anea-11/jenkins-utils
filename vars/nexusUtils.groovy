import utils.GlobalVars

def getRepository(Map config = [:]){
    def repository = ""

    // -b in artifact name indicates snapshot build
    if (config.artifact.contains(GlobalVars.ENCODER_APP_NAME) && config.artifact.contains("-b")) {
        repository = "repository/encoder-app-snapshot/"
    }
    else if (config.artifact.contains(GlobalVars.ENCODER_APP_NAME)) {
        repository = "repository/encoder-app-release/"
    }

    return repository
}

def upload(Map config = [:]){

    repository = getRepository(artifact: config.artifact)

    withCredentials([usernameColonPassword(
                    credentialsId: GlobalVars.NEXUS_CREDENTIALS_ID,
                    variable: 'NEXUS_USERPASS')]) {
        sh """
            curl -v --user ${NEXUS_USERPASS} --upload-file ${config.artifact} ${GlobalVars.NEXUS_URL}/${repository}
        """
    }
}