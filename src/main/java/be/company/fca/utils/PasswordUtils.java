package be.company.fca.utils;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtils {

    public static String DEFAULT_PASSWORD = "$2a$10$Uq.lmbGwvrynxRMoMLoYt.rFotX.PykXYpk0L1Pze8mrE3neN7ALi";

    public static String DEFAULT_MEMBER_PASSWORD = "$2a$10$5J7DVDpYAmgwa7rF4k9LweMNS4/aVGYkhgv7Np3MIpDvHU9wlvISm";

    public static String generatePassword(){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789;:,.?";
        String pwd = RandomStringUtils.random( 6, characters );
        return pwd;
    }
}
