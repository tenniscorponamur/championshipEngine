package be.company.fca.model;

import java.util.Date;

public class Rencontre {

    private Division division;

    // La poule doit etre une poule de la division mais elle peut etre nulle quand il s'agit d'une rencontre interserie
    private Poule poule;

    private Equipe equipeVisites;
    private Equipe equipeVisiteurs;

    private Terrain terrain;
    private Date dateRencontre;

    //TODO : liste des matchs - quid stockage resultat rencontre ?? Redondance pour simplifier le calcul des points --> gerer au niveau transactionnel lors
    // de l'enregistrement du resultat d'un des matchs de la rencontre ??? methode pour connaitre l'equipe gagnante ?? --> structure differente ??

    private Integer pointsVisites;
    private Integer pointsVisiteurs;
    private Integer setsVisites;
    private Integer setsVisiteurs;

}
