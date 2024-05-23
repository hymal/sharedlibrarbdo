

import hudson.model.Cause

def String getUsernameForBuild() {
  def causes = currentBuild.rawBuild.getCauses()
  for (Cause cause in causes) {
    def user;
    if (cause instanceof hudson.model.Cause.UserIdCause) {
      hudson.model.Cause.UserIdCause userIdCause = cause;
      user = userIdCause.getUserName()
      return user
    }
  }
  return null
}

def String getUserIdForBuild() {
  def causes = currentBuild.rawBuild.getCauses()
  for (Cause cause in causes) {
    def user;
    if (cause instanceof hudson.model.Cause.UserIdCause) {
      hudson.model.Cause.UserIdCause userIdCause = cause;
      user = userIdCause.getUserId()
      return user
    }
  }
  return null
}


def boolean isBuildCauseUserAction() {
  def causes = currentBuild.rawBuild.getCauses()
  for (Cause cause in causes) {
    if (cause instanceof hudson.model.Cause.UserIdCause) return true
  }
  return false
}

def boolean isBuildCauseTriggerAction() {
  def causes = currentBuild.rawBuild.getCauses()
  for (Cause cause in causes) {
    if (cause instanceof hudson.triggers.SCMTrigger.SCMTriggerCause) return true
  }
  return false
}

def boolean isBuildTriggeredByUser() {
  return isBuildCauseUserAction()
}

return this;
