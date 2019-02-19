package be.company.fca.controller;

import be.company.fca.utils.MailUtils;
import be.company.fca.utils.ReportUtils;
import com.sendgrid.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRHtmlExporterContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;

@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api")
public class TestController {

    @Autowired
    DataSource datasource;

    @RequestMapping("/public/testMail")
    String testMail() {
        if (MailUtils.sendPasswordMail("Fabrice","Calay", "fabrice.calay@gmail.com", "TEST")){
            return "Mail envoye";
        }else{
            return "Probleme lors de l'envoi du mail";
        }
    }

    @RequestMapping("/public")
    String home() {
        return "Tennis Corpo Engine started !";
    }

//    @RequestMapping("/login/google")
//    String loginGoogle() {
//        return "Tennis Corpo Engine login with Google !";
//    }
//
//    @RequestMapping("/testRapport")
//    ResponseEntity<byte[]> testRapport() throws Exception {
//
//        JasperReport jasperReport = JasperCompileManager.compileReport(ReportUtils.getRapportTest());
//
//        // Get the connection
//        Connection conn = datasource.getConnection();
//
//        // Generate jasper print
//
//        JasperPrint jprint = JasperFillManager.fillReport(jasperReport, new HashMap(), conn);
//
//        byte[] pdfFile =  JasperExportManager.exportReportToPdf(jprint);
//
//        conn.close();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.parseMediaType("application/pdf"));
////        String filename = "RapportTest.pdf";
////        headers.setContentDispositionFormData(filename, filename);
////        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
////        headers.add("content-disposition", "inline; filename=" + filename);
//        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(pdfFile, headers, HttpStatus.OK);
//        return response;
//    }

    @RequestMapping("/public/testWithoutAuth")
    String testWithoutAuth() {
        return "Test public";
    }

/*
    @RequestMapping("/public/testMail")
    void testMail(@RequestParam String mailAdress) {

        Email from = new Email("noreply@tenniscorponamur.be");
        String subject = "Hello World from the SendGrid Java Library!";
        Email to = new Email(mailAdress);
        Content content = new Content("text/plain", "Hello, Email!");
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
    */

    @RequestMapping("/private/testWithAuth")
    @PreAuthorize("hasAuthority('ADMIN_USER') or hasAuthority('STANDARD_USER')")
    String testWithAuth() {
        return "Test authentification";
    }

}
