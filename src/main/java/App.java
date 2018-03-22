import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * First class of Tennis Corpo Engine
 * Created by fca on 22-03-18.
 */

@RestController
@EnableAutoConfiguration
public class App {

    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }

    public static void main(String[] args) throws Exception{

        System.setProperty("http.proxyHost", "proxy-http.spw.wallonie.be");
        System.setProperty("http.proxyPort", "3129");

        System.setProperty("https.proxyHost", "proxy-http.spw.wallonie.be");
        System.setProperty("https.proxyPort", "3129");

        SpringApplication.run(App.class, args);

    }
}
