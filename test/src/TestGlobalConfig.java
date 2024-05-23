import com.apigee.loader.BootStrapConfigLoad;
import junit.framework.TestCase;
import org.junit.Test;

import java.net.MalformedURLException;

public class TestGlobalConfig extends AbstractTestRunner  {

    @Test
    public void testInit(){
        BootStrapConfigLoad configLoad = new BootStrapConfigLoad();
        try {
            configLoad.setupConfig("https://microservice.fei.sidgs.net/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }


}
