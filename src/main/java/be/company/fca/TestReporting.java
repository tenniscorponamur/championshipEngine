package be.company.fca;

import net.sf.jasperreports.engine.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;

public class TestReporting {

    public static void main(String[] args) throws Exception {
        String jrxmlFileName = "C:/Users/47965/JaspersoftWorkspace/MyReports/RapportTest.jrxml";

        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlFileName);

        // String dbUrl = props.getProperty("jdbc.url");
        String dbUrl = "jdbc:postgresql://localhost:5432/tennisCorpo";
        // String dbDriver = props.getProperty("jdbc.driver");
        String dbDriver = "org.postgresql.Driver";
        // String dbUname = props.getProperty("db.username");
        String dbUname = "fca";
        // String dbPwd = props.getProperty("db.password");
        String dbPwd = "fca";

        // Load the JDBC driver
        Class.forName(dbDriver);
        // Get the connection
        Connection conn = DriverManager.getConnection(dbUrl, dbUname, dbPwd);

        // Generate jasper print

        JasperPrint jprint = (JasperPrint) JasperFillManager.fillReport(jasperReport, new HashMap(), conn);

        // Export pdf file
        JasperExportManager.exportReportToPdfFile(jprint, "D:/jasperTest.pdf");

    }
}
