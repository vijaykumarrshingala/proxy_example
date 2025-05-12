import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.security.KeyStore;
import java.util.Collections;

@SpringBootApplication
public class RestClientProxyTruststoreApplication implements CommandLineRunner {

    private final String proxyHost = "your.proxy.hostname";
    private final int proxyPort = 8080;
    private final String proxyUsername = "your_proxy_username";
    private final String proxyPassword = "your_proxy_password";
    private final String truststorePath = "classpath:your_truststore.jks";
    private final String truststorePassword = "your_truststore_password";
    private final String thirdPartyApiUrl = "https://api.example.com/some/endpoint";

    public static void main(String[] args) {
        SpringApplication.run(RestClientProxyTruststoreApplication.class, args);
    }

    @Bean
    public RestClient restClient() throws Exception {
        // 1. Configure Truststore
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (InputStream inputStream = getClass().getResourceAsStream(truststorePath)) {
            trustStore.load(inputStream, truststorePassword.toCharArray());
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        // 2. Configure Proxy with Basic Authentication
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setProxySelector(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)));

        ClientHttpRequestInterceptor basicAuthInterceptor = (request, body, execution) -> {
            String authValue = "Basic " + Base64Utils.encodeToString((proxyUsername + ":" + proxyPassword).getBytes());
            request.getHeaders().set("Proxy-Authorization", authValue);
            return execution.execute(request, body);
        };

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeaders(headers -> headers.set("Accept", "application/json"))
                .interceptors(interceptors -> interceptors.add(basicAuthInterceptor))
                .sslContext(sslContext)
                .build();
    }

    private final RestClient restClient;

    public RestClientProxyTruststoreApplication(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(URI.create(thirdPartyApiUrl))
                    .retrieve()
                    .toEntity(String.class);
            System.out.println("Response from 3rd party API: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Error calling 3rd party API: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
        }
    }
}
