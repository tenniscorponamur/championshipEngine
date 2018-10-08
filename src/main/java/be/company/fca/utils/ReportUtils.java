package be.company.fca.utils;

import java.io.FileInputStream;
import java.io.InputStream;

public class ReportUtils {

    public static InputStream getRapportTest(){
        return ReportUtils.class.getResourceAsStream("/reports/RapportTest.jrxml");
    }

    public static InputStream getListeForceTemplate(){
        return ReportUtils.class.getResourceAsStream("/reports/listeForce.jrxml");
    }

    public static InputStream getListeCapitainesTemplate(){
        return ReportUtils.class.getResourceAsStream("/reports/listeCapitaines.jrxml");
    }


}
