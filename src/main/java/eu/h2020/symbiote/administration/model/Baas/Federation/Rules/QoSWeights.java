package eu.h2020.symbiote.administration.model.Baas.Federation.Rules;

public class QoSWeights{
    public double availability;
    public double precision;
    public double response_time;

    public double getAvailability() {
        return availability;
    }

    public void setAvailability(double availability) {
        this.availability = availability;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getResponse_time() {
        return response_time;
    }

    public void setResponse_time(double response_time) {
        this.response_time = response_time;
    }

    public QoSWeights(double availability, double precision, double response_time) {
        this.availability = availability;
        this.precision = precision;
        this.response_time = response_time;
    }

    public QoSWeights() {
    }
}
