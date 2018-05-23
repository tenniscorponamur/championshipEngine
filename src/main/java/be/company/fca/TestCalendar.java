package be.company.fca;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class TestCalendar {

    private List<Team> teams = new ArrayList<>();
    private List<Rencontre> rencontres = new ArrayList<>();

    public static void main(String[] args){

        TestCalendar testCalendar = new TestCalendar();
        testCalendar.generateTeams(5);
        testCalendar.displayTeams();

        testCalendar.generateCalendar();
    }

    private void generateCalendar(){

        if (teams.size()<2){
            throw new RuntimeException("Pas suffisamment d'equipes pour definir un calendrier");
        }

        rencontres.clear();

        int nbJournees = getNbJournees();
        int nbRencontresParJournee = teams.size() / 2;
        System.err.println("Nb rencontres par journee : " + nbRencontresParJournee);
        System.err.println("Nombre de matchs à jouer :" + nbJournees*nbRencontresParJournee);

        // Decoupe en journees

        for (int i=0;i<nbJournees;i++){

            // S'il s'agit d'un nombre pair d'equipes, on va boucler sur toutes les equipes sauf la premiere
            List<Team> equipes = new ArrayList<>(teams.subList(1,teams.size()));

            for (int j=0;j<nbRencontresParJournee;j++){

                Rencontre rencontre = new Rencontre();
                rencontre.numeroJournee = i;

                // Pour un nombre d'equipes impair, permutation circulaire parmi toutes les equipes --> l'une sera bye via la procedure
                if (nombreEquipesImpair()){
                    rencontre.visites = teams.get((0+j+i)%teams.size());
                    rencontre.visiteurs = teams.get( (teams.size() - 1 - j + i)%teams.size() );
                }else{
                    // Pour un nombre d'equipes pair,
                    // On va garder la premiere equipe fixe et faire tourner les autres
                    if (j==0){
                        rencontre.visites = teams.get(0);
                        rencontre.visiteurs = equipes.get( (equipes.size() - 1 - j + i) % equipes.size() );
                    }else{
                        rencontre.visites = equipes.get( (equipes.size()-1 + i + j) % equipes.size());
                        rencontre.visiteurs = equipes.get( (equipes.size() - 1 - j + i) % equipes.size() );
                    }
                }
                System.err.println(rencontre);
                rencontres.add(rencontre);

            }

        }

        // TODO : Aller-retour == dupliquer les rencontres en inversant visites-visiteurs

    }

    // Pour les nombres d'equipes impair

    // Boucle sur les journees
    // Journee 1 :

    // R1A = 0
    // R1B = (nbTeams - 1)
    // R2A = 1
    // R2B = (nbTeams - 1) - 1

    // Journee 2 : decalage de 1

    // R1A = 1 == 0 + 1
    // R1B = 0 == (nbTeams - 1)+1 (modulo) nbTeams
    // R2A = 2 == 1 + 1
    // R2B = (nbTeams - 1) - 1 + 1


    // Pour les nombres d'equipes pair

    // Boucle sur les journees mais on restreint les equipes a une de moins pour boucler
    // Journee 1 :

    // R1A = FIXE
    // R1B = 2 (nbTeamsReduit - 1)
    // R2A = 0
    // R2B = 1 (nbTeamsReduit - 1) - 1

    // Journee 2 : decalage de 1

    // R1A = FIXE
    // R1B = 0 == (nbTeamsReduit - 1) + 1 (modulo) nbTeamsReduit
    // R2A = 1 == 0 + 1
    // R2B = 2 == (nbTeamsReduit - 1) - 1 + 1

    // Journee 3 : decalage de 2 sauf pour R1A

    // R1A = FIXE
    // R1B = 1 == (nbTeamsReduit - 1) + 2 (modulo) nbTeamsReduit
    // R2A = 2 == 0 + 2
    // R2B = 0 ==  (nbTeamsReduit - 1) - 1 + 2 (modulo) nbTeamsReduit

    /*

    Permutation circulaire

        r1(a) = 0   r1(a) = 2   r1(a) = 4
        r1(b) = 1   r1(b) = 0   r1(b) = 2
        r2(a) = 2   r2(a) = 4   r2(a) = 3
        r2(b) = 3   r2(b) = 1   r2(b) = 0
        r3(-) = 4   r3(-) = 3   r3(-) = 1


        A   E
        B   D
          C

        A - B   0 - 1
        C - D   2 - 3
          E       4

        C - A   2 - 0
        E - B   4 - 1
          D       3

        E - C   4 - 2
        D - A   3 - 0
          B       1

        D - E
        B - C
          A

        B - D
        A - E
          C


     */

    private boolean rencontreAlreadyExists(Team visites, Team visiteurs){
        for (Rencontre rencontre : rencontres){
            if (rencontre.visites.equals(visites) && rencontre.visiteurs.equals(visiteurs)){
                return true;
            }
        }
        return false;
    }

    private int getNbJournees(){
        if (nombreEquipesImpair()){
            return this.teams.size();
        }else{
            return this.teams.size()-1;
        }
    }

    private boolean nombreEquipesImpair(){
        return this.teams.size()%2!=0;
    }

    private String getCharForNumber(int i) {
        return i > 0 && i < 27 ? String.valueOf((char)(i + 64)) : null;
    }

    private void generateTeams(int nbTeams){
        teams.clear();
        for (int i=0;i<nbTeams;i++){
            teams.add(new Team("Equipe " + getCharForNumber(i+1)));
        }
    }

    private void displayTeams(){
        for (Team team : teams){
            System.err.println(team);
        }
    }

    private class Team {
        private String name;

        public Team(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private class Rencontre {
        private int numeroJournee;
        private Team visites;
        private Team visiteurs;

        @Override
        public String toString() {
            return "Journée " + numeroJournee + " : " + visites + " / " + visiteurs;
        }
    }
}
