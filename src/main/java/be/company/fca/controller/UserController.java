package be.company.fca.controller;

import io.swagger.annotations.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api")
@Api(description = "API REST pour la gestion des utilisateurs")
public class UserController {

    @ApiOperation(value = "Find user private",
            notes = "Ceci est une méthode privée pour recupérer l'utilisateur reconnu par le token d'accès")
    @RequestMapping(method=RequestMethod.GET, path="/private/user")
    public Principal user(Principal principal) {
        return principal;
    }

    @ApiOperation(value = "Find user public",
            notes = "Ceci est une méthode publique pour recupérer un utilisateur fictif...")
    @RequestMapping(method=RequestMethod.GET, path="/public/user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of user detail", response = User.class),
            @ApiResponse(code = 404, message = "User does not exist"),
            @ApiResponse(code = 500, message = "Internal server error")}
    )
    public Map userPublic(Principal principal, @ApiParam(value = "Nom d'utilisateur") @RequestParam(value="username",required = false) String username) {
        Map map = new HashMap();
        map.put("principal","Essai");
        return map;
    }

}

// TODO :
// DEFINIR DES VERSIONS POUR L'API
// DEFINIR UNE PARTIE PUBLIQUE ET UNE PARTIE PRIVEE
// GESTION DES ROLES DANS LA PARTIE PRIVEE



/*


Get access token
curl tenniscorpoclientid:ABi8u34kPoDo@localhost:9100/oauth/token -d grant_type=password -d username=fca -d password=jwtpass

Refresh token

--> intercept url --> if 401 utilisation du refresh token et relance des appels

curl tenniscorpoclientid:ABi8u34kPoDo@localhost:9100/oauth/token -d grant_type=refresh_token -d refresh_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsidGVubmlzY29ycG9yZXNvdXJjZWlkIl0sInVzZXJfbmFtZSI6Imxlb3BvbGQiLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXSwiYXRpIjoiMTM3MGRkOWItMTdkZS00NTc0LTk5YzYtZGFhMDlhMTlmNjgzIiwiZXhwIjoxNTI0NTgyMDMyLCJhdXRob3JpdGllcyI6WyJBRE1JTl9VU0VSIl0sImp0aSI6ImYyMzM2YmMzLTE1NGMtNDU1OC04NmMxLWU1MzQ1MjQ3ZjNjMiIsImNsaWVudF9pZCI6InRlbm5pc2NvcnBvY2xpZW50aWQifQ.VnVS43yqHMETgqQUSBxCESYr7_7aKymlYDzOOl0t1_o


 */