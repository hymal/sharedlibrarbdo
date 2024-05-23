
import Maven
/**
 * This method is used for identifying which type of template to generated based on deployment type
 * @param deploymentType
 * @param entityType
 * @return
 */


def generateProject(String deploymentType,String entityType){
    echo"Generating template for ${entityType}"
    if(deploymentType == "apigee-saas"){
        generateSaasProject(entityType)
    }
    else if (deploymentType =="apigee-hybrid"){
        generateHybridProject(entityType)
    }
    else{
        throw "invalid deployment type"
    }
}

/**
 * This method genraes template for apigee-saas
 * @param entityType
 */
def generateSaasProject(String entityType){
    /*
    echo"Generating saas template project for ${entityType}"
Place holder for saas project generator
                        "mvn archetype:generate " +
                        "-DarchetypeGroupId=com.ferguson.apigee.archetype " +
                        "-DarchetypeArtifactId=api-pass-through " +
                        "-DarchetypeVersion=${params.version} " +
                        "-DgroupId=com-ferguson-api " +
                        "-DartifactId=${params.artifactId} " +
                        "-Dpackage=com.ferguson.api " +
                        "-DteamName=${params.teamName} " +
                        "-DinteractiveMode=false"
     */
}

/**
 * This method generates template for apigee-hybrid
 * @param entityType
 * @return
 */
def generateHybridProject(String entityType){
    echo"Generating hybrid template project for ${entityType}"
    def templateProject
    Maven maven=new Maven()
    if(entityType == "apiproxy") {
                templateProject = "mvn archetype:generate " +
                "-DarchetypeGroupId=com.ferguson.apigee.hybrid.archetype " +
                "-DarchetypeArtifactId=hybrid-apiproxy-pass-through " +
                "-DarchetypeVersion=1.0.0-SNAPSHOT " +
                "-DgroupId=com-ferguson-api " +
                "-DartifactId=${params.artifactId} " +
                "-Dpackage=com.ferguson.api " +
                "-DteamName=${params.teamName} " +
                "-DbuName=${params.buName} " +
                "-DinteractiveMode=false"
    }
    else if (entityType == "sharedflow"){
        templateProject = "mvn archetype:generate " +
                "-DarchetypeGroupId=com.ferguson.apigee.hybrid.archetype " +
                "-DarchetypeArtifactId=hybrid-sharedflow-pass-through " +
                "-DarchetypeVersion=1.0.0-SNAPSHOT " +
                "-DgroupId=com-ferguson-api " +
                "-DartifactId=${params.artifactId} " +
                "-Dpackage=com.ferguson.api " +
                "-DteamName=${params.teamName} " +
                "-DbuName=${params.buName} " +
                "-DinteractiveMode=false"
    }
    else{
        throw "invalid entity type"
    }
    maven.runCommand("${templateProject}")
}


return this;