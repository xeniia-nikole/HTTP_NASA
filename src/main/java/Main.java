import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static final String HTTP_URL =
            "https://api.nasa.gov/planetary/apod?api_key=pPsb2pIy4gPSdhBRh0xhNYi0JWUq9lnYhpKPa3dD";
    public static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setUserAgent("My test service")
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();

        HttpGet request = new HttpGet(HTTP_URL);
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        CloseableHttpResponse response = httpClient.execute(request);
        // вывод полученных заголовков
        Arrays.stream(response.getAllHeaders()).forEach(System.out::println);
        // чтение тела ответа
        NASAData nasa = mapper.readValue(response.getEntity().getContent(), NASAData.class);
        System.out.println(nasa.getUrl());

    }
}