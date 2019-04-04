package be.company.fca.dto;

import be.company.fca.model.AutorisationRencontre;
import be.company.fca.model.TypeAutorisation;

public class AutorisationRencontreDto {

    private Long id;
    private Long rencontreFk;
    private TypeAutorisation type;
    private MembreDto membre;

    public AutorisationRencontreDto(AutorisationRencontre autorisationRencontre) {
        this.id = autorisationRencontre.getId();
        this.type= autorisationRencontre.getType();
        this.rencontreFk = autorisationRencontre.getRencontreFk();
        this.membre = new MembreDto(autorisationRencontre.getMembre(),false,false);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRencontreFk() {
        return rencontreFk;
    }

    public void setRencontreFk(Long rencontreFk) {
        this.rencontreFk = rencontreFk;
    }

    public TypeAutorisation getType() {
        return type;
    }

    public void setType(TypeAutorisation type) {
        this.type = type;
    }

    public MembreDto getMembre() {
        return membre;
    }

    public void setMembre(MembreDto membre) {
        this.membre = membre;
    }
}
