package utils 

class GlobalVars {

    public static String NEXUS_URL = 'http://52.50.97.95:8081';
    public static String NEXUS_CREDENTIALS_ID = 'nexus-credentials';
    public static String DOCKERHUB_REGISTRY_URL =  'https://index.docker.io/v1/';
    public static String DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials';
    public static String ENCODER_APP_NAME = 'encoder-x265'
    public static String ENCODING_DOCKER_REGISTRY = 'anea11/encoding'
    public static String ENCODER_JENKINS_BUILD_DIR = 'x265-jenkins-build'
    public static String JENKINS_GITHUB_CREDENTIALS_ID = 'jenkins-bump-version-github-credentials'
    public static String ENCODER_APP_GITHUB_URL = 'github.com/anea-11/x265'
    public static String MINOR_VERSION_BUMP_COMMIT_COMMAND = '__MINOR_VERSION_BUMP'
    public static String MAJOR_VERSION_BUMP_COMMIT_COMMAND = '__MAJOR_VERSION_BUMP'
    public static String FRONTEND_APP_NAME = 'frontend'
    public static String FRONTEND_APP_GITHUB_URL = 'github.com/anea-11/frontend-demo'
    public static String INFRA_CONFIG_REPO_URL = 'https://github.com/anea-11/infra-config'
    public static String GITHUB_CREDENTIALS_ID = 'github-credentials'
    public static String AWS_CREDENTIALS_ID = 'aws-credentials'
}