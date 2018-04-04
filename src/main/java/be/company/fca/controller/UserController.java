package be.company.fca.controller;

import be.company.fca.dto.UserDto;
import be.company.fca.model.User;
import be.company.fca.repository.UserRepository;
import be.company.fca.utils.PasswordUtils;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des utilisateurs")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @ApiOperation(value = "Get current user",
            notes = "Ceci est une méthode privée pour récupérer l'utilisateur reconnu par le token d'accès")
    @RequestMapping(method=RequestMethod.GET, path="/private/currentUser")
    public UserDto getCurrentUser(Principal principal) {
        return new UserDto(userRepository.findByUsername(principal.getName()));
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/users", method= RequestMethod.GET)
    public Iterable<User> getAllUsers(){
        return userRepository.findAll();
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/user/{id}", method= RequestMethod.GET)
    public User getUserById(@PathVariable Long id){
        return userRepository.findOne(id);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/user/{id}", method = RequestMethod.PUT)
    public UserDto updateUser(@PathVariable Integer id, @RequestBody UserDto userDto){
        User user = userRepository.findOne(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setPrenom(userDto.getPrenom());
        user.setNom(userDto.getNom());
        userRepository.save(user);
        return new UserDto(user);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/user", method = RequestMethod.POST)
    public UserDto addUser(@RequestBody UserDto userDto){
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPrenom(userDto.getPrenom());
        user.setNom(userDto.getNom());
        user.setPassword(PasswordUtils.DEFAULT_PASSWORD);
        userRepository.save(user);
        return new UserDto(user);
    }

//    @RequestMapping(value = "/public/defaultUser", method = RequestMethod.GET)
//    public UserDto getDefaultUser(){
//        return new UserDto(userRepository.findByUsername("fca"));
//    }
//
//    @ApiOperation(value = "Find user public",
//            notes = "Ceci est une méthode publique pour recupérer un utilisateur fictif...")
//    @RequestMapping(method=RequestMethod.GET, path="/public/user")
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Successful retrieval of user detail", response = User.class),
//            @ApiResponse(code = 404, message = "User does not exist"),
//            @ApiResponse(code = 500, message = "Internal server error")}
//    )
//    public Map userPublic(Principal principal, @ApiParam(value = "Nom d'utilisateur") @RequestParam(value="username",required = false) String username) {
//        Map map = new HashMap();
//        map.put("principal","Essai");
//        return map;
//    }

}


/*


@ApiModel
public class User {

 private String userName;

 @ApiModelProperty(position = 1, required = true, value = "username containing only lowercase letters or numbers")
 public String getUserName() {
     return userName;
 }
}

 */