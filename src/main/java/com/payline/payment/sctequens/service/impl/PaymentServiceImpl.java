package com.payline.payment.sctequens.service.impl;

import com.payline.payment.sctequens.bean.GenericPaymentRequest;
import com.payline.payment.sctequens.bean.business.payment.PaymentData;
import com.payline.payment.sctequens.exception.PluginException;
import com.payline.payment.sctequens.service.GenericPaymentService;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.paymentform.bean.form.BankTransferForm;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentService;
import org.apache.logging.log4j.Logger;

public class PaymentServiceImpl implements PaymentService {
    private GenericPaymentService genericPaymentService = GenericPaymentService.getInstance();
    private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);

    @Override
    public PaymentResponse paymentRequest(PaymentRequest paymentRequest) {
        try {
            GenericPaymentRequest genericPaymentRequest = new GenericPaymentRequest(paymentRequest);

            PaymentData paymentData = new PaymentData.PaymentDataBuilder()
                    .withBic(paymentRequest.getPaymentFormContext().getPaymentFormParameter().get(BankTransferForm.BANK_KEY))
                    .withIban(paymentRequest.getPaymentFormContext().getSensitivePaymentFormParameter().get(BankTransferForm.IBAN_KEY))
                    .build();

            // execute the payment Request
            return genericPaymentService.paymentRequest(genericPaymentRequest, paymentData);
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected plugin error", e);
            return PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withErrorCode(PluginException.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();
        }
    }
}
