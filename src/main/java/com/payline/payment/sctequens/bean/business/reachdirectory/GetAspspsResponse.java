package com.payline.payment.sctequens.bean.business.reachdirectory;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.sctequens.bean.business.EquensApiMessage;

import java.util.List;

public class GetAspspsResponse extends EquensApiMessage {

    @SerializedName("ASPSP")
    private List<Aspsp> aspsps;

    GetAspspsResponse(EquensApiMessageBuilder builder) {
        super(builder);
    }

    public List<Aspsp> getAspsps() {
        return aspsps;
    }
}
