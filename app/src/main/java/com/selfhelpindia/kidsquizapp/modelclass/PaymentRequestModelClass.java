package com.selfhelpindia.kidsquizapp.modelclass;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class PaymentRequestModelClass {
    private String uId;
    private String paytmNumber;
    private Double amount;
    private int status = 0;

    @ServerTimestamp
    private Date createdAt;

    public PaymentRequestModelClass() {

    }

    public PaymentRequestModelClass(String uId, String paytmNumber, Double amount) {
        this.uId = uId;
        this.paytmNumber = paytmNumber;
        this.amount = amount;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getPaytmNumber() {
        return paytmNumber;
    }

    public void setPaytmNumber(String paytmNumber) {
        this.paytmNumber = paytmNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
