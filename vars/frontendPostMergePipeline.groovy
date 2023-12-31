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
                appVersion = new Version(versionString, "main", "${BUILD_ID}")
            }

            stage('Bump version') {
                appVersion = bumpVersion(appVersion: appVersion,
                                versionFile: 'version.txt')
            }

            stage('Build app Docker image') {
                frontendAppImage = frontendBuildUtils.buildAppDockerImage()
            }

            stage('Sanity container tests') {
                echo 'Mimicking sanity container tests'
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