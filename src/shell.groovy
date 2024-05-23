
def pipe(command){
    if (isUnix())
    sh(script: command, returnStdout: true)
    else
    bat(script: command, returnStdout: true )
}

return this


