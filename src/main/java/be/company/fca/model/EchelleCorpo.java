package be.company.fca.model;

import java.util.ArrayList;
import java.util.List;

public class EchelleCorpo {

    public static List<EchelleCorpo> getAllEchellesCorpo(){
        List<EchelleCorpo> echellesCorpo = new ArrayList<>();
        for (int i=0;i<22;i++){
            echellesCorpo.add(new EchelleCorpo(i*5+5));
        }

        return echellesCorpo;
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
