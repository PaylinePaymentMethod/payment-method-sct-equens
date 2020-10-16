package com.payline.payment.sctequens.bean.business.psu;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.sctequens.bean.business.EquensApiMessage;

public class PsuCreateResponse extends EquensApiMessage {

    @SerializedName("Psu")
    private Psu psu;

    PsuCreateResponse(EquensApiMessageBuilder builder) {
        // default constructor, won't be used.
        super(builder);
    }

    public Psu getPsu() {
        return psu;
    }

}
