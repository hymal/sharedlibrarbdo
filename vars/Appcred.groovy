node{
      stage('testing') {

            withCredentials([
              file(credentialsId: params.orgn, variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {

                    git credentialsId: 'bb-cred-vivek', url: 'https://bitbucket.org/konthamvivek13/acess-token-shell-script.git'
                    sh "ls -la"
                    sh "chmod +x create-jwt-token.sh get-access-token.sh"
                    sh "ls -la"
                    sh '''
            
                    ./get-access-token.sh $GOOGLE_APPLICATION_CREDENTIALS "https://www.googleapis.com/auth/cloud-platform" >> access.json
                    awk -F'"' '/access_token/{print $4}' "access.json"  > token.json
                    cat token.json
            echo "To Create custom Key and secret"
            echo $key
            '''
            if (operation.equals("create_key_secret")){
                sh'''
            curl --request POST \
            "https://apigee.googleapis.com/v1/organizations/${orgn}/developers/${developer}/apps/$app/keys/create" \
            --header "Authorization: Bearer $(cat token.json)" \
            --header 'Accept: application/json' \
            --header 'Content-Type: application/json' \
            --data '{\"consumerSecret\":\"'${consumerSecret}\'",\"consumerKey\":\"'${consumerKey}'\",\"apiProducts\":[\"'${product}'\"],"status":"approved"}' | grep -i status
        
            '''
            }
            else if (operation.equals("add_product")){
            sh'''
            echo "for update/add Product to credential"
            curl --request POST \
            "https://apigee.googleapis.com/v1/organizations/${orgn}/developers/${developer}/apps/${app}/keys/${consumerKey}" \
            --header "Authorization: Bearer ya29.a0AWY7Ckna7vWfgdd5ASo22osr8pzuknsMO68RPzmfRb2lPX8e7e1jPOuIM9_-d_R5q89Ad-mFGSEi2QV5JWHv1FYg_m1oKWsOEXfzmsSdEy-G844ynanhPUPWucING-ws_3OxWI3aBmC2DwE3DoAQf24ARb5LV5lZazWTugFW77prJvzX8y-NKniKW2tg96xqSPKRgSmQIAyK7OYRnAbiczSC97XQjSLQqMcVJgaCgYKAXASARESFQG1tDrp5r9ohgp_NkwLB-2eLLi-kQ0237"  \
            --header 'Accept: application/json' \
            --header 'Content-Type: application/json' \
            --data '{\"apiProducts\":[\"'${product}'\"]}' | grep -i status
        
            '''
            }
            else if (operation.equals("create_key_secret_and_add_product")){
            sh '''
                    
                    ./get-access-token.sh $GOOGLE_APPLICATION_CREDENTIALS "https://www.googleapis.com/auth/cloud-platform" >> access.json
                    awk -F'"' '/access_token/{print $4}' "access.json"  > token.json
                    cat token.json
            echo "To Create custom Key and secret"
            '''
                sh '''
            curl --request POST \
            "https://apigee.googleapis.com/v1/organizations/${orgn}/developers/${developer}/apps/$app/keys/create" \
            --header "Authorization: Bearer $(cat token.json)" \
            --header 'Accept: application/json' \
            --header 'Content-Type: application/json' \
            --data '{\"consumerSecret\":\"'${consumerSecret}\'",\"consumerKey\":\"'${consumerKey}'\",\"apiProducts\":[\"'${product}'\"],"status":"approved"}' | grep -i status
            
            curl --request POST \
            "https://apigee.googleapis.com/v1/organizations/${orgn}/developers/${developer}/apps/${app}/keys/${consumerKey}" \
            --header "Authorization: Bearer $(cat token.json)"  \
            --header 'Accept: application/json' \
            --header 'Content-Type: application/json' \
            --data '{\"apiProducts\":[\"'${product}'\"]}'  | grep -i status
                '''
            }
            else if (operation.equals("delete_key")){
            sh '''
            echo "Delete selected credentials via key"
            curl --request DELETE \
            "https://apigee.googleapis.com/v1/organizations/${orgn}/developers/${developer}/apps/${app}/keys/${consumerKey}" \
            --header "Authorization: Bearer ya29.a0AWY7Ckna7vWfgdd5ASo22osr8pzuknsMO68RPzmfRb2lPX8e7e1jPOuIM9_-d_R5q89Ad-mFGSEi2QV5JWHv1FYg_m1oKWsOEXfzmsSdEy-G844ynanhPUPWucING-ws_3OxWI3aBmC2DwE3DoAQf24ARb5LV5lZazWTugFW77prJvzX8y-NKniKW2tg96xqSPKRgSmQIAyK7OYRnAbiczSC97XQjSLQqMcVJgaCgYKAXASARESFQG1tDrp5r9ohgp_NkwLB-2eLLi-kQ0237" \
            --header 'Accept: application/json'  | grep -i status
                    '''
                }
                        }
            }
}