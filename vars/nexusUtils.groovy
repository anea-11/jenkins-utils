import utils.GlobalVars

def upload(Map config = [:]){

    withCredentials([usernameColonPassword(
                    credentialsId: GlobalVars.NEXUS_CREDENTIALS_ID,
                    variable: 'NEXUS_USERPASS')]) {
        sh """
            curl -v --user ${NEXUS_USERPASS} --upload-file ${config.artifact} ${GlobalVars.NEXUS_URL}
        """
    }
}