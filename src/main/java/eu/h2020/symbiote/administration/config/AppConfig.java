package eu.h2020.symbiote.administration.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import eu.h2020.symbiote.administration.model.ServerInformation;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;

@Configuration
public class AppConfig extends AbstractMongoConfiguration {

    @Value("${http.proxy.host}")
    private String proxyHost;

    @Value("${http.proxy.port}")
    private Integer proxyPort;

    @Value("${http.proxy.enabled}")
    private boolean proxyEnable;

    @Value("${symbiote.core.administration.databaseHostname}")
    private String databaseHostname;

    @Value("${symbiote.core.administration.database}")
    private String databaseName;

    @Value("${symbiote.core.administration.serverInformation.name}")
    private String serverInfoName;

    @Value("${symbiote.core.administration.serverInformation.dataProtectionOrganization}")
    private String dataProtectionOrganization;

    @Value("${symbiote.core.administration.serverInformation.address}")
    private String address;

    @Value("${symbiote.core.administration.serverInformation.country}")
    private String country;

    @Value("${symbiote.core.administration.serverInformation.phoneNumber}")
    private String phoneNumber;

    @Value("${symbiote.core.administration.serverInformation.email}")
    private String email;

    @Value("${symbiote.core.administration.serverInformation.website}")
    private String website;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    public Mongo mongo() {
        return new MongoClient(this.databaseHostname);
    }

    @Override
    protected Collection<String> getMappingBasePackages() { return Collections.singletonList("com.oreilly.springdata.mongodb"); }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // The basename should be "classpath:/{path}/messages"
        messageSource.setBasename("classpath:/messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(0);
        return messageSource;
    }

    @Bean
    public AlwaysSampler defaultSampler() {
        return new AlwaysSampler();
    }

//    @Bean
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster
                = new SimpleApplicationEventMulticaster();

        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return eventMulticaster;
    }

    @Bean
    public ServerInformation serverInformation() {
        return new ServerInformation(
                serverInfoName,
                dataProtectionOrganization,
                address,
                country,
                phoneNumber,
                email,
                website
        );
    }

    @Bean
    public RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {


        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        HttpClientBuilder builder = HttpClients
                .custom()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setSSLContext(SSLContexts
                        .custom()
                        .loadTrustMaterial((TrustStrategy) (arg0, arg1) -> true)
                        .build());

        if (proxyEnable) {
            builder.setProxy(new HttpHost(proxyHost, proxyPort, "http"));
        }

        requestFactory.setHttpClient(builder.build());

        RestTemplate restTemplate = new RestTemplate(requestFactory);

        return restTemplate;
    }
}