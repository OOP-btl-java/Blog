pipeline {
  agent any
  stages {
    stage('') {
      steps {
        sh '''git pull origin develop
docker build -t .
docker push
'''
        sh '''ssh jenkins@144.91.94.75
docker compose up -d'''
      }
    }

  }
}