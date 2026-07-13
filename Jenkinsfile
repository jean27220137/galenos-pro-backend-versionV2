pipeline {
    agent any
    tools {
        maven 'Maven-3'
    }
    stages {
        stage('Build y Test') {
            steps {
                sh 'mvn clean verify'
            }
        }
        stage('Análisis SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube-Galenos') {
                    sh """mvn sonar:sonar -Dsonar.projectKey=galenos-pro-backend \
                      -Dsonar.coverage.exclusions='**/entity/**,**/dto/**,**/config/**,**/*MapperImpl.java,**/*Application.java,**/*Exception.java,**/messaging/**'"""
                }
            }
        }
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
    post {
        success { echo 'Calidad aprobada — código listo para producción' }
        failure { echo 'Pipeline fallido — corregir antes de hacer push' }
    }
}
// test
