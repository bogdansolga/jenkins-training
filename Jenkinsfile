pipeline {
  agent {
    dockerContainer {
      image 'maven:3.9.9-eclipse-temurin-17'
      // Reuse the Maven cache between builds for speed:
      //args '-v $JENKINS_HOME/.m2-cache:/root/.m2'
    }
  }

  stages {
    stage('Checkout') {
      steps {
        // get the code from the GitHub repository
        git url: 'https://github.com/bogdansolga/jenkins-training', branch: 'main'
      }
    }

    stage('Build') {
      steps {
        dir('java') {
          sh 'mvn clean package'
        }
      }
    }
  }

  post {
    // If Maven was able to run the tests, even if some of the test
    // failed, record the test results and archive the jar file.
    success {
      junit '**/target/surefire-reports/TEST-*.xml'
      archiveArtifacts 'target/*.jar'
    }
  }
}
