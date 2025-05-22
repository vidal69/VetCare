package models;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendPatient {
    private String doctorID;
    private String patientID;
    private LocalDate serveDate;
    private LocalTime serveTime;
    private String serviceType;

    public AttendPatient(String doctorID, String patientID, LocalDate serveDate,
                         LocalTime serveTime, String serviceType) {
        this.doctorID = doctorID;
        this.patientID = patientID;
        this.serveDate = serveDate;
        this.serveTime = serveTime;
        this.serviceType = serviceType;
    }

    public String getDoctorID() {
        return doctorID;
    }

    public void setDoctorID(String doctorID) {
        this.doctorID = doctorID;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public LocalDate getServeDate() {
        return serveDate;
    }

    public void setServeDate(LocalDate serveDate) {
        this.serveDate = serveDate;
    }

    public LocalTime getServeTime() {
        return serveTime;
    }

    public void setServeTime(LocalTime serveTime) {
        this.serveTime = serveTime;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public String toString() {
        return "AttendPatient{" +
                "doctorID='" + doctorID + '\'' +
                ", patientID='" + patientID + '\'' +
                ", serveDate=" + serveDate +
                ", serveTime=" + serveTime +
                ", serviceType='" + serviceType + '\'' +
                '}';
    }
}
