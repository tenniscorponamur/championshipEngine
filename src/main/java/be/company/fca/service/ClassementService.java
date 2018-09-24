package be.company.fca.service;

import be.company.fca.model.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

public interface ClassementService {

    public Classement getClassementPoule(Poule poule);

    public List<Classement> getClassementsByChampionnat(Long championnatId);

    public List<ClassementClub> getClassementsClubByChampionnat(Long championnatId);

    public Equipe getGagnantRencontreInterserie(Rencontre rencontre);
}
