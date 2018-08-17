package be.company.fca.service;

import be.company.fca.model.ClassementAFT;

import java.util.List;

public interface ClassementAFTService {

    public ClassementAFT saveClassementsAFT(Long membreId, List<ClassementAFT> classementAFTList);
}
