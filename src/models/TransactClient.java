package models;

import java.time.LocalDate;
import java.time.LocalTime;

public class TransactClient {
    private String doctorID;
    private String clientID;
    private String totalBills;
    private String receipt;
    private LocalDate transactionDate;
    private LocalTime transactionTime;

    public TransactClient(String doctorID, String clientID, String totalBills,
                          String receipt, LocalDate transactionDate, LocalTime transactionTime) {
        this.doctorID = doctorID;
        this.clientID = clientID;
        this.totalBills = totalBills;
        this.receipt = receipt;
        this.transactionDate = transactionDate;
        this.transactionTime = transactionTime;
    }

    public String getDoctorID() {
        return doctorID;
    }

    public void setDoctorID(String doctorID) {
        this.doctorID = doctorID;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getTotalBills() {
        return totalBills;
    }

    public void setTotalBills(String totalBills) {
        this.totalBills = totalBills;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    @Override
    public String toString() {
        return "TransactClient{" +
                "doctorID='" + doctorID + '\'' +
                ", clientID='" + clientID + '\'' +
                ", totalBills='" + totalBills + '\'' +
                ", receipt='" + receipt + '\'' +
                ", transactionDate=" + transactionDate +
                ", transactionTime=" + transactionTime +
                '}';
    }
}
