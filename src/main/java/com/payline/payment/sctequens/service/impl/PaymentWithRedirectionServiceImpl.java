package com.payline.payment.sctequens.service.impl;

import com.payline.payment.sctequens.bean.configuration.RequestConfiguration;
import com.payline.payment.sctequens.exception.InvalidDataException;
import com.payline.payment.sctequens.exception.PluginException;
import com.payline.payment.sctequens.utils.constant.RequestContextKeys;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.service.PaymentWithRedirectionService;

public class PaymentWithRedirectionServiceImpl extends com.payline.payment.sctequens.service.impl.AbstractRedirectionServiceImpl implements PaymentWithRedirectionService {
    
    @Override
    public PaymentResponse finalizeRedirectionPayment(RedirectionPaymentRequest redirectionPaymentRequest) {
        PaymentResponse paymentResponse;

        try {
            // Retrieve payment ID from request context
            if( redirectionPaymentRequest.getRequestContext() == null
                    || redirectionPaymentRequest.getRequestContext().getRequestData() == null
                    || redirectionPaymentRequest.getRequestContext().getRequestData().get(RequestContextKeys.PAYMENT_ID) == null ){
                throw new InvalidDataException("Missing payment ID from request context");
            }
            String paymentId = redirectionPaymentRequest.getRequestContext().getRequestData().get(RequestContextKeys.PAYMENT_ID);

            // check and update payment status
            paymentResponse = this.updatePaymentStatus( paymentId, RequestConfiguration.build( redirectionPaymentRequest ) );
        }
        catch( PluginException e ){
            paymentResponse = e.toPaymentResponseFailureBuilder().build();
        }

        return paymentResponse;
    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest transactionStatusRequest) {
        PaymentResponse paymentResponse;

        try {
            paymentResponse = this.updatePaymentStatus( transactionStatusRequest.getTransactionId(), RequestConfiguration.build( transactionStatusRequest ) );
        }
        catch( PluginException e ){
            paymentResponse = e.toPaymentResponseFailureBuilder().build();
        }

        return paymentResponse;
    }
}
