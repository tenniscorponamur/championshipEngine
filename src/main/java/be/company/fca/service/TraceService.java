package be.company.fca.service;

import be.company.fca.model.Trace;
import org.springframework.security.core.Authentication;

public interface TraceService {

    /**
     * Permet d'ajouter une trace dans le systeme
     * @param authentication
     * @param type
     * @param foreignKey
     * @param message
     * @return
     */
    public Trace addTrace(Authentication authentication, String type, String foreignKey, String message);
}
