package be.company.fca.controller;

import be.company.fca.dto.ChangePasswordDto;
import be.company.fca.dto.UserDto;
import be.company.fca.model.Membre;
import be.company.fca.model.Role;
import be.company.fca.model.User;
import be.company.fca.repository.MembreRepository;
import be.company.fca.repository.UserRepository;
import be.company.fca.utils.MailUtils;
import be.company.fca.utils.PasswordUtils;
import be.company.fca.utils.UserUtils;
import io.swagger.annotations.*;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des utilisateurs")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembreRepository membreRepository;

    @ApiOperation(value = "Get current user",
            notes = "Ceci est une méthode privée pour récupérer l'utilisateur reconnu par le token d'accès")
    @RequestMapping(method=RequestMethod.GET, path="/private/user/current")
    public UserDto getCurrentUser(Principal principal) {

        // Pour l'authentification des membres
        // Si le user n'existe pas, on va regarder dans les membres actifs sur base du numero AFT

        User user = userRepository.findByUsername(principal.getName().toLowerCase());
        if (user==null){
            Membre membre = membreRepository.findByNumeroAft(principal.getName());
            user = UserUtils.getUserFromMembre(membre);
        }
        return new UserDto(user,UserUtils.getRoles(user));
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/users", method= RequestMethod.GET)
    public List<UserDto> getAllUsers(){
        List<UserDto> usersDto = new ArrayList<>();
        List<User> userList = (List<User>) userRepository.findAll();
        for (User user : userList){
            usersDto.add(new UserDto(user,UserUtils.getRoles(user)));
        }
        return usersDto;
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
        user.setAdmin(userDto.isAdmin());
        if (userDto.getMembre()!=null && userDto.getMembre().getId()!=null){
            Membre membre = membreRepository.findOne(userDto.getMembre().getId());
            user.setMembre(membre);
        }else{
            user.setMembre(null);
        }
        userRepository.save(user);
        return new UserDto(user,UserUtils.getRoles(user));
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/user", method = RequestMethod.POST)
    public UserDto addUser(@RequestBody UserDto userDto){
        User user = new User();
        user.setUsername(userDto.getUsername().toLowerCase());
        user.setPrenom(userDto.getPrenom());
        user.setNom(userDto.getNom());
        user.setAdmin(userDto.isAdmin());
        if (userDto.getMembre()!=null && userDto.getMembre().getId()!=null){
            Membre membre = membreRepository.findOne(userDto.getMembre().getId());
            user.setMembre(membre);
        }else{
            user.setMembre(null);
        }
        user.setPassword(PasswordUtils.DEFAULT_PASSWORD);
        userRepository.save(user);
        return new UserDto(user,UserUtils.getRoles(user));
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/user", method = RequestMethod.DELETE)
    public void deleteUser(@RequestParam Long userId){
        userRepository.delete(userId);
    }


    @RequestMapping(value = "/private/user/changePassword", method = RequestMethod.PUT)
    public boolean updatePassword(Authentication authentication, @RequestBody ChangePasswordDto changePasswordDto ){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = userRepository.findByUsername(authentication.getName());
        if (user!=null){
            // On teste si le mot de passe coïncide
            if (encoder.matches(changePasswordDto.getOldPassword(),user.getPassword())) {
                user.setPassword(encoder.encode(changePasswordDto.getNewPassword()));
                userRepository.save(user);
                return true;
            }
        }else{
            Membre membre = membreRepository.findByNumeroAft(authentication.getName());
            // On teste si le mot de passe coïncide
            if (encoder.matches(changePasswordDto.getOldPassword(),membre.getPassword())) {
                membre.setPassword(encoder.encode(changePasswordDto.getNewPassword()));
                membreRepository.save(membre);
                return true;
            }
        }
        return false;
    }


    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/user/resetPassword", method = RequestMethod.POST)
    public boolean resetUserPassword(@RequestParam Long id){

        User user = userRepository.findOne(id);
        user.setPassword(PasswordUtils.DEFAULT_PASSWORD);
        userRepository.save(user);

        return true;
    }

    @RequestMapping(value = "/public/user/askPassword", method = RequestMethod.POST)
    public boolean getOfficialAFT(@RequestParam("numeroAft") String numeroAft, @RequestBody String captchaResponse){

        // secretKey en variable d'environnement

        String recaptchaSecretKey = System.getenv("RECAPTCHA_SECRET_KEY");
        if (StringUtils.isEmpty(recaptchaSecretKey)){
            return false;
        }

        boolean captchaCheck = false;
        try{
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(headers);
            String result = restTemplate.postForObject("https://www.google.com/recaptcha/api/siteverify?secret="+recaptchaSecretKey+"&response="+captchaResponse, request,String.class);

            JsonNode jsonNode = new ObjectMapper().readTree(result);
            captchaCheck = jsonNode.findValue("success").asBoolean();

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

        //Si le captcha est correct, on continue
        if (!captchaCheck){
            return false;
        }

        // Verifier que le numero Aft est connu
        Membre membre = membreRepository.findByNumeroAft(numeroAft);
        if (membre==null){
            return false;
        }

        // Verifier qu'il y a bien un mail connu
        if (!StringUtils.isEmpty(membre.getMail())){
            String newPassword = PasswordUtils.generatePassword();
            boolean mailSended = MailUtils.sendPasswordMail(membre.getPrenom(),membre.getNom(),membre.getMail(),newPassword);
            // Verifier que le mail a bien ete envoye
            if (mailSended){
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                membre.setPassword(encoder.encode(newPassword));
                membreRepository.save(membre);
                return true;
            }
        }

        return false;
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