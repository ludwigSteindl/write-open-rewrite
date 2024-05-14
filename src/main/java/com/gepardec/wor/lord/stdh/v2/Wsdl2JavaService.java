package com.gepardec.wor.lord.stdh.v2;

import java.util.List;

public class Wsdl2JavaService {
    private String serviceAlias;

    private List<String> requestTypes;

    private List<String> responseTypes;

    public Wsdl2JavaService(String serviceAlias, List<String> requestTypes, List<String> responseTypes) {
        this.serviceAlias = serviceAlias;
        this.requestTypes = requestTypes;
        this.responseTypes = responseTypes;
    }

    public String getServiceAlias() {
        return serviceAlias;
    }

    public List<String> getRequestTypes() {
        return requestTypes;
    }

    public List<String> getResponseTypes() {
        return responseTypes;
    }
}
