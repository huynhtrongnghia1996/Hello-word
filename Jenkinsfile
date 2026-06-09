pipeline {
    agent any

    tools {
        jdk 'java-17'
        maven 'maven-3'
    }

    parameters {
        choice(name: 'ENV', choices: ['UAT'], description: 'Target environment')
        choice(name: 'SUITE', choices: ['src/test/resources/testng/testng-uat-ui.xml', 'src/test/resources/testng/testng-uat-login.xml'], description: 'TestNG suite file')
        choice(name: 'BROWSER', choices: ['', 'chrome', 'edge'], description: 'Override browser. Empty runs suite browsers.')
        choice(name: 'HEADLESS', choices: ['true', 'false'], description: 'Run browser in headless mode')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Install Playwright Browsers') {
            steps {
                sh 'mvn -B -DskipTests exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps --force chrome msedge"'
            }
        }

        stage('Run Tests') {
            steps {
                sh '''
                    mvn -B clean test \
                      -Denv=${ENV} \
                      -DsuiteXmlFile=${SUITE} \
                      -Dbrowser=${BROWSER} \
                      -Dbrowser.headless=${HEADLESS}
                '''
            }
        }

        stage('Generate Allure HTML Report') {
            steps {
                sh 'mvn -B allure:report'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/site/allure-maven-plugin/**/*,target/screenshots/**/*,target/traces/**/*,logs/**/*', allowEmptyArchive: true
            allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]
        }
    }
}
