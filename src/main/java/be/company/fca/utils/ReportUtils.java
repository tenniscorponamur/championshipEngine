package be.company.fca.utils;

import java.io.FileInputStream;
import java.io.InputStream;

public class ReportUtils {

    public static InputStream getRapportTest(){
        return ReportUtils.class.getResourceAsStream("/reports/RapportTest.jrxml");
    }
}
