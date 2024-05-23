

def getTemplate() { new template() }

/**
 * Slurp a json string to a map
 * @param json string
 * @param configs map
 * @return
 */
def Map<String, String> slurpToMap(String raw, Map configs) {
  Map<String, String> map = new HashMap<>()
  def tempString = getTemplate().transform(raw, configs)
  def configObject = new ConfigSlurper().parse(tempString as String)
  def result = configObject.flatten(map)
  return result
}

/**
 * Load a tokenized resource from path ; substitutes tokens and returns a map
 * @param resource
 * @param configmap
 * @return
 */
def slurpJsonResourceToMap(String resource, Map configmap) {
  def raw = libraryResource resource
  def tempString = getTemplate().transform(raw as String, configmap)
  def map = [:]
  def slurper = new groovy.json.JsonSlurperClassic()
  def configObject = slurper.parseText(tempString as String);
//  echo "DEBUG: Config Object : ${configObject}"
  return configObject;
}

/**
 * Write a map to file
 * @param map
 * @param fileName
 * @return
 */
def writeToFile(Map map, String fileName) {
  def config = new ConfigObject()
  config.putAll(map)
  def file = new File("${fileName}")
  file.createNewFile()
  def writer = new FileWriter(file);
  config.writeTo(writer)
}

/**
 * Read a map from file
 * @param map
 * @param fileName
 * @return
 */
def mergeWithFile(Map map, String fileName) {
  def raw = readFile(fileName) as String
  def newConfig = new ConfigObject()
  newConfig.putAll(map)
  def toSave = new ConfigSlurper().parse(raw as String).merge(newConfig)
  def resultsFile = new File("${fileName}")
  resultsFile.createNewFile()
  def writer = new FileWriter(resultsFile);
  toSave.writeTo(writer)
}

return this;
