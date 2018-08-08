package com.github.storytime.other;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class RequestLoggerInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LogManager.getLogger(RequestLoggerInterceptor.class);

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
                                        final ClientHttpRequestExecution execution) throws IOException {
        traceRequest(body);
        final ClientHttpResponse response = execution.execute(request, body);
        traceResponse(response);
        return response;
    }

    private void traceRequest(final byte[] body) {
        try {
            final String message = "\n=============================request begin================================================\n" +
                    "Request body: " + new String(body, UTF_8) +
                    "\n=============================request end================================================";
            LOGGER.trace(message);
        } catch (final Exception e) {
            LOGGER.error("Cannot trace request: ", e);
        }
    }

    private void traceResponse(final ClientHttpResponse response) {
        try {
            final String message = "\n=============================response begin================================================\n" +
                    "Response code:     " + response.getRawStatusCode() + "\n" +
                    "Headers:           " + response.getHeaders() + "\n" +
                    "=============================response end================================================";
            LOGGER.trace(message);
        } catch (final Exception e) {
            LOGGER.error("Cannot trace response: ", e);
        }
    }

}