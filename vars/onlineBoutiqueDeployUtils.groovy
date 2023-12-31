import utils.GlobalVars

def deployToEKS(Map config = [:]){

    withCredentials([usernamePassword(credentialsId: GlobalVars.AWS_CREDENTIALS_ID,
                        passwordVariable: 'aws_pass',
                        usernameVariable: 'aws_user')]) {

        def aws_region=config.aws_region
        def aws_eks_cluster_name=config.aws_eks_cluster_name
        def k8s_config_version=config.k8s_config_version

        // sh block must not use groovy string interpolation, because it passes sensitive data
        sh '''
            export AWS_ACCESS_KEY_ID=$aws_user
            export AWS_SECRET_ACCESS_KEY=$aws_pass
            export AWS_DEFAULT_REGION=$aws_region
            export KUBECONFIG=\$(pwd)/kubeconfig
            export HOME=\$(pwd)

            aws eks update-kubeconfig --name $aws_eks_cluster_name

            ansible-playbook playbooks/deploy-online-boutique.yaml \
                -e k8s_config_version=$k8s_config_version \
        '''
    }
}