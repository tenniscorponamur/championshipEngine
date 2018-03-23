package be.company.fca.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping("/")
    String home() {
        return "Tennis Corpo Engine started !";
    }

    @RequestMapping("/test")
    String test() {
        return "Test authentification";
    }

}
