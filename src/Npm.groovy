import com.apigee.cicd.service.DefaultConfigService

//
//
//def getShell() {
//    new shell()
//}
//
//def getVersion() {
//    def version = getShell().pipe("node -e \"console.log(require('./package.json').version);\"")
//    return version.trim()
//}


def runCommand(String command) {
    nodejs(configId: DefaultConfigService.instance.nodejsSettingsFileId,
            nodeJSInstallationName: DefaultConfigService.instance.tools.nodejs) {
        if (!isUnix()) {
            println command
            bat returnStdout: true, script: "${command}"
        } else {
            println command
            sh returnStdout: true, script: command
        }
    }
}
return this
