package be.company.fca.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InfosCalculClassementDto {

    private Long membreId;
    private Date startDate;
    private Date endDate;
    private Integer totalObtenu;
    private Integer pointsDepart;
    private Integer pointsFin;
    private List<CaracteristiquesMatchDto> caracteristiquesMatchList = new ArrayList<>();

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

    public Integer getTotalObtenu() {
        return totalObtenu;
    }

    public void setTotalObtenu(Integer totalObtenu) {
        this.totalObtenu = totalObtenu;
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

    public List<CaracteristiquesMatchDto> getCaracteristiquesMatchList() {
        return caracteristiquesMatchList;
    }

    public void setCaracteristiquesMatchList(List<CaracteristiquesMatchDto> caracteristiquesMatchList) {
        this.caracteristiquesMatchList = caracteristiquesMatchList;
    }
}
