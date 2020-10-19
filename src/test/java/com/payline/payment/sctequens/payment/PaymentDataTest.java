package com.payline.payment.sctequens.payment;

import com.payline.payment.sctequens.MockUtils;
import com.payline.payment.sctequens.bean.business.payment.PaymentData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PaymentDataTest {

    public PaymentData create() {
        return MockUtils.aPaymentData();
    }

    @Test
    void getBic() {
        Assertions.assertEquals("PSSTFRPP", create().getBic());
    }

    @Test
    void getIban() {
        Assertions.assertEquals("anIbanWithMoreThan8Charactere", create().getIban());
    }

}