pipeline {
    agent any
    environment {
        DB_HOST = 'localhost'
        DB_PORT = '3307'
        DB_NAME = 'url_shortener'
        DB_USER = 'root'

        REDIS_HOST = 'localhost'
        REDIS_PORT = '6379'

        DB_PASS = credentials('mysql-db-password')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Start Dependencies') {
            steps {
                bat 'docker compose up -d mysql redis'
            }
        }

        stage('Wait for Dependencies') {
            steps {
                bat 'docker compose ps'
            }
        }

        stage('Build & Test') {
            steps {
                bat 'mvnw.cmd clean test'
            }
        }

        stage('Package') {
            steps {
                bat 'mvnw.cmd package -DskipTests'
            }
        }
    }

    post {
        always {
            bat 'docker compose down'
        }
        success {
            echo 'CI Pipeline completed successfully!'
        }
        failure {
            echo 'CI Pipeline failed!'
        }
    }
}