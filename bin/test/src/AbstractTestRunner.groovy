import com.apigee.loader.BootStrapConfigLoad
import junit.framework.TestCase

abstract class AbstractTestRunner extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp()
        BootStrapConfigLoad configLoad = new BootStrapConfigLoad();
        try {
            configLoad.setupConfig("https://microservice.fei.sidgs.net/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }
}
