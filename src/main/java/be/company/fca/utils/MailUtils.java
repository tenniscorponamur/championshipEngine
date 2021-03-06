package be.company.fca.utils;

import be.company.fca.model.Rencontre;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MailUtils {

    private static final String MAIL_FROM = "tenniscorponamur@gmail.com";

    /**
     * Permet d'envoyer le nouveau mot de passe a un utilisateur
     * @param mailTo
     * @param password
     * @return true si le mail est bien parti
     */
    public static boolean sendPasswordMail(String prenom, String nom, String mailTo, String password) {
        return sendPasswordMailUsingMailjet(prenom,nom,mailTo,password);
    }

    public static boolean sendRappelRencontre(Rencontre rencontre){

        // Le systeme ne va s'adresser qu'au capitaine qui pourra relayer dans son equipe

        String frontEndUrl = System.getenv("FRONT_END_URL");
        String subject = "Encodage rencontre Tennis Corpo Namur";

        if (rencontre.getEquipeVisites()!=null
                && rencontre.getEquipeVisites().getCapitaine()!=null
                && !StringUtils.isEmpty(rencontre.getEquipeVisites().getCapitaine().getMail())) {

            String htmlContent = getRappelRencontreHtmlContent(frontEndUrl, rencontre);

            if (isMailJetConfigured()){
                return sendMailUsingMailjet(rencontre.getEquipeVisites().getCapitaine().getPrenom(),
                        rencontre.getEquipeVisites().getCapitaine().getNom(),
                        rencontre.getEquipeVisites().getCapitaine().getMail(),subject,htmlContent);
            }else{
                System.err.println("Rappel envoyé : \n" + htmlContent);
                return true;
            }

        }
        return false;

    }

    /**
     * Permet d'envoyer le nouveau mot de passe a un utilisateur
     * @param mailTo
     * @param password
     * @return true si le mail est bien parti
     */
    private static boolean sendPasswordMailUsingMailjet(String prenom, String nom, String mailTo, String password) {
        String frontEndUrl = System.getenv("FRONT_END_URL");
        String subject = "Votre mot de passe pour Tennis Corpo Namur";
        String htmlContent = getPasswordHtmlContent(frontEndUrl, prenom, nom, password);
        if (isMailJetConfigured()){
            return sendMailUsingMailjet(prenom,nom,mailTo,subject,htmlContent);
        }else{
            System.err.println("Nouveau mot de passe : " + password);
            return true;
        }
    }

    /**
     * Permet de savoir si MailJet a ete configure dans l'environnement
     * @return
     */
    private static boolean isMailJetConfigured(){
        String apiPublicKey = System.getenv("MAILJET_APIKEY_PUBLIC");
        String apiPrivateKey = System.getenv("MAILJET_APIKEY_PRIVATE");
        if (!StringUtils.isEmpty(apiPublicKey) && !StringUtils.isEmpty(apiPrivateKey)){
            return true;
        }
        return false;
    }

    /**
     * Permet d'envoyer le nouveau mot de passe a un utilisateur
     * @param mailTo
     * @param subject
     * @param htmlContent
     * @return true si le mail est bien parti
     */
    private static boolean sendMailUsingMailjet(String prenom, String nom, String mailTo, String subject, String htmlContent){

        // ApiKey a preciser dans l'environnement de production
        // En test, le mot de passe s'inscrira dans la console

        String apiPublicKey = System.getenv("MAILJET_APIKEY_PUBLIC");
        String apiPrivateKey = System.getenv("MAILJET_APIKEY_PRIVATE");

        if (!StringUtils.isEmpty(apiPublicKey) && !StringUtils.isEmpty(apiPrivateKey)){

            try{

                MailjetClient client;
                MailjetRequest request;
                MailjetResponse response;
                client = new MailjetClient(apiPublicKey, apiPrivateKey, new ClientOptions("v3.1"));
                request = new MailjetRequest(Emailv31.resource)
                        .property(Emailv31.MESSAGES, new JSONArray()
                                .put(new JSONObject()
                                        .put(Emailv31.Message.FROM, new JSONObject()
                                                .put("Email", MAIL_FROM)
                                                .put("Name", "Tennis Corpo Namur"))
                                        .put(Emailv31.Message.TO, new JSONArray()
                                                .put(new JSONObject()
                                                        .put("Email", mailTo)
                                                        .put("Name", prenom + " " + nom)))
                                        .put(Emailv31.Message.SUBJECT, subject)
                                        .put(Emailv31.Message.HTMLPART, htmlContent)));
                response = client.post(request);
                System.out.println(response.getStatus());
                System.out.println(response.getData());
                return true;

            }catch(Exception e){
                e.printStackTrace(System.err);
                return false;
            }

        }

        return false;

    }

//    /**
//     * Permet d'envoyer le nouveau mot de passe a un utilisateur en utilisant SendGrid
//     * @param mailTo
//     * @param password
//     * @return true si le mail est bien parti
//     */
//    private static boolean sendPasswordMailUsingSendGrid(String prenom, String nom, String mailTo, String password){
//
//        // ApiKey a preciser dans l'environnement de production
//        // En test, le mot de passe s'inscrira dans la console
//
//        String apiKey = System.getenv("SENDGRID_API_KEY");
//        String frontEndUrl = System.getenv("FRONT_END_URL");
//
//        String subject = "Votre mot de passe pour Tennis Corpo Namur";
//        String htmlContent = getHtmlContent(frontEndUrl,prenom,nom,password);
//
//        if (!StringUtils.isEmpty(apiKey)){
//
//            Content content = new Content("text/html", htmlContent);
//            Mail mail = new Mail(new Email(MAIL_FROM), subject, new Email(mailTo), content);
//
//            SendGrid sg = new SendGrid(apiKey);
//            Request request = new Request();
//            try {
//                request.setMethod(Method.POST);
//                request.setEndpoint("mail/send");
//                request.setBody(mail.build());
//                Response response = sg.api(request);
//                System.out.println(response.getStatusCode());
//                return true;
//
//            } catch (IOException ex) {
//                ex.printStackTrace(System.err);
//                return false;
//            }
//
//        }else{
//
//            System.err.println("Nouveau mot de passe : " + password);
//            return true;
//
//        }
//
//    }

    private static String getRappelRencontreHtmlContent(String frontEndUrl, Rencontre rencontre){

        String link = "<strong>Tennis Corpo Namur</strong>";
        if (!StringUtils.isEmpty(frontEndUrl)){
            link = "<a href=\"" + frontEndUrl + "\">" + link + "</a>";
        }

        String prenom = "";
        String nom = "";

        if (rencontre.getEquipeVisites()!=null && rencontre.getEquipeVisites().getCapitaine()!=null){
            prenom = rencontre.getEquipeVisites().getCapitaine().getPrenom();
            nom = rencontre.getEquipeVisites().getCapitaine().getNom();

        }

        String htmlContent = "<p>Bonjour " + prenom + " " + nom + ",</p>\n" +
                "<p>Pourriez-vous proc&eacute;der à l'encodage des r&eacute;sultats de la rencontre "
                + rencontre.getEquipeVisites().getCodeAlphabetique() + " - "
                + rencontre.getEquipeVisiteurs().getCodeAlphabetique()
                + " dans l'application " + link + " ? </p>\n" +
                "<p>Cette rencontre devait initialement avoir lieu le " + new SimpleDateFormat("dd/MM/yyyy").format(rencontre.getDateHeureRencontre())
                + (rencontre.getTerrain()!=null?(" &agrave; " + rencontre.getTerrain()):"")
                + " dans le cadre du championnat " + rencontre.getDivision().getChampionnat().getType().toString()
                + " en Division " + rencontre.getDivision().getNumero() + " " + rencontre.getDivision().getChampionnat().getCategorie().toString() + ".</p>\n"
                + "<p>En cas de probl&egrave;me, prenez contact via l'adresse suivante : " + "<a href=\"mailto:tenniscorponamur@gmail.com\">tenniscorponamur@gmail.com</a>.</p>\n"
                + "<p>Merci d'avance,</p>\n" +
                "<p>L'&eacute;quipe Tennis Corpo Namur</p>";

        return htmlContent;
    }


    private static String getPasswordHtmlContent(String frontEndUrl, String prenom, String nom, String password){

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

        return htmlContent;

    }

}
