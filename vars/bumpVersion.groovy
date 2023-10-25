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
        git commit -m "CICD_VERSION_BUMP"
        git push
    """
}

def bump(Map config = [:]){

    def lastCommitMessage = sh(script: 'git log -1 --pretty=%s', returnStdout: true).trim()
    def bumpedVersion = config.appVersion

    if (lastCommitMessage.contains(GlobalVars.MINOR_VERSION_BUMP_COMMIT_COMMAND)){
        bumpedVersion.bumpMinorVersion()
    }
    else if (lastCommitMessage.contains(GlobalVars.MAJOR_VERSION_BUMP_COMMIT_COMMAND)){
        bumpedVersion.bumpMajorVersion()
    }
    else {
        bumpedVersion.bumpPatchVersion()
    }

    return bumpedVersion
}

def call(Map config = [:]){

    def lastCommitAuthor = sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim()
    def bumpedVersion = config.appVersion

    if (lastCommitAuthor != "Jenkins") {

        bumpedVersion = bump(appVersion: config.appVersion)

        writeFile file: config.versionFile, text: "${bumpedVersion}"

        setJenkinsGithubCredentialsForRepository()

        commitUpdatedVersionFile(versionFile: config.versionFile)

        echo "Jenkins automatically bumped app version to ${bumpedVersion}"
    }
    else {
        echo "Skipping automatic version bump, because it was already bumped in the last commit"
    }

    return bumpedVersion
}