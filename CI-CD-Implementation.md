# 🚀 CI/CD Implementation Guide

## Stack Used in This Guide

```
Git + GitHub → Feature Branch → Pull Request → Jenkins → Build → Test
→ SonarQube → Quality Gate → Docker → Docker Hub → AWS EC2 Staging
→ Deployment Verification
```

---

## Step 1 — Create the GitHub Repository

Create an empty repository on GitHub, then locally:

```bash
git init
git remote add origin https://github.com/<user>/<repo>.git
git remote -v   # verify

git add .
git commit -m "chore: initial project setup"
git branch -M main
git push -u origin main
```

---

## Step 2 — Create the Branching Strategy

Recommended flow:

```
main ← develop ← feature/*
```

```bash
# create develop
git checkout -b develop
git push -u origin develop

# create a feature branch
git checkout -b feature/setup-ci-cd
```

For every new feature:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/<feature-name>

# example
git checkout -b feature/add-authentication
```

---

## Step 3 — Create `.gitignore`

Make sure secrets and generated files aren't committed. Example for a Spring Boot project:

```gitignore
target/
.idea/
.vscode/
*.iml
.env
.env.*
!.env.example
```

Check with:

```bash
git status
```

---

## Step 4 — Create `.env.example`

Never commit the real `.env`.

```powershell
New-Item .env.example -ItemType File
```

Example contents:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=my_database
DB_USER=root
DB_PASS=your_database_password

REDIS_HOST=localhost
REDIS_PORT=6379
```

Commit the example file — never the real values.

---

## Step 5 — Audit Secrets Before CI/CD

```powershell
git ls-files | Select-String "\.env"
```

You want to see `.env.example` but **not** `.env`.

Search for leaked secrets:

```bash
git grep -n -i "password"
git grep -n -i "secret"
git grep -n -i "token"
git grep -n -E "AKIA|AWS_ACCESS_KEY|AWS_SECRET_ACCESS_KEY"
```

> ⚠️ If a real secret is found: **STOP.** Remove and rotate it before continuing.

---

## Step 6 — Make Sure the Application Builds Locally

Never start debugging Jenkins before the local build works.

**Maven**
```powershell
.\mvnw.cmd clean test
```
Expected:
```
Tests run: XX
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

**Gradle**
```powershell
.\gradlew clean test
```

**Node**
```bash
npm ci
npm test
npm run build
```

---

## Step 7 — Add a Dockerfile

Example for Spring Boot:

```dockerfile
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> Change `8080` to whatever port your application actually uses.

Build and test locally:

```bash
docker build -t <dockerhub-username>/<image>:latest .
docker run -p 8080:8080 <dockerhub-username>/<image>:latest
```

---

## Step 8 — Create Docker Compose

For apps requiring a database/cache:

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASS}
      MYSQL_DATABASE: ${DB_NAME}
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine

  app:
    image: <dockerhub-username>/<image>:latest
    environment:
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: ${DB_NAME}
      DB_USER: root
      DB_PASS: ${DB_PASS}
      REDIS_HOST: redis
      REDIS_PORT: 6379
    ports:
      - "80:8080"
    depends_on:
      - mysql
      - redis

volumes:
  mysql_data:
```

```bash
docker compose up -d
docker compose ps
docker compose down
```

---

## Step 9 — Install Jenkins

Install Jenkins on your dev machine or CI server, then verify:

```
http://localhost:8080
```

```bash
java -version
```

Make sure Jenkins has the required JDK.

---

## Step 10 — Install Jenkins Plugins

Typical plugins needed:

- Git
- Pipeline
- GitHub
- GitHub Branch Source
- Credentials Binding
- SSH Agent
- Docker Pipeline
- SonarQube Scanner
- JUnit

(Some may already be installed depending on your environment.)

---

## Step 11 — Configure Jenkins Credentials

**Jenkins → Manage Jenkins → Credentials → Global**

| Credential | Type | ID |
|---|---|---|
| GitHub (repo/PR access) | Username/Token | `github-credentials` |
| Docker Hub (use access token, not password) | Username + Access Token | `dockerhub-credentials` |
| EC2 SSH | SSH Username with private key (user: `ubuntu`) | `ec2-staging-ssh` |
| Database password | Secret text | `mysql-db-password` |
| SonarQube | Secret text | `sonar-token` |

---

## Step 12 — Install and Configure SonarQube

```bash
docker run -d \
  --name sonarqube \
  -p 9000:9000 \
  sonarqube:community
