package be.company.fca.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api")
public class TestController {

    @RequestMapping("/public")
    String home() {
        return "Tennis Corpo Engine started !";
    }

    @RequestMapping("/public/testWithoutAuth")
    String testWithoutAuth() {
        return "Test public";
    }

    @RequestMapping("/private/testWithAuth")
    @PreAuthorize("hasAuthority('ADMIN_USER') or hasAuthority('STANDARD_USER')")
    String testWithAuth() {
        return "Test authentification";
    }

}
