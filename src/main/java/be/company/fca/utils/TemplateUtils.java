package be.company.fca.utils;

import java.io.InputStream;

public class TemplateUtils {

    public static InputStream getTemplateImportMembres(){
        return ReportUtils.class.getResourceAsStream("/templates/echantillonMembre.xls");
    }

    public static InputStream getTemplateLigue(){
        return ReportUtils.class.getResourceAsStream("/templates/templateLigue.xls");
    }
}
