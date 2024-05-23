import com.apigee.cicd.service.DefaultConfigService
import com.apigee.cicd.service.OrgInfoService
import com.apigee.cicd.service.VHostInfoService
import org.junit.Test

class TestVhost extends AbstractTestRunner {

    @Test
    public void testVhost() {

        def vhostInfo=  VHostInfoService.getInstance().getVhostInfoForEnv("dev")

        println "Printing vhost info for dev"

        println vhostInfo.envName
        println vhostInfo.cname
        println vhostInfo.name
        println vhostInfo.port
        println vhostInfo.protocol

        def vhostInfo2=  VHostInfoService.getInstance().getVhostInfoForEnv("test")

        println "Printing vhost info for test"

        println vhostInfo2.envName
        println vhostInfo2.cname
        println vhostInfo2.name
        println vhostInfo2.port
        println vhostInfo2.protocol

    }
}

