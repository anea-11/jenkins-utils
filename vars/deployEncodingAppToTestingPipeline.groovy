import utils.GlobalVars
import utils.Version

def call(Map config = [:]){

    properties([
        parameters([
            string( name: 'encoder_service_version', 
                    description: """
                        Snapshot version of encoder service to deploy.
                        Example: 1.0.0-master-b21.
                        Only "master" snapshots are accepted.
                    """,
                    trim: true
            ),
            string( name: 'frontend_service_version', 
                    description: """
                        Snapshot version of frontend service to deploy.
                        Example: 1.0.0-main-b21.
                        Only "main" snapshots are accepted.
                    """,
                    trim: true
            ),
    ])])

    node {
        try {
            stage('Checkout deploy scripts') {
               checkout scmGit( branches: [[name: '*/main']], 
                                userRemoteConfigs: [[   credentialsId: "${GlobalVars.GITHUB_CREDENTIALS_ID}", 
                                                        url: "${GlobalVars.INFRA_CONFIG_REPO_URL}"
                ]])
            }

            stage ('Deploy encoding app') {
                withCredentials([usernamePassword(credentialsId: GlobalVars.DOCKERHUB_CREDENTIALS_ID,
                    passwordVariable: 'dockerhub_pass',
                    usernameVariable: 'dockerhub_user')]) {
                        sh """
                            ansible-playbook playbooks/deploy-encoding-app.yaml \
                            -e encoder_img_version=${params.encoder_service_version} \
                            -e frontend_img_version=${params.frontend_service_version} \
                            -e dockerhub_user=${dockerhub_user} \
                            -e dockerhub_pass=${dockerhub_pass}
                        """
                }
            }

            stage ('Run integration tests') {
                echo 'Mimicking running integration tests'
            }

            stage ('Promote verisons to releases') {
                // TODO
            }
        }

        catch(Exception e) {
            currentBuild.result = 'FAILURE'
            error("Pipeline failed: ${e.message}")
        }

        finally {
            cleanWs()
        }
    }
}