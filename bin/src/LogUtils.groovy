import groovy.transform.Field

@Field Map logConfig = null;

boolean isEnabled(boolean status) {
  if (status == true) return true
  return false
}

private void init() {
  if (logConfig == null) {
    CICDEnvUtils utils = new CICDEnvUtils()
    logConfig = utils.getConfig().log
    println("Log Config: ${logConfig}")
  }
}

def debug(message) {
  init()
  if (logConfig.debug && isEnabled(logConfig.debug)) {
    println("DEBUG: ${message}")
  }
}

def info(message) {
  init()
  if (logConfig.info && isEnabled(logConfig.info)) {
    println("INFO: ${message}")
  }
}

def warn(message) {
  init()
  if (logConfig.warn && isEnabled(logConfig.warn)) {
    println("WARN:  ${message}")
  }
}

def error(message) {
  init()
  if (logConfig.error && isEnabled(logConfig.error)) {
    println("Error: ${message}")
  }
}

def fatal(message) {
  init()
  println("FATAL : ${message}")
  error("FATAL: ${message}")
}

return this

