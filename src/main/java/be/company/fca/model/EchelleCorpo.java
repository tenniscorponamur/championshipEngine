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
            map.put(echelleCorpo.getPoints(),Math.max(5,getManPointsFromWomanPoints(echelleCorpo.points)));
        }
        return map;
    }

    /**
     * Permet de recuperer la valeur correspondant en points homme pour les points d'une dame
     * @return
     */
    private static Integer getManPointsFromWomanPoints(Integer womanPoints){
        // classement dames divisé par 2 puis ajout de 10 pts. On arrondit par le bas (dans mon cas 35 pts soit 17.5 + 10 = 27.5 => 25pts)
        return (((womanPoints/2)+10)/5)*5;
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
