package be.company.fca.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InfosCalculClassement {

    private Long membreId;
    private Date startDate;
    private Date endDate;
    private Integer pointsDepart;
    private Integer pointsFin;
    private List<CaracteristiquesMatch> caracteristiquesMatchList = new ArrayList<>();

    public Long getMembreId() {
        return membreId;
    }

    public void setMembreId(Long membreId) {
        this.membreId = membreId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getPointsDepart() {
        return pointsDepart;
    }

    public void setPointsDepart(Integer pointsDepart) {
        this.pointsDepart = pointsDepart;
    }

    public Integer getPointsFin() {
        return pointsFin;
    }

    public void setPointsFin(Integer pointsFin) {
        this.pointsFin = pointsFin;
    }

    public List<CaracteristiquesMatch> getCaracteristiquesMatchList() {
        return caracteristiquesMatchList;
    }

    public void setCaracteristiquesMatchList(List<CaracteristiquesMatch> caracteristiquesMatchList) {
        this.caracteristiquesMatchList = caracteristiquesMatchList;
    }
}
