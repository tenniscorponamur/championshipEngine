package be.company.fca.controller;

import be.company.fca.dto.ChangePasswordDto;
import be.company.fca.dto.UserDto;
import be.company.fca.model.Role;
import be.company.fca.model.User;
import be.company.fca.repository.UserRepository;
import be.company.fca.utils.PasswordUtils;
import be.company.fca.utils.UserUtils;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des utilisateurs")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @ApiOperation(value = "Get current user",
            notes = "Ceci est une méthode privée pour récupérer l'utilisateur reconnu par le token d'accès")
    @RequestMapping(method=RequestMethod.GET, path="/private/user/current")
    public UserDto getCurrentUser(Principal principal) {
        User user = userRepository.findByUsername(principal.getName().toLowerCase());
        return new UserDto(user, UserUtils.getDefaultRoles());
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/users", method= RequestMethod.GET)
    public Iterable<User> getAllUsers(){
        return userRepository.findAll();
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/user", method= RequestMethod.GET)
    public User getUserById(@RequestParam Long id){
        return userRepository.findOne(id);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/user", method = RequestMethod.PUT)
    public UserDto updateUser(@RequestBody UserDto userDto){
        User user = userRepository.findOne(userDto.getId());
        user.setUsername(userDto.getUsername().toLowerCase());
        user.setPrenom(userDto.getPrenom());
        user.setNom(userDto.getNom());
        userRepository.save(user);
        return new UserDto(user,UserUtils.getDefaultRoles());
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/user", method = RequestMethod.POST)
    public UserDto addUser(@RequestBody UserDto userDto){
        User user = new User();
        user.setUsername(userDto.getUsername().toLowerCase());
        user.setPrenom(userDto.getPrenom());
        user.setNom(userDto.getNom());
        user.setPassword(PasswordUtils.DEFAULT_PASSWORD);
        userRepository.save(user);
        return new UserDto(user,UserUtils.getDefaultRoles());
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/user/changePassword", method = RequestMethod.PUT)
    public boolean updatePassword(Authentication authentication, @PathVariable Long id, @RequestBody ChangePasswordDto changePasswordDto ){

        authentication.getName();

        new BCryptPasswordEncoder().encode(changePasswordDto.getOldPassword());

        if (true){

        }

        new BCryptPasswordEncoder().encode(changePasswordDto.getNewPassword());

        //TODO : return false si l'ancien mot de passe ne correspond pas a celui qui est connu

        return false;
    }


    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/user/resetPassword", method = RequestMethod.POST)
    public boolean resetPassword(@RequestParam Long id){

        User user = userRepository.findOne(id);
        user.setPassword(PasswordUtils.DEFAULT_PASSWORD);
        userRepository.save(user);

        return true;
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