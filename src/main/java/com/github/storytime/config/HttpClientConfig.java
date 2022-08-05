package com.github.storytime.config;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeaderElementIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor;

import static com.github.storytime.config.props.Constants.TIMEOUT_HEADER;
import static java.lang.Long.parseLong;
import static org.apache.http.client.config.RequestConfig.custom;
import static org.apache.http.impl.client.HttpClientBuilder.create;
import static org.apache.http.protocol.HTTP.CONN_KEEP_ALIVE;

@Configuration
public class HttpClientConfig {

    private final Logbook logbook;

    @Value("${http.connect.timeout.millis}")
    private int httpConnectTimeoutMillis;

    @Value("${http.connect.manager.timeout.millis}")
    private int httpConnectManagerTimeoutMillis;

    @Value("${http.request.timeout.millis}")
    private int httpRequestTimeoutMillis;

    @Value("${http.request.keep.alive.millis}")
    private int httpRequestKeepAliveMillis;

    @Value("${http.request.max.connections}")
    private int maxConn;

    @Autowired
    public HttpClientConfig(final Logbook logbook) {
        this.logbook = logbook;
    }

    @Bean
    public RestTemplate restTemplate(final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory) {
        return new RestTemplate(clientHttpRequestFactory);
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory(final HttpClient httpClient) {
        final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient);
        return clientHttpRequestFactory;
    }

    @Bean
    public CloseableHttpClient httpClient() {

        final RequestConfig requestConfig = custom()
                .setConnectionRequestTimeout(httpConnectTimeoutMillis)
                .setConnectTimeout(httpConnectManagerTimeoutMillis)
                .setSocketTimeout(httpRequestTimeoutMillis)
                .build();

        return create()
                .setDefaultRequestConfig(requestConfig)
                .addInterceptorFirst(new LogbookHttpRequestInterceptor(logbook))
                .addInterceptorFirst(new LogbookHttpResponseInterceptor())
                .setKeepAliveStrategy(connectionKeepAliveStrategy())
                .setMaxConnTotal(maxConn)
                .build();
    }

    @Bean
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return (response, context) -> {
            final HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(CONN_KEEP_ALIVE));

            while (it.hasNext()) {
                final HeaderElement he = it.nextElement();
                final String param = he.getName();
                final String value = he.getValue();

                if (value != null && param.equalsIgnoreCase(TIMEOUT_HEADER)) {
                    return parseLong(value) * 1000;
                }
            }
            return httpRequestKeepAliveMillis;
        };
    }
}
