pipeline {
    agent any

    parameters {
        choice(name: 'ENV', choices: ['UAT'], description: 'Target environment')
        choice(name: 'BROWSER', choices: ['chrome', 'edge'], description: 'Browser channel')
        choice(name: 'HEADLESS', choices: ['true', 'false'], description: 'Run browser in headless mode')
        choice(name: 'MARKER', choices: ['smoke', 'login', 'regression'], description: 'Pytest marker')
        string(name: 'PYTEST_ARGS', defaultValue: '', description: 'Extra pytest arguments')
    }

    environment {
        PIP_DISABLE_PIP_VERSION_CHECK = '1'
        UV_CACHE_DIR = '.uv-cache'
        ALLURE_VERSION = '2.34.1'
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Install') {
            steps {
                sh '''
                    python3 -m pip install --user uv
                    export PATH="$HOME/.local/bin:$PATH"
                    uv sync --extra dev
                    uv run playwright install --with-deps chrome msedge
                '''
            }
        }

        stage('Quality') {
            steps {
                sh '''
                    export PATH="$HOME/.local/bin:$PATH"
                    uv run ruff check .
                    uv run pyright
                '''
            }
        }

        stage('Run Tests') {
            steps {
                withCredentials([string(credentialsId: 'automation-account-password', variable: 'ACCOUNT_PASSWORD')]) {
                    sh '''
                        export PATH="$HOME/.local/bin:$PATH"
                        ENV=${ENV} BROWSER=${BROWSER} BROWSER_HEADLESS=${HEADLESS} \
                        uv run pytest -m ${MARKER} --alluredir=allure-results ${PYTEST_ARGS}
                    '''
                }
            }
        }

        stage('Generate Allure HTML Report') {
            steps {
                sh '''
                    export PATH="$HOME/.local/bin:$PATH"
                    mkdir -p .allure
                    if [ ! -x ".allure/allure-${ALLURE_VERSION}/bin/allure" ]; then
                      curl -fsSL -o .allure/allure.tgz "https://github.com/allure-framework/allure2/releases/download/${ALLURE_VERSION}/allure-${ALLURE_VERSION}.tgz"
                      tar -xzf .allure/allure.tgz -C .allure
                    fi
                    .allure/allure-${ALLURE_VERSION}/bin/allure generate allure-results -o allure-report --clean
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'allure-report/**/*,allure-results/**/*,test-results/**/*,logs/**/*', allowEmptyArchive: true
        }
    }
}
