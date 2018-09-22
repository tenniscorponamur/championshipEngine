package be.company.fca.service;

import be.company.fca.model.Classement;
import be.company.fca.model.ClassementClub;
import be.company.fca.model.Poule;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

public interface ClassementService {

    public Classement getClassementPoule(Poule poule);

    public List<Classement> getClassementsByChampionnat(Long championnatId);

    public List<ClassementClub> getClassementsClubByChampionnat(Long championnatId);
}
