package eu.h2020.symbiote.administration.services.baas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;

@Component
public class BaasClient {

    private static Log log = LogFactory.getLog(BaasClient.class);

    @Value("${http.proxy.host}")
    private String proxyHost;

    @Value("${http.proxy.port}")
    private Integer proxyPort;

    @Value("${http.proxy.enabled}")
    private boolean proxyEnable;

    @Autowired
    private ObjectMapper objectMapper;

    public ResponseEntity<String> makeBaasHttpRequest(String baasBaseUrl, String baasPrefix, HttpMethod method, HashMap<String, String> body, MultiValueMap<String, String> parameters) {

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        HttpClientBuilder builder = null;
        try {
            builder = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).setSSLContext(SSLContexts.custom().loadTrustMaterial((TrustStrategy) (arg0, arg1) -> true).build());
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }

        if (proxyEnable) {
            builder.setProxy(new HttpHost(proxyHost, proxyPort, "http"));
        }

        requestFactory.setHttpClient(builder.build());
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(baasBaseUrl + "/" + baasPrefix).queryParams(parameters);
        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        log.debug("Sending request to: " + uriComponentsBuilder.toUriString() + " using: " + method);
        log.debug("With body: " + entity.getBody());

        try {
            return restTemplate.exchange(uriComponentsBuilder.toUriString(), method, entity, String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders()).body(e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<String> makeBaasHttpRequest2(String baasBaseUrl, String baasPrefix, HttpMethod method, String body, MultiValueMap<String, String> parameters) {

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        HttpClientBuilder builder = null;
        try {
            builder = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).setSSLContext(SSLContexts.custom().loadTrustMaterial((TrustStrategy) (arg0, arg1) -> true).build());
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }

        if (proxyEnable) {
            builder.setProxy(new HttpHost(proxyHost, proxyPort, "http"));
        }

        requestFactory.setHttpClient(builder.build());
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(baasBaseUrl + "/" + baasPrefix).queryParams(parameters);
        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        log.debug("Sending request to: " + uriComponentsBuilder.toUriString() + " using: " + method);
        log.debug("With body: " + entity.getBody());

        try {
            return restTemplate.exchange(uriComponentsBuilder.toUriString(), method, entity, String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders()).body(e.getResponseBodyAsString());
        }
    }

}
