package be.company.fca.model;

import java.util.ArrayList;
import java.util.List;

public class Classement {

    private Poule poule;

    private List<ClassementEquipe> classementEquipes=new ArrayList<>();

    public Poule getPoule() {
        return poule;
    }

    public void setPoule(Poule poule) {
        this.poule = poule;
    }

    public List<ClassementEquipe> getClassementEquipes() {
        return classementEquipes;
    }

    public void setClassementEquipes(List<ClassementEquipe> classementEquipes) {
        this.classementEquipes = classementEquipes;
    }

    /**
     * Permet de retrouver le classement d'une equipe dans la liste
     * @param equipe
     * @return
     */
    public ClassementEquipe findByEquipe(Equipe equipe){
        for (ClassementEquipe classementEquipe : classementEquipes){
            if (classementEquipe.getEquipe().equals(equipe)){
                return classementEquipe;
            }
        }
        return null;
    }

}
