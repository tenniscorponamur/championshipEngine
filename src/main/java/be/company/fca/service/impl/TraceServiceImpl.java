package be.company.fca.service.impl;

import be.company.fca.model.Trace;
import be.company.fca.model.User;
import be.company.fca.repository.TraceRepository;
import be.company.fca.repository.UserRepository;
import be.company.fca.service.TraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional(readOnly = true)
public class TraceServiceImpl implements TraceService {

    @Autowired
    TraceRepository traceRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional(readOnly = false)
    public Trace addTrace(String username, String type, String foreignKey, String message) {
        Trace trace = new Trace();
        trace.setDateHeure(new Date());
        trace.setType(type);
        trace.setForeignKey(foreignKey);
        User user = userRepository.findByUsername(username);
        trace.setUtilisateur(user.getPrenom() + " " + user.getNom());
        trace.setMessage(message);
        return traceRepository.save(trace);
    }
}
