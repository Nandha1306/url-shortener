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

        stage('Test EC2 Connection') {
            steps {
                sshagent(['ec2-staging-ssh']) {
                    bat '''
                        ssh -o StrictHostKeyChecking=no ubuntu@13.221.76.212 "docker --version"
                    '''
                }
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

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvnw.cmd clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=url-shortener -Dsonar.projectName=url-shortener'
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

        stage('Package') {
            steps {
                bat 'mvnw.cmd package -DskipTests'
            }
        }

        stage('Docker Build & Push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    bat '''
                        echo %DOCKER_PASSWORD% | docker login -u %DOCKER_USERNAME% --password-stdin
                        docker build -t %DOCKER_USERNAME%/url-shortener:latest .
                        docker push %DOCKER_USERNAME%/url-shortener:latest
                    '''
                }
            }
        }

        stage('Deploy to Staging') {
            steps {
                sshagent(['ec2-staging-ssh']) {
                    bat '''
                        ssh -o StrictHostKeyChecking=no ubuntu@13.221.76.212 "cd /home/ubuntu/url-shortener && git pull --ff-only origin feature/setup-ci-cd && docker compose -f docker-compose.staging.yml pull && docker compose -f docker-compose.staging.yml up -d"
                    '''
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                sshagent(['ec2-staging-ssh']) {
                    bat '''
                        ssh -o StrictHostKeyChecking=no ubuntu@13.221.76.212 "for i in {1..12}; do curl -fsS http://localhost/api/urls && exit 0; echo Waiting for application...; sleep 5; done; exit 1"
                    '''
                }
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