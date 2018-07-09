package be.company.fca.model;

import java.util.ArrayList;
import java.util.List;

public class EchelleAFT {

    public static List<EchelleAFT> getAllEchellesAFT(){
        List<EchelleAFT> echellesAFT = new ArrayList<>();
        echellesAFT.add(new EchelleAFT("NC",5));
        echellesAFT.add(new EchelleAFT("C30.5",5));
        echellesAFT.add(new EchelleAFT("C30.4",10));
        echellesAFT.add(new EchelleAFT("C30.3",15));
        echellesAFT.add(new EchelleAFT("C30.2",20));
        echellesAFT.add(new EchelleAFT("C30.1",25));
        echellesAFT.add(new EchelleAFT("C30",30));
        echellesAFT.add(new EchelleAFT("C15.5",35));
        echellesAFT.add(new EchelleAFT("C15.4",40));
        echellesAFT.add(new EchelleAFT("C15.3",45));
        echellesAFT.add(new EchelleAFT("C15.2",50));
        echellesAFT.add(new EchelleAFT("C15.1",55));
        echellesAFT.add(new EchelleAFT("C15",60));
        echellesAFT.add(new EchelleAFT("B+4/6",65));
        echellesAFT.add(new EchelleAFT("B+2/6",70));
        echellesAFT.add(new EchelleAFT("B0",75));
        echellesAFT.add(new EchelleAFT("B-2/6",80));
        echellesAFT.add(new EchelleAFT("B-4/6",85));
        echellesAFT.add(new EchelleAFT("B-15",90));
        echellesAFT.add(new EchelleAFT("B-15.1",95));
        echellesAFT.add(new EchelleAFT("B-15.2",100));
        echellesAFT.add(new EchelleAFT("B-15.4",105));

        return echellesAFT;
    }

    EchelleAFT(String code, int points) {
        this.code = code;
        this.points = points;
    }

    private String code;
    private int points;

    public String getCode() {
        return code;
    }

    public int getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return "EchelleAFT{" +
                "code='" + code + '\'' +
                ", points=" + points +
                '}';
    }
}
