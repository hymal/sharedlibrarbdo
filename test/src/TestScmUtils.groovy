import com.apigee.cicd.service.DefaultConfigService;
import com.apigee.loader.BootStrapConfigLoad;
import junit.framework.TestCase;
import org.junit.Test;

import java.net.MalformedURLException;

public class TestScmUtils extends AbstractTestRunner {


    @Test
    public void testScm() {
      def config =  DefaultConfigService.getInstance().getConfig();
        def scm = config.scm
        SCMUtils scmUtils = new SCMUtils()
        def url = scmUtils.getRepoUrl("test", "test", "test-api")
        print(url)
    }
}
