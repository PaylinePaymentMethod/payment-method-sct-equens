package com.payline.payment.sctequens.bean.business.reachdirectory;

import com.google.gson.annotations.SerializedName;

public class Detail {

    @SerializedName("Api")
    private String api;

    @SerializedName("FieldName")
    private String fieldName;

    @SerializedName("Type")
    private String type;

    @SerializedName("Value")
    private String value;

    @SerializedName("ProtocolVersion")
    private String protocolVersion;

    public Detail() {
    }

    public Detail(String api, String fieldName,String type, String value, String protocolVersion) {
        this.api = api;
        this.fieldName = fieldName;
        this.value = value;
        this.protocolVersion = protocolVersion;
    }

    public String getApi() {
        return api;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getValue() {
        return value;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }
}
