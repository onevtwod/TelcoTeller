pipeline {
    agent any
    
    environment {
        REGISTRY = "telco-registry"
        IMAGE_TAG = "${env.BUILD_NUMBER ?: 'latest'}"
        MAVEN_OPTS = "-Xmx1024m -XX:MaxPermSize=256m"
    }
    
           tools {
               maven 'Maven-3.9.0'
               jdk 'JDK-21'
           }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: "git rev-parse --short HEAD",
                        returnStdout: true
                    ).trim()
                }
            }
        }
        
        stage('Code Quality Analysis') {
            parallel {
                stage('SonarQube Analysis') {
                    steps {
                        script {
                            def scannerHome = tool 'SonarQubeScanner'
                            withSonarQubeEnv('SonarQube') {
                                sh "${scannerHome}/bin/sonar-scanner"
                            }
                        }
                    }
                }
                stage('Security Scan') {
                    steps {
                        sh 'mvn org.owasp:dependency-check-maven:check'
                    }
                }
            }
        }
        
        stage('Build & Test') {
            parallel {
                stage('User Service') {
                    steps {
                        dir('user-service') {
                            sh 'mvn clean compile test package'
                            publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                            publishCoverage adapters: [
                                jacocoAdapter('target/site/jacoco/jacoco.xml')
                            ], sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                        }
                    }
                }
                stage('Alert Trigger Service') {
                    steps {
                        dir('alert-trigger-service') {
                            sh 'mvn clean compile test package'
                            publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Notification Service') {
                    steps {
                        dir('notification-service') {
                            sh 'mvn clean compile test package'
                            publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh 'docker-compose -f docker-compose.test.yml up -d --build'
                sh 'sleep 30' // Wait for services to start
                sh 'mvn test -Dtest=**/*IntegrationTest'
                sh 'docker-compose -f docker-compose.test.yml down'
            }
        }
        
        stage('Build Docker Images') {
            steps {
                script {
                    def services = ['user-service', 'alert-trigger-service', 'notification-service']
                    services.each { service ->
                        sh "docker build -t ${REGISTRY}/${service}:${IMAGE_TAG} ${service}/"
                        sh "docker build -t ${REGISTRY}/${service}:latest ${service}/"
                    }
                }
            }
        }
        
        stage('Security Scan Images') {
            steps {
                script {
                    def services = ['user-service', 'alert-trigger-service', 'notification-service']
                    services.each { service ->
                        sh "trivy image --exit-code 1 --severity HIGH,CRITICAL ${REGISTRY}/${service}:${IMAGE_TAG}"
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                sh 'docker-compose -f docker-compose.staging.yml up -d --build'
                sh 'sleep 30'
                sh 'curl -f http://localhost:8081/actuator/health || exit 1'
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                script {
                    def services = ['user-service', 'alert-trigger-service', 'notification-service']
                    services.each { service ->
                        sh "docker push ${REGISTRY}/${service}:${IMAGE_TAG}"
                        sh "docker push ${REGISTRY}/${service}:latest"
                    }
                }
                sh 'kubectl apply -f k8s/'
                sh 'kubectl rollout status deployment/user-service -n telco-system'
                sh 'kubectl rollout status deployment/alert-trigger-service -n telco-system'
                sh 'kubectl rollout status deployment/notification-service -n telco-system'
            }
        }
    }
    
    post {
        always {
            cleanWs()
            publishHTML([
                allowMissing: false,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'user-service/target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'Coverage Report'
            ])
        }
        success {
            emailext (
                subject: "Build Successful: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: "Build ${env.BUILD_NUMBER} completed successfully!",
                to: "${env.CHANGE_AUTHOR_EMAIL}"
            )
        }
        failure {
            emailext (
                subject: "Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: "Build ${env.BUILD_NUMBER} failed. Please check the logs.",
                to: "${env.CHANGE_AUTHOR_EMAIL}"
            )
        }
    }
}
