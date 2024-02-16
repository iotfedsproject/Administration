package eu.h2020.symbiote.administration.converters;

import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.IoTFedsRule;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;
import eu.h2020.symbiote.model.mim.Comparator;
import eu.h2020.symbiote.model.mim.QoSConstraint;
import eu.h2020.symbiote.model.mim.QoSMetric;

import java.util.ArrayList;
import java.util.List;

public class SmartContractToConstraintsConverter {

    public static List<QoSConstraint> convertSmartContractToConstrains(SmartContract smartContract){
        List<QoSConstraint> slaConstraints = new ArrayList<>();

        QoSConstraint qoSConstraint = new QoSConstraint();
        qoSConstraint.setMetric(QoSMetric.load);
        qoSConstraint.setComparator(Comparator.greaterThanOrEqual);
        qoSConstraint.setDuration(1000);
        qoSConstraint.setThreshold(new Double(smartContract.getIoTFedsRule().getQualityMetric().getQoSPercentage()));
        qoSConstraint.setResourceType("public");


        slaConstraints.add(qoSConstraint);

        return slaConstraints;
    }
}
