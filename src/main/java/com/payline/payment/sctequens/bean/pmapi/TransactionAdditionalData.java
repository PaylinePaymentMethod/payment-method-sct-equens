package com.payline.payment.sctequens.bean.pmapi;

public class TransactionAdditionalData {

    private String aspspPaymentId;

    public TransactionAdditionalData(String aspspPaymentId) {
        this.aspspPaymentId = aspspPaymentId;
    }

    public String getAspspPaymentId() {
        return aspspPaymentId;
    }

}