```

Open `http://localhost:9000`, create a project (e.g. Project Key: `my-project`), generate a token, and store it in Jenkins as `sonar-token`.

---

## Step 13 — Configure SonarQube in Jenkins

**Manage Jenkins → System → SonarQube servers**

- Name: `SonarQube`
- Server URL: `http://localhost:9000`
- Authentication: use the Jenkins SonarQube token

---

## Step 14 — Create the `Jenkinsfile`

Place a `Jenkinsfile` in the project root. Basic CI pipeline:

```groovy
pipeline {
    agent any

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

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvnw.cmd sonar:sonar'
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
    }
}
```

> On Linux, use `./mvnw clean test` instead of `mvnw.cmd`.

---

## Step 15 — Create the Full CI/CD Pipeline

Once CI works, add deployment:

```
Checkout → Dependencies → Build + Test → SonarQube → Quality Gate
→ Package → Docker Build → Docker Push → SSH EC2 → Deploy → Verify
```

Additional Jenkins stages:

```groovy
stage('Docker Build & Push') {
    steps {
        // Build and push Docker image
    }
}

stage('Deploy to Staging') {
    steps {
        // SSH to EC2
        // docker compose pull
        // docker compose up -d
    }
}

stage('Verify Deployment') {
    steps {
        // curl application endpoint
    }
}
```

---

## Step 16 — Create the EC2 Staging Server

Create an Ubuntu EC2 instance, then:

```bash
sudo apt update
# install Docker using the official method for your Ubuntu version

docker --version
docker compose version

sudo usermod -aG docker ubuntu
# reconnect or refresh the session

docker ps
```

---

## Step 17 — Configure EC2 Environment

```bash
nano .env
```

```env
DB_NAME=my_database
DB_PASS=<REAL_SECRET>
```

> Never commit this file.

```bash
ls -la

# deploy manually once before automating
docker compose -f docker-compose.staging.yml pull
docker compose -f docker-compose.staging.yml up -d
docker compose -f docker-compose.staging.yml ps
docker logs <app-container>
```

---

## Step 18 — Test EC2 Manually

Before wiring up Jenkins deployment, confirm EC2 works manually:

```bash
curl http://localhost/api/health
# or your actual endpoint
curl http://localhost/api/urls
```

If the application responds — EC2 staging works ✅ — now automate it.

---

## Step 19 — Add Docker Push to Jenkins

```
Docker Build → Docker Login → Docker Push
```

```bash
docker build -t <dockerhub-username>/<image>:latest .
echo %DOCKER_PASSWORD% | docker login -u %DOCKER_USERNAME% --password-stdin
docker push <dockerhub-username>/<image>:latest
```

Username/password **must** come from Jenkins credentials.

> ❌ Never hardcode credentials, e.g. `docker login -u nandha -p myPassword`

---

## Step 20 — Add EC2 Deployment to Jenkins

Use the Jenkins SSH Agent plugin:

```groovy
sshagent(['ec2-staging-ssh']) {
    bat '''
    ssh -o StrictHostKeyChecking=no ubuntu@<EC2_IP> "docker compose -f docker-compose.staging.yml pull && docker compose -f docker-compose.staging.yml up -d"
    '''
}
```

Do not hardcode private keys.

---

## Step 21 — Add Deployment Verification

Never consider a deployment successful just because SSH succeeded — verify the actual application:

```bash
curl -fsS http://localhost/api/urls
```

Better — retry with a wait loop to give the app time to start:

```bash
for i in {1..12}; do
    curl -fsS http://localhost/api/urls && exit 0
    echo "Waiting for application..."
    sleep 5
done
exit 1
```

---

## Step 22 — Create the Jenkins PR Pipeline

Create a **Multibranch Pipeline** job named e.g. `url-shortener-pr-gate`:

```
GitHub Repository → GitHub Branch Source → Discover PRs
```

Configure GitHub authentication. The PR pipeline should run:

```
Checkout → Build → Test → SonarQube → Quality Gate
```

It should **not** deploy to EC2.

---

## Step 23 — Configure the GitHub Webhook

**GitHub → Repository → Settings → Webhooks → Add webhook**

- Payload URL: `https://<PUBLIC-JENKINS-URL>/github-webhook/`
- For local Jenkins, tunnel it: `ngrok http 8080` → use `https://<ngrok-url>/github-webhook/`
- Content type: `application/json`
- Enable: **Push**, **Pull Request**
- Test the webhook

---

## Step 24 — Configure GitHub Branch Protection

