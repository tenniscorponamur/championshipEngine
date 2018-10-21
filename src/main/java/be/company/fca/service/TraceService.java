package be.company.fca.service;

import be.company.fca.model.Trace;

public interface TraceService {

    /**
     * Permet d'ajouter une trace dans le systeme
     * @param username
     * @param type
     * @param foreignKey
     * @param message
     * @return
     */
    public Trace addTrace(String username, String type, String foreignKey, String message);
}
