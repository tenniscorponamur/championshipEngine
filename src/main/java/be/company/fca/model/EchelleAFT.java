package be.company.fca.model;

import java.util.ArrayList;
import java.util.List;

public class EchelleAFT {

    public static List<EchelleAFT> getAllEchellesAFT(){
        List<EchelleAFT> echellesAFT = new ArrayList<>();
        echellesAFT.add(new EchelleAFT("C30.6",3,true));
        echellesAFT.add(new EchelleAFT("C30.5",5,true));
        echellesAFT.add(new EchelleAFT("C30.4",10,true));
        echellesAFT.add(new EchelleAFT("C30.3",15,true));
        echellesAFT.add(new EchelleAFT("C30.2",20,true));
        echellesAFT.add(new EchelleAFT("C30.1",25,true));
        echellesAFT.add(new EchelleAFT("C30",30,true));
        echellesAFT.add(new EchelleAFT("C15.5",35,true));
        echellesAFT.add(new EchelleAFT("C15.4",40,true));
        echellesAFT.add(new EchelleAFT("C15.3",45,true));
        echellesAFT.add(new EchelleAFT("C15.2",50,true));
        echellesAFT.add(new EchelleAFT("C15.1",55,true));
        echellesAFT.add(new EchelleAFT("C15",60,true));
        echellesAFT.add(new EchelleAFT("B+4/6",65,true));
        echellesAFT.add(new EchelleAFT("B+2/6",70,true));
        echellesAFT.add(new EchelleAFT("B0",75,true));
        echellesAFT.add(new EchelleAFT("B-2/6",80,true));
        echellesAFT.add(new EchelleAFT("B-4/6",85,true));
        echellesAFT.add(new EchelleAFT("B-15",90,true));
        echellesAFT.add(new EchelleAFT("B-15.1",95,true));
        echellesAFT.add(new EchelleAFT("B-15.2",100,true));
        echellesAFT.add(new EchelleAFT("B-15.4",105,true));
        echellesAFT.add(new EchelleAFT("NC",5,false));

        return echellesAFT;
    }

    /**
     * Permet de recuperer l'echelle AFT sur base de son code
     * @param codeAft
     * @return
     */
    public static EchelleAFT getEchelleAFTByCode(String codeAft){
        List<EchelleAFT> echellesAFT = EchelleAFT.getAllEchellesAFT();
        for (EchelleAFT echelleAFT : echellesAFT){
            if (echelleAFT.getCode().equals(codeAft)){
                return echelleAFT;
            }
        }
        return null;
    }

    EchelleAFT(String code, int points, boolean actif) {
        this.code = code;
        this.points = points;
        this.actif = actif;
    }

    private String code;
    private int points;
    private boolean actif;
    public String getCode() {
        return code;
    }

    public int getPoints() {
        return points;
    }

    public boolean isActif() {
        return actif;
    }

    @Override
    public String toString() {
        return "EchelleAFT{" +
                "code='" + code + '\'' +
                ", points=" + points +
                '}';
    }
}