**GitHub → Settings → Rules → Rulesets → Create ruleset ("Protect develop")**

- Target: `develop`
- Enable: Require a pull request before merging
- Enable: Require status checks to pass (add your Jenkins PR status check)
- Enable: Require branches to be up to date
- Enable: Block force pushes

> For a solo project, approval requirements can be configured as you see fit.

---

## Step 25 — Test a Successful PR

```bash
git checkout develop
git pull origin develop
git checkout -b feature/test-ci

git add .
git commit -m "test: verify CI pipeline"
git push -u origin feature/test-ci
```

Open a PR: `feature/test-ci` → `develop`

Expected flow:
```
PR → Jenkins → Build → Tests → SonarQube → Quality Gate → PASS → Merge available
```

---

## Step 26 — Test a Failed PR

This step is **extremely important.**

Intentionally introduce a temporary test/build failure and push it:

```bash
git add .
git commit -m "test: verify CI failure protection"
git push
```

Expected:
```
Jenkins → FAIL → GitHub status = FAILED → Merge blocked
```

Fix the issue and push again:

```bash
git add .
git commit -m "fix: restore CI"
git push
```

Expected:
```
Jenkins → PASS → Merge available
```

This proves branch protection is actually wired to CI.

---

## Step 27 — Security Audit

Before calling CI/CD complete:

```bash
git ls-files | Select-String "\.env"
git grep -n -i "password"
git grep -n -i "secret"
git grep -n -i "token"
git grep -n -E "AKIA|AWS_ACCESS_KEY|AWS_SECRET_ACCESS_KEY"
git status
```

Expected: `nothing to commit, working tree clean`

---

## Step 28 — Check Git History for Accidentally Committed Secrets

```bash
git log -S"<SECRET_VALUE>" --all --oneline
```

If a real credential was exposed:

1. Rotate/revoke the credential.
2. Remove it from the current source.
3. Assess whether Git history needs rewriting.

> Do not assume deleting the current file removes the historical secret.

---

## Step 29 — Final End-to-End Test

```
Developer → Feature Branch → Commit → Push → Pull Request
→ Jenkins PR CI → Build → Test → SonarQube → Quality Gate → Merge
→ develop → Full Jenkins CI/CD → Package → Docker Build → Docker Hub
→ EC2 → Deploy → Verify
```

Do not skip this final test.

---

## Step 30 — Final Checklist

### Git
- [ ] Git repository created
- [ ] `main` branch exists
- [ ] `develop` branch exists
- [ ] Feature branches used
- [ ] PR workflow implemented

### CI
- [ ] Jenkins installed
- [ ] Jenkinsfile committed
- [ ] Checkout works
- [ ] Build works
- [ ] Tests work
- [ ] Test reports visible
- [ ] SonarQube works
- [ ] Quality Gate works

### CD
- [ ] Dockerfile created
- [ ] Docker build works
- [ ] Docker Hub configured
- [ ] EC2 created
- [ ] Docker installed on EC2
- [ ] SSH configured
- [ ] Deployment works
- [ ] Deployment verification works

### PR Gate
- [ ] Jenkins Multibranch Pipeline
- [ ] GitHub webhook
- [ ] PR automatically triggers Jenkins
- [ ] Successful CI allows merge
- [ ] Failed CI blocks merge

### Security
- [ ] `.env` ignored
- [ ] `.env.example` exists
- [ ] No hardcoded passwords
- [ ] No API tokens committed
- [ ] No AWS credentials committed
- [ ] Jenkins credentials used
- [ ] Docker credentials secured
- [ ] EC2 private key secured

### Documentation
- [ ] README updated
- [ ] CI/CD documented
- [ ] Deployment documented
- [ ] Environment variables documented
- [ ] Branching strategy documented

### Final
- [ ] Successful end-to-end deployment tested
- [ ] Failure path tested
- [ ] Recovery path tested

---

## ⭐ The Golden Order to Remember

Whenever implementing CI/CD for a new project, don't configure things randomly — follow this order:

1. Git Repository
2. Branching Strategy
3. `.gitignore`
4. Environment Variables
5. Local Build
6. Local Tests
7. Dockerfile
8. Docker Compose
9. Jenkins
10. Jenkins Credentials
11. SonarQube
12. Jenkins CI
13. Docker Build
14. Docker Hub
15. EC2
16. Automated Deployment
17. Deployment Verification
18. Jenkins PR Pipeline
19. GitHub Webhook
20. Branch Protection
21. Failure Testing
22. Security Audit
23. Final End-to-End Test
24. Email Notifications
