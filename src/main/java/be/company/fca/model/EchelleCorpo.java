package be.company.fca.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EchelleCorpo {

    public static List<EchelleCorpo> getAllEchellesCorpo(){
        List<EchelleCorpo> echellesCorpo = new ArrayList<>();
        for (int i=0;i<22;i++){
            echellesCorpo.add(new EchelleCorpo(i*5+5));
        }

        return echellesCorpo;
    }

    /**
     * Permet de recuperer la table correspondance en points classement homme
     * des classements Dames
     * @return
     */
    public static Map<Integer,Integer> getCorrespondancePointsHommeFemme(){
        Map<Integer, Integer> map=new HashMap<>();
        for (EchelleCorpo echelleCorpo : EchelleCorpo.getAllEchellesCorpo()){
            map.put(echelleCorpo.getPoints(),Math.max(5,echelleCorpo.getPoints()-10));
        }
        return map;
    }

    public EchelleCorpo(int points) {
        this.points = points;
    }

    private int points;

    public int getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return "EchelleCorpo{" +
                "points=" + points +
                '}';
    }
}
