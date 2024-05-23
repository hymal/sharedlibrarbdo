#!groovy

/**
 * Jenkinsfile to be used for Production Deployment for Ferguson shared  flows
 * */



properties([[$class  : 'BuildDiscarderProperty',
             strategy: [$class: 'LogRotator', numToKeepStr: '10']]])


if ( !params.environment ) error ("EdgeEnv Name is Required")

productionDeliverySharedflowPipeline params.environment , env.BUILD_NUMBER
