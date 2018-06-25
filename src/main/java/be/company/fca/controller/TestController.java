package be.company.fca.controller;

import be.company.fca.utils.ReportUtils;
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

    @RequestMapping("/public")
    String home() {
        return "Tennis Corpo Engine started !";
    }

    @RequestMapping("/testRapport")
    ResponseEntity<byte[]> testRapport() throws Exception {

        JasperReport jasperReport = JasperCompileManager.compileReport(ReportUtils.getRapportTest());

        // Get the connection
        Connection conn = datasource.getConnection();

        // Generate jasper print

        JasperPrint jprint = JasperFillManager.fillReport(jasperReport, new HashMap(), conn);

        byte[] pdfFile =  JasperExportManager.exportReportToPdf(jprint);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        String filename = "RapportTest.pdf";
//        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        headers.add("content-disposition", "inline; filename=" + filename);
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(pdfFile, headers, HttpStatus.OK);
        return response;
    }

    @RequestMapping("/public/testWithoutAuth")
    String testWithoutAuth() {
        return "Test public";
    }

    @RequestMapping("/private/testWithAuth")
    @PreAuthorize("hasAuthority('ADMIN_USER') or hasAuthority('STANDARD_USER')")
    String testWithAuth() {
        return "Test authentification";
    }

}
