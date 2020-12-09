package com.payline.payment.sctequens.service.impl;

import com.payline.payment.sctequens.bean.business.reachdirectory.Aspsp;
import com.payline.payment.sctequens.bean.business.reachdirectory.Detail;
import com.payline.payment.sctequens.bean.business.reachdirectory.GetAspspsResponse;
import com.payline.payment.sctequens.exception.InvalidDataException;
import com.payline.payment.sctequens.exception.PluginException;
import com.payline.payment.sctequens.service.JsonService;
import com.payline.payment.sctequens.service.LogoPaymentFormConfigurationService;
import com.payline.payment.sctequens.utils.Constants;
import com.payline.payment.sctequens.utils.PluginUtils;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.paymentform.bean.field.SelectOption;
import com.payline.pmapi.bean.paymentform.bean.form.BankTransferForm;
import com.payline.pmapi.bean.paymentform.bean.form.CustomForm;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseFailure;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PaymentFormConfigurationServiceImpl extends LogoPaymentFormConfigurationService {

    private static final Logger LOGGER = LogManager.getLogger(PaymentFormConfigurationServiceImpl.class);

    JsonService jsonService = JsonService.getInstance();

    @Override
    public PaymentFormConfigurationResponse getPaymentFormConfiguration(PaymentFormConfigurationRequest paymentFormConfigurationRequest) {
        PaymentFormConfigurationResponse pfcResponse;
        List<String> listCountryCode;

        try {
            Locale locale = paymentFormConfigurationRequest.getLocale();

            // build the banks list from the plugin configuration
            if (paymentFormConfigurationRequest.getPluginConfiguration() == null) {
                throw new InvalidDataException("Plugin configuration must not be null");
            }
            // check if the string who contain the list of country is empty
            String countries = paymentFormConfigurationRequest.getContractConfiguration().getProperty(Constants.ContractConfigurationKeys.COUNTRIES).getValue();
            if (PluginUtils.isEmpty(countries)) {
                throw new InvalidDataException("country must not be empty");
            }

            listCountryCode = PluginUtils.createListCountry(countries);

            List<SelectOption> banks = this.getBanks(paymentFormConfigurationRequest.getPluginConfiguration(), listCountryCode);

            // Build the payment form
            CustomForm form = BankTransferForm.builder()
                    .withBanks(banks)
                    .withDescription(i18n.getMessage("paymentForm.description", locale))
                    .withDisplayButton(true)
                    .withButtonText(i18n.getMessage("paymentForm.buttonText", locale))
                    .withCustomFields(new ArrayList<>())
                    .build();

            pfcResponse = PaymentFormConfigurationResponseSpecific.PaymentFormConfigurationResponseSpecificBuilder
                    .aPaymentFormConfigurationResponseSpecific()
                    .withPaymentForm(form)
                    .build();
        } catch (PluginException e) {
            pfcResponse = e.toPaymentFormConfigurationResponseFailureBuilder().build();
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected plugin error", e);
            pfcResponse = PaymentFormConfigurationResponseFailure.PaymentFormConfigurationResponseFailureBuilder
                    .aPaymentFormConfigurationResponseFailure()
                    .withErrorCode(PluginException.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();
        }

        return pfcResponse;
    }

    /**
     * This method parses the PluginConfiguration string to read the list of ASPSPs and convert it to a list of choices
     * for a select list. The key of each option is the AspspId and the value is "BIC - name".
     * PAYLAPMEXT-204: if BIC is null, the selection option's value will just be the name of the bank.
     * PAYLAPMEXT-203: filter the list using the countryCode (if provided) to keep only the banks which country code matches.
     *
     * @param pluginConfiguration The PluginConfiguration string
     * @param listCountryCode     List of 2-letters country code
     * @return The list of banks, as select options.
     */
    List<SelectOption> getBanks(String pluginConfiguration, List<String> listCountryCode) {
        final List<SelectOption> options = new ArrayList<>();
        if (pluginConfiguration == null) {
            LOGGER.warn("pluginConfiguration is null");
        } else {
            List<Aspsp> aspsps = jsonService.fromJson(PluginUtils.extractBanks(pluginConfiguration), GetAspspsResponse.class).getAspsps();
            List<Aspsp> validAspsps = filter(aspsps, listCountryCode);

            for (Aspsp aspsp : validAspsps) {
                options.add(createAspspOption(aspsp));
            }
        }
        return options;
    }

    private List<Aspsp> filter(List<Aspsp> aspsps, List<String> listCountryCode) {
        return aspsps.stream()
                .filter(e -> e.getCountryCode() != null)
                .filter(e -> listCountryCode.isEmpty() || listCountryCode.contains(e.getCountryCode()))
                .filter(e -> !PluginUtils.isEmpty(e.getBic()))
                .filter(e -> isCompatible(e.getDetails()))
                .collect(Collectors.toList());
    }

    private SelectOption createAspspOption(Aspsp aspsp) {
        // add the aspsp name if exists
        String valuesBuilder = "";
        if (aspsp.getName() != null && !aspsp.getName().isEmpty()) {
            valuesBuilder = aspsp.getName().get(0);
        }
        return SelectOption.SelectOptionBuilder.aSelectOption()
                .withKey(aspsp.getBic())
                .withValue(valuesBuilder)
                .build();
    }


    /**
     * Check if a bank is compatible with the Normal paymentMode
     * see PAYLAPMEXT-294
     *
     * @param details
     * @return true if compatible or no info
     */
    public boolean isCompatible(List<Detail> details) {
        boolean isCompatible = true;

        if(details != null){
            for (Detail detail : details) {
                if ( !PluginUtils.isEmpty(detail.getValue()) && !detail.getValue().contains("Normal")) {
                    isCompatible = false;
                    break;
                }
            }
        }
        return isCompatible;
    }
}

