package eu.h2020.symbiote.administration.services.baas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class BaasClient {

    private RestTemplate restTemplate;

    @Autowired
    public BaasClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public ResponseEntity<?> makeBaasHttpRequest(String baasBaseUrl, String baasPrefix, HttpMethod method, Object body, HttpHeaders headers, MultiValueMap<String, String> parameters, Class responseClass){
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baasBaseUrl + "/" + baasPrefix).queryParams(parameters);
        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        ResponseEntity<?> response = restTemplate.exchange(
                builder.toUriString(),
                method,
                entity,
                responseClass);
        return response;
    }
}
