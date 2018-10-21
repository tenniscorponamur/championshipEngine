package be.company.fca.controller;


import be.company.fca.model.Trace;
import be.company.fca.model.User;
import be.company.fca.repository.TraceRepository;
import be.company.fca.repository.UserRepository;
import be.company.fca.service.TraceService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des traces")
public class TraceController {

    @Autowired
    TraceService traceService;

    @Autowired
    TraceRepository traceRepository;

    @RequestMapping(value = "/public/traces", method = RequestMethod.GET)
    public List<Trace> getTraces(Authentication authentication, @RequestParam String type, @RequestParam String foreignKey){
        //TODO : getByTypeAndForeignKey + order by date desc
        return (List<Trace>) traceRepository.findAll();
    }

    @RequestMapping(value = "/private/trace", method = RequestMethod.POST)
    public Trace addTrace(Authentication authentication, @RequestParam String type, @RequestParam String foreignKey, @RequestBody String message){
        return traceService.addTrace(authentication.getName(),type,foreignKey,message);
    }


}
