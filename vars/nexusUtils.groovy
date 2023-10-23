def upload(Map config = [:]){

    withCredentials([usernameColonPassword(
                    credentialsId: 'nexus-credentials',
                    variable: 'NEXUS_USERPASS')]) {
        sh """
            curl -v --user ${NEXUS_USERPASS} --upload-file ${config.artifact} ${config.nexusRepositoryURL}
        """
    }
}