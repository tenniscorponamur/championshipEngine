package be.company.fca.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @RequestMapping("/user")
    public Principal user(Principal principal) {
        return principal;
    }

}
