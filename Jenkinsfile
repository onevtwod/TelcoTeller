pipeline {
    agent any
    environment {
        REGISTRY = "local"
    }
    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Build Maven Projects') {
            steps {
                sh 'mvn -q -DskipTests package -f user-service/pom.xml'
                sh 'mvn -q -DskipTests package -f alert-trigger-service/pom.xml'
                sh 'mvn -q -DskipTests package -f notification-service/pom.xml'
            }
        }
        stage('Build Docker Images') {
            steps {
                sh 'docker build -t telco/user-service:latest user-service'
                sh 'docker build -t telco/alert-trigger-service:latest alert-trigger-service'
                sh 'docker build -t telco/notification-service:latest notification-service'
            }
        }
        stage('Compose Up') {
            steps {
                sh 'docker compose up -d --build'
            }
        }
    }
}
