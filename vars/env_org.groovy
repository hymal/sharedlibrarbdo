def call(){
node {
        deleteDir()
   withMaven(globalMavenSettingsConfig: 'gcp-artifact-registry-file', jdk: 'JDK', maven: 'apigee-maven') {     
            stage('Checkout') {
                git credentialsId: 'bb-scm-cred', url: 'https://bitbucket.org/sidgs/bdo-env_org-configurations.git'
                sh "ls -la"
                sh "git checkout ${orgn}"
            }

            dir('edge') {
            stage('package'){
            sh "mvn package"
            }
                        stage('env-org') {
                            withCredentials([file(credentialsId: params.orgn, variable: 'file')]) {
                                echo params.orgn
                                sh'''
                                echo "${type},${orgn},${envt}"
                
                                '''
                                    sh"mvn -X apigee-config:${type} -Dapigee.config.options=${operation} -Phybrid-apiproxy -Dorg=${orgn} -Denv=${envt}"    
                            }
                        }     
            }
      }
  }    
}