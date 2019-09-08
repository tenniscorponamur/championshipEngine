package be.company.fca.model;

import java.util.*;

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
    public static Map<Integer,Integer> getCorrespondancePointsHommeFemme(Date date){
        Map<Integer, Integer> map=new HashMap<>();
        for (EchelleCorpo echelleCorpo : EchelleCorpo.getAllEchellesCorpo()){
            map.put(echelleCorpo.getPoints(),Math.max(5,getManPointsFromWomanPoints(echelleCorpo.points,date)));
        }
        return map;
    }

    /**
     * Permet de recuperer la valeur correspondant en points homme pour les points d'une dame
     * @return
     */
    private static Integer getManPointsFromWomanPoints(Integer womanPoints, Date date){

        // Changement de la regle de correspondance le 15 septembre 2019

        Calendar _15septembre2019 = new GregorianCalendar();
        _15septembre2019.set(Calendar.YEAR,2019);
        _15septembre2019.set(Calendar.MONTH,Calendar.SEPTEMBER);
        _15septembre2019.set(Calendar.DAY_OF_MONTH,15);
        _15septembre2019.set(Calendar.HOUR_OF_DAY,0);
        _15septembre2019.set(Calendar.MINUTE,0);
        _15septembre2019.set(Calendar.SECOND,0);
        _15septembre2019.set(Calendar.MILLISECOND,0);

        if (date!=null && date.before(_15septembre2019.getTime())){
            // classement dames divisé par 2 puis ajout de 10 pts. On arrondit par le bas (dans mon cas 35 pts soit 17.5 + 10 = 27.5 => 25pts)
            return (((womanPoints/2)+10)/5)*5;
        }else{
            // classement dames - 25 points
            return womanPoints-25;
        }

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
