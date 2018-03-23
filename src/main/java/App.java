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
        return "Hello Tennis Corpo!";
    }

    public static void main(String[] args) throws Exception{
        SpringApplication.run(App.class, args);

    }
}
