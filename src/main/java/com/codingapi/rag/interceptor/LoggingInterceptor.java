package com.codingapi.rag.interceptor;

import io.micrometer.core.instrument.util.IOUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@AllArgsConstructor
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private final boolean enable;

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        // 打印请求信息
        if (enable) {
            System.out.println("Http Request URI: " + request.getURI());
            System.out.println("Http Request Method: " + request.getMethod());
            System.out.println("Http Request Headers: " + request.getHeaders());
            System.out.println("Http Request Body: " + new String(body));
        }

        // 执行请求
        ClientHttpResponse response = execution.execute(request, body);

        if (enable) {
            // 打印响应信息
            System.out.println("Http Response Status Code: " + response.getStatusCode());
            System.out.println("Http Response Headers: " + response.getHeaders());
            System.out.println("Http Response Body: " + IOUtils.toString(response.getBody()));
        }
        return response;
    }
}
