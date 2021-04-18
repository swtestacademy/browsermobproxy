import java.io.File;
import java.net.Inet4Address;
import lombok.SneakyThrows;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class BrowserMobProxyExample {
    WebDriver             driver;
    BrowserMobProxyServer proxy;
    Proxy                 seleniumProxy;

    @SneakyThrows
    @BeforeAll
    public void setup() {
        //Proxy Operations
        proxy = new BrowserMobProxyServer();
        proxy.start(8080);
        seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
        String hostIp = Inet4Address.getLocalHost().getHostAddress();
        seleniumProxy.setHttpProxy(hostIp + ":" + proxy.getPort());
        seleniumProxy.setSslProxy(hostIp + ":" + proxy.getPort());
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);

        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        System.setProperty("webdriver.chrome.whitelistedIps", "");
        DesiredCapabilities seleniumCapabilities = new DesiredCapabilities();
        seleniumCapabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
        seleniumCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-insecure-localhost");
        options.addArguments("--ignore-urlfetcher-cert-requests");
        driver = new ChromeDriver(seleniumCapabilities);
    }

    @SneakyThrows
    @Test
    public void browserMobProxyTest() {
        proxy.newHar("google.com");
        driver.get("https://www.google.com");
        Thread.sleep(2000);

        Har har = proxy.getHar();
        File harFile = new File("google.har");
        har.writeTo(harFile);
    }

    @AfterAll
    public void teardown() {
        proxy.stop();
        driver.quit();
    }
}
