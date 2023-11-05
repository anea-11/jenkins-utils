import utils.GlobalVars

def deployToEKS(Map config = [:]){

    withCredentials([usernamePassword(credentialsId: GlobalVars.AWS_CREDENTIALS_ID,
                        passwordVariable: 'aws_pass',
                        usernameVariable: 'aws_user')]) {

        sh '''
            export AWS_ACCESS_KEY_ID=$aws_user
            export AWS_SECRET_ACCESS_KEY=$aws_pass
            export AWS_DEFAULT_REGION=$config.aws_region
            export KUBECONFIG=\$(pwd)/kubeconfig
            export HOME=\$(pwd)

            aws eks update-kubeconfig --name $config.aws_eks_cluster_name

            ansible-playbook playbooks/deploy-online-boutique.yaml \
                -e k8s_config_version=$config.k8s_config_version \
        '''
    }
}