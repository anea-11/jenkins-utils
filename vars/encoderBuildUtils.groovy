def buildApp(Map config = [:]){
    sh """
        cd ${config.buildDir}
        cmake ../source &&
        make -j4
    """
}

def buildAndRunUnitTests(Map config = [:]){
     sh '''
        cd source/unit-tests
        ./run_tests.sh 1
    '''
}