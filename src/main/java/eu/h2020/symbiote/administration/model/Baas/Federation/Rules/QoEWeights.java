package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

public class QoEWeights{

    public double business_enablement;
    public double completeness;
    public double correctness;
    public double ease_of_use;
    public double precision;
    public double relevance;
    public double response_time;
    public double value_for_money;

    public double getBusiness_enablement() {
        return business_enablement;
    }

    public void setBusiness_enablement(double business_enablement) {
        this.business_enablement = business_enablement;
    }

    public double getCompleteness() {
        return completeness;
    }

    public void setCompleteness(double completeness) {
        this.completeness = completeness;
    }

    public double getCorrectness() {
        return correctness;
    }

    public void setCorrectness(double correctness) {
        this.correctness = correctness;
    }

    public double getEase_of_use() {
        return ease_of_use;
    }

    public void setEase_of_use(double ease_of_use) {
        this.ease_of_use = ease_of_use;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRelevance() {
        return relevance;
    }

    public void setRelevance(double relevance) {
        this.relevance = relevance;
    }

    public double getResponse_time() {
        return response_time;
    }

    public void setResponse_time(double response_time) {
        this.response_time = response_time;
    }

    public double getValue_for_money() {
        return value_for_money;
    }

    public void setValue_for_money(double value_for_money) {
        this.value_for_money = value_for_money;
    }

    public QoEWeights(double business_enablement, double completeness, double correctness, double ease_of_use, double precision, double relevance, double response_time, double value_for_money) {
        this.business_enablement = business_enablement;
        this.completeness = completeness;
        this.correctness = correctness;
        this.ease_of_use = ease_of_use;
        this.precision = precision;
        this.relevance = relevance;
        this.response_time = response_time;
        this.value_for_money = value_for_money;
    }

    public QoEWeights() {
    }
}
