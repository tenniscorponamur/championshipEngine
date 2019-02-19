package be.company.fca.utils;

import com.sendgrid.*;
import sendinblue.*;
import sendinblue.auth.*;
import sibApi.SmtpApi;
import sibModel.*;
import sibApi.AccountApi;

import java.io.File;
import java.util.*;
import org.apache.commons.lang.StringUtils;


import java.io.IOException;

public class MailUtils {


    //TODO : alternative possible : SendInBlue

    /**
     * Permet d'envoyer le nouveau mot de passe a un utilisateur
     * @param mailTo
     * @param password
     * @return true si le mail est bien parti
     */
    public static boolean sendPasswordMail(String prenom, String nom, String mailTo, String password) {
        return sendPasswordMailUsingSendinBlue(prenom,nom,mailTo,password);
    }

    private static boolean sendPasswordMailUsingSendinBlue(String prenom, String nom, String mailTo, String password){
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        // Configure API key authorization: api-key
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey("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");

        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //apiKey.setApiKeyPrefix("Token");

        SmtpApi apiInstance = new SmtpApi();
        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail(); // SendSmtpEmail | Values to send a transactional email
        SendSmtpEmailSender sendSmtpEmailSender = new SendSmtpEmailSender();
        sendSmtpEmailSender.setEmail("tenniscorponamur@gmail.com");
        sendSmtpEmailSender.setName("Tennis Corpo Namur");
        sendSmtpEmail.setSender(sendSmtpEmailSender);
        sendSmtpEmail.setSubject("Ceci est mon sujet");
        sendSmtpEmail.setHtmlContent("Ceci est mon contenu");
        SendSmtpEmailTo sendSmtpEmailTo = new SendSmtpEmailTo();
        sendSmtpEmailTo.setName(prenom + nom);
        sendSmtpEmailTo.setEmail(mailTo);
        sendSmtpEmail.setTo(Collections.singletonList(sendSmtpEmailTo));

        try {
            CreateSmtpEmail result = apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println(result);
            return true;
        } catch (ApiException e) {
            System.err.println("Exception when calling SmtpApi#sendTransacEmail");
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Permet d'envoyer le nouveau mot de passe a un utilisateur
     * @param mailTo
     * @param password
     * @return true si le mail est bien parti
     */
    private static boolean sendPasswordMailUsingSendGrid(String prenom, String nom, String mailTo, String password){

        // ApiKey a preciser dans l'environnement de production
        // En test, le mot de passe s'inscrira dans la console

        String apiKey = System.getenv("SENDGRID_API_KEY");
        String frontEndUrl = System.getenv("FRONT_END_URL");
        String backupMail = System.getenv("BACKUP_MAIL");

        if (!StringUtils.isEmpty(apiKey)){
            Email from = new Email("noreply@tenniscorponamur.be");
            String subject = "Votre mot de passe pour Tennis Corpo Namur";
            Email to = new Email(mailTo);

            String link = "<strong>Tennis Corpo Namur</strong>";
            if (!StringUtils.isEmpty(frontEndUrl)){
                link = "<a href=\"" + frontEndUrl + "\">" + link + "</a>";
            }

            String htmlContent = "<p>Bonjour " + prenom + " " + nom + ",</p>\n" +
                    "<p>Vous trouverez ci-dessous votre nouveau mot de passe pour acc&eacute;der &agrave; l'application " + link + ".</p>\n" +
                    "<p>Votre nom d'utilisateur correspond à votre numéro AFT.</p>\n" +
                    "<p>Nouveau mot de passe : <strong>" + password + "</strong></p>\n" +
                    "<p>Vous pouvez &agrave; tout moment modifier ce mot de passe une fois connect&eacute; &agrave; l'application.</p>\n" +
                    "<p>Salutations,</p>\n" +
                    "<p>L'&eacute;quipe Tennis Corpo Namur</p>";

            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);

            if (!StringUtils.isEmpty(backupMail)){
                Personalization personalization = new Personalization();
                personalization.addBcc(new Email(backupMail));
                mail.addPersonalization(personalization);
            }

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            try {
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);
                System.out.println(response.getStatusCode());
                return true;

            } catch (IOException ex) {
                ex.printStackTrace(System.err);
                return false;
            }

        }else{

            System.err.println("Nouveau mot de passe : " + password);
            return true;

        }

    }

}
