package be.company.fca.service;

import be.company.fca.model.ClassementCorpo;

import java.util.List;

public interface ClassementCorpoService {

    public ClassementCorpo saveClassementsCorpo(Long membreId, List<ClassementCorpo> classementCorpoList);

}
