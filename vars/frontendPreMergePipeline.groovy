import utils.GlobalVars
import utils.Version

def call(Map config = [:]){

    def frontendAppImage
    def appVersion

    node {
        try {
            stage('Checkout') {
                checkout scm

                def versionString = readFile 'version.txt'
                appVersion = new Version(versionString, "${BRANCH_NAME}", "${BUILD_ID}")
            }

            stage('Build app Docker image') {
                frontendAppImage = frontendBuildUtils.buildAppDockerImage()
            }

            stage ('Push app docker image') {
                def imageTag = "${GlobalVars.FRONTEND_APP_NAME}-${appVersion}"
                dockerHubUtils.pushImage(image: frontendAppImage, tag: imageTag)
            }
        }

        catch(Exception e) {
            currentBuild.result = 'FAILURE'
            error("Pipeline failed: ${e.message}")
        }

        finally {
            cleanWs()
            try {
                sh "docker rmi -f \$(docker images ${GlobalVars.ENCODING_DOCKER_REGISTRY} -q)"
            }
            catch(Exception e) {}
        }
    }
}
