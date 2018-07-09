package be.company.fca.model;

import java.util.Date;

public class ClassementAFT {

    // Liste des codes AFT et points associes disponibles en //
    // afin de pouvoir gerer une adaptation du reglement sans impacter les anciens codes

    private String codeClassement;
    private Integer points;
    private Date date;
    private Long membreId; // Pas de chargement de l'arbre relatif au membre pour les classements

}
