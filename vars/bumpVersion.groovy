import utils.GlobalVars
import utils.Version

def getRepositoryURL() {
    def remote = sh(script: 'git remote -v', returnStdout: true).trim()
    def repositoryURL = ""
    if (remote.contains(GlobalVars.ENCODER_APP_GITHUB_URL))
        repositoryURL = GlobalVars.ENCODER_APP_GITHUB_URL

    return repositoryURL
}

def setJenkinsGithubCredentialsForRepository() {

    def repositoryURL = getRepositoryURL()

    withCredentials([usernameColonPassword(
            credentialsId: GlobalVars.JENKINS_GITHUB_CREDENTIALS_ID,
            variable: 'GITHUB_USERPASS')]) {
                sh """
                    git remote set-url origin https://${GITHUB_USERPASS}@${repositoryURL}
                """
    }
}

def commitUpdatedVersionFile(Map config = [:]) {

    def baseBranch = "main"
    def repositoryURL = getRepositoryURL()

    if (repositoryURL.contains(GlobalVars.ENCODER_APP_GITHUB_URL))
        baseBranch = "master"

    sh """
        git checkout ${baseBranch}
        git config user.email "jenkins-@tutanota.com"
        git config user.name "Jenkins"
        git add ${config.versionFile}
        git commit -m "CICD_VERSION_BUMP PATCH"
        git push
    """
}

def call(Map config = [:]){

    def lastCommitAuthor = sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim()

    if (lastCommitAuthor != "Jenkins") {

        config.appVersion.bumpVersion()
        writeFile file: config.versionFile, text: "${config.appVersion}"

        setJenkinsGithubCredentialsForRepository()

        commitUpdatedVersionFile(versionFile: config.versionFile)

        echo "Jenkins automatically bumped app version to ${config.appVersion}"
    }
    else {
        echo "Skipping automatic version bump, because it was already bumped in the last commit"
    }
}