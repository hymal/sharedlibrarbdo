//
//
//import groovy.transform.Field
//
//@Field LogUtils log = new LogUtils();
//@Field CICDEnvUtils envUtils = new CICDEnvUtils();
//
//
//String getHttpResponse(def response) {
//  def httpStatus = response.status
//  if (response.status != 404 && (httpStatus >= 100 && httpStatus <= 399)) {
//    return response.content as String
//  } else {
//    ""
//  }
//}
//
//
////TODO
//boolean roleExists(String roleName, String org) {
//
//  return true
//}
//
//
//
//boolean isUserInRole(String username, String roleName, String org) {
//  // TODO
//  return true
//
//}
//
///**
// * Set the permissions on a role
// * @param roleName
// * @param orgName
// * @param permissions
// */
//void setResourcePermissions(String roleName, String orgName, String permissions) {
//  String url = getApigeeServerUrl() + "/o/${orgName}/userroles/${roleName}/resourcepermissions"
//  log.debug("Doing POST on ${url} using Data : ${permissions}")
//  httpRequest contentType: 'APPLICATION_JSON',
//    httpMode: 'POST',
//    customHeaders: [[name: 'Authorization', value: getAuthToken()]],
//    url: url, requestBody: permissions
//}
//
//
//protected Map<String, String> getMgmtServerCreds() {
//  return envUtils.getCredentials(envUtils.getConfig().apigee.mgmt.credential)
//}
//
//protected String getAuthToken() {
//  def creds = envUtils.getCredentials(envUtils.getConfig().apigee.mgmt.credential)
//  if (creds.username && creds.password) {
//    authToken = getBasicAuthCreds(creds.username, creds.password)
//    return authToken
//  } else {
//    log.fatal("Credential " + envUtils.getConfig().apigee.mgmt.credential + " is not setup correctly ")
//  }
//}
//
//def getBasicAuthCreds(String u, String p) {
//  return httpUtil.getBasicAuthCreds(u, p)
//}
//
//
//return this;
