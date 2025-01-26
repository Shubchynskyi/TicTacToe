pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Set Permissions') {
              steps {
                   script {
                       sh 'chmod +x ./build-and-test.sh'
                       sh 'chmod +x ./deploy.sh'
                   }
              }
        }

        stage('Build and Test') {
            steps {
                sh './build-and-test.sh'
            }
        }
        stage('Deploy') {
            steps {
                sh './deploy.sh'
            }
        }
    }
    post {
        success {
            echo 'Build and deployment successful.'
        }
        failure {
            echo 'Build failed.'
        }
    }
}