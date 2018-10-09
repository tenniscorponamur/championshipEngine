package be.company.fca.utils;

import org.springframework.util.StringUtils;

import java.util.Arrays;

public class LocaliteUtils {

    static String[] codesPostauxGrandNamur = new String[]{"5000","5101","5020","5024","5002","5004","5100","5003","5001","5021","5022"};
    static String[] codesPostauxProvinceNamur = new String[]{"5000","5001","5002","5003","5004","5020","5021","5022","5024","5030","5031","5032","5060","5070","5080","5081","5100","5101","5140","5150","5170","5190","5300","5310","5330","5332","5333","5334","5336","5340","5350","5351","5352","5353","5354","5360","5361","5362","5363","5364","5370","5372","5374","5376","5377","5380","5500","5501","5502","5503","5504","5520","5521","5522","5523","5524","5530","5537","5540","5541","5542","5543","5544","5550","5555","5560","5561","5562","5563","5564","5570","5571","5572","5573","5574","5575","5576","5580","5590","5600","5620","5621","5630","5640","5641","5644","5646","5650","5651","5660","5670","5680"};

    /**
     * Permet de savoir si une localite (via son code postal) fait partie du grand namur
     * @param codePostal
     * @return
     */
    public static boolean isGrandNamur(String codePostal){
        if (!StringUtils.isEmpty(codePostal)){
            return Arrays.asList(codesPostauxGrandNamur).contains(codePostal);
        }
        return false;
    }


    /**
     * Permet de savoir si une localite (via son code postal) fait partie de la province de namur
     * @param codePostal
     * @return
     */
    public static boolean isProvinceNamur(String codePostal){
        if (!StringUtils.isEmpty(codePostal)){
            return Arrays.asList(codesPostauxProvinceNamur).contains(codePostal);
        }
        return false;
    }
}
