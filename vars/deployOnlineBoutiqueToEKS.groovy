import utils.GlobalVars

def call(Map config = [:]){

    def deployAgentImageName = "deploy-online-boutique:${env.BUILD_ID}"

    properties([
        parameters([
            string( name: 'k8s_config_version', 
                    description: """
                        Branch or tag on https://github.com/anea-11/kubernetes-manifests which contains desired configs.
                    """,
                    defaultValue: 'main',
                    trim: true
            ),
            string( name: 'aws_eks_cluster_name', 
                    description: """
                        Name of the EKS cluster to which the app will be deployed.
                    """,
                    defaultValue: 'online_boutique_cluster',
                    trim: true
            ),
            string( name: 'aws_region', 
                    description: """
                        AWS region in which the EKS cluster resides.
                    """,
                    defaultValue: 'eu-west-1',
                    trim: true
            )
    ])])

    node {
        try {
            stage('Checkout deploy scripts') {
               checkout scmGit( branches: [[name: '*/main']], 
                                userRemoteConfigs: [[   credentialsId: "${GlobalVars.GITHUB_CREDENTIALS_ID}", 
                                                        url: "${GlobalVars.INFRA_CONFIG_REPO_URL}"
                ]])
            }

            stage ('Deploy app') {
                deployAgentImage = docker.build("${deployAgentImageName}", '-f docker/Dockerfile-ansible-jenkins-agent .')

                deployAgentImage.inside {
                    onlineBoutiqueDeployUtils.deployToEKS(  aws_region: "${params.aws_region}",
                                                            aws_eks_cluster_name: "${params.aws_eks_cluster_name}",
                                                            k8s_config_version: "${params.k8s_config_version}")
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
                sh "docker rmi -f ${deployAgentImageName}"
            }
            catch(Exception e) {}
        }
    }
}