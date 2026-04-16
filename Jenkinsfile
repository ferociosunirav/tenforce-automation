pipeline {
    agent any

    environment {
        HEADLESS = 'true'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn clean test -DHEADLESS=$HEADLESS'
                    } else {
                        bat 'mvn clean test -DHEADLESS=%HEADLESS%'
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'target/custom-reports/*.html, target/surefire-reports/**'
        }
    }
}
