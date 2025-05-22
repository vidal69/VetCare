package models;

import java.time.LocalDate;

public class Patient {
    private String patientID;
    private String name;
    private LocalDate dateOfBirth;
    private String gender;
    private String species;
    private String breed;
    private String remarks;
    private String clientID;

    public Patient(String patientID, String name, LocalDate dateOfBirth, String gender,
                   String species, String breed, String remarks, String clientID) {
        this.patientID = patientID;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.species = species;
        this.breed = breed;
        this.remarks = remarks;
        this.clientID = clientID;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientID='" + patientID + '\'' +
                ", name='" + name + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender='" + gender + '\'' +
                ", species='" + species + '\'' +
                ", breed='" + breed + '\'' +
                ", remarks='" + remarks + '\'' +
                ", clientID='" + clientID + '\'' +
                '}';
    }
}
