pipeline {
    agent any

    environment {
        DB_HOST = 'localhost'
        DB_PORT = '3307'
        REDIS_HOST = 'localhost'
        REDIS_PORT = '6379'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                bat 'mvnw.cmd clean test'
            }
        }
    }
}