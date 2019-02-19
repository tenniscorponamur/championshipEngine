package be.company.fca.utils;

import com.mailjet.client.MailjetClient;
import com.sendgrid.*;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;

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
        return sendPasswordMailUsingMailjet(prenom,nom,mailTo,password);
    }

    /**
     * Permet d'envoyer le nouveau mot de passe a un utilisateur
     * @param mailTo
     * @param password
     * @return true si le mail est bien parti
     */
    private static boolean sendPasswordMailUsingMailjet(String prenom, String nom, String mailTo, String password){

        try{

            MailjetClient client;
            MailjetRequest request;
            MailjetResponse response;
            client = new MailjetClient(System.getenv("MAILJET_APIKEY_PUBLIC"), System.getenv("MAILJET_APIKEY_PRIVATE"), new ClientOptions("v3.1"));
            request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                    .put(Emailv31.Message.FROM, new JSONObject()
                                            .put("Email", "no-reply@tenniscorponamur.be")
                                            .put("Name", "Test de mail"))
                                    .put(Emailv31.Message.TO, new JSONArray()
                                            .put(new JSONObject()
                                                    .put("Email", "fabrice.calay@gmail.com")
                                                    .put("Name", "Fabrice")))
                                    .put(Emailv31.Message.SUBJECT, "Your email flight plan!")
                                    .put(Emailv31.Message.TEXTPART, "Dear passenger 1, welcome to Mailjet! May the delivery force be with you!")
                                    .put(Emailv31.Message.HTMLPART, "<h3>Dear passenger 1, welcome to Mailjet!</h3><br />May the delivery force be with you!")));
            response = client.post(request);
            System.out.println(response.getStatus());
            System.out.println(response.getData());
            return true;

        }catch(Exception e){
            e.printStackTrace(System.err);
            return false;
        }

    }

    /**
     * Permet d'envoyer le nouveau mot de passe a un utilisateur en utilisant SendGrid
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
