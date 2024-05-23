pipeline {
    agent any

    stages {
        stage('access-token') {
            steps {
            withCredentials([
              file(credentialsId: params.orgn, variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                script {
                    git branch: 'master', credentialsId: 'bb-cred-vivek', url: 'https://bitbucket.org/konthamvivek13/acess-token-shell-script.git'
                  //  git credentialsId: 'bb-cred-vivek', url: 'https://bitbucket.org/konthamvivek13/acess-token-shell-script.git'
                    sh "chmod +x create-jwt-token.sh get-access-token.sh"
                    sh "ls -la"
                    sh '''
                    rm access.json
                    ./get-access-token.sh $GOOGLE_APPLICATION_CREDENTIALS "https://www.googleapis.com/auth/cloud-platform" >> access.json
                    awk -F'"' '/access_token/{print $4}' "access.json"  > token.json
                    cat token.json
                    curl -X DELETE -H "Authorization: Bearer $(cat token.json)"  https://apigee.googleapis.com/v1/organizations/{${orgn}}/environments/{${envt}}/apis/{${api_name}}/revisions/{${revision_number}}/deployments
                    '''
                        }
                        }
                }
            }
        }
    }