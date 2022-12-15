package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SmartContract {

    @JsonProperty("IoTFedsRules")
    private IoTFedsRule ioTFedsRule;

    public IoTFedsRule getIoTFedsRule() {
        return ioTFedsRule;
    }

    public void setIoTFedsRule(IoTFedsRule ioTFedsRule) {
        this.ioTFedsRule = ioTFedsRule;
    }

    public SmartContract(IoTFedsRule ioTFedsRule) {
        this.ioTFedsRule = ioTFedsRule;
    }

    public SmartContract() {
    }
}
