

import groovy.text.GStringTemplateEngine

String getResourceAsString(String resourceName , Map map) {
  def raw = libraryResource resourceName
  def tempString = transform(raw as String, map)
  return tempString

}
def transform(String template, Map map) {
  return new GStringTemplateEngine().createTemplate(template).make(map).toString()
}

return this
