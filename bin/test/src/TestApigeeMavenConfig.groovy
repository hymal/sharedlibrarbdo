import com.apigee.cicd.service.ApigeeMavenPluginConfigService
import org.junit.Test

class TestApigeeMavenConfig extends AbstractTestRunner {
    @Test
    public void testApigeeMaven() {

        def apigeeMavenInfo = ApigeeMavenPluginConfigService.getInstance().getConfig("dev")

        println apigeeMavenInfo.envname
        println apigeeMavenInfo.configExportDir
        println apigeeMavenInfo.configDir
        println apigeeMavenInfo.configOptions
        println apigeeMavenInfo.deploy
    }
}
