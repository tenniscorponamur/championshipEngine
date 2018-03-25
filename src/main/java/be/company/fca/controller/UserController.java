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

/*


Get access token
curl tenniscorpoclientid:ABi8u34kPoDo@localhost:9100/oauth/token -d grant_type=password -d username=fca -d password=jwtpass

Refresh token

--> intercept url --> if 401 utilisation du refresh token et relance des appels

curl tenniscorpoclientid:ABi8u34kPoDo@localhost:9100/oauth/token -d grant_type=refresh_token -d refresh_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsidGVubmlzY29ycG9yZXNvdXJjZWlkIl0sInVzZXJfbmFtZSI6Imxlb3BvbGQiLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXSwiYXRpIjoiMTM3MGRkOWItMTdkZS00NTc0LTk5YzYtZGFhMDlhMTlmNjgzIiwiZXhwIjoxNTI0NTgyMDMyLCJhdXRob3JpdGllcyI6WyJBRE1JTl9VU0VSIl0sImp0aSI6ImYyMzM2YmMzLTE1NGMtNDU1OC04NmMxLWU1MzQ1MjQ3ZjNjMiIsImNsaWVudF9pZCI6InRlbm5pc2NvcnBvY2xpZW50aWQifQ.VnVS43yqHMETgqQUSBxCESYr7_7aKymlYDzOOl0t1_o


 */