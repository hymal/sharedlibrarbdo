
#!groovy
node {
    edgeProxyOnboardArchetypePipeline env.BRANCH_NAME, env.BUILD_NUMBER
}
