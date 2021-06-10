import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileOutputStream;
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
        NASAData nasaData = mapper.readValue(response.getEntity().getContent(), NASAData.class);
        //url из json
        String jpg_url = nasaData.getUrl();

        // запрос по ссылке изображения
        HttpGet requestUrl = new HttpGet(jpg_url);
        CloseableHttpResponse responseUrl = httpClient.execute(requestUrl);
        // вывод заголовков
        Arrays.stream(responseUrl.getAllHeaders()).forEach(System.out::println);

        // чтение тела ответа
        byte[] bytes = response.getEntity().getContent().readAllBytes();

        // создание файла
        String fileName = jpg_url.substring(jpg_url.lastIndexOf("/") + 1);
        createFile(fileName);

        // запись в файл
        try (FileOutputStream fos = new FileOutputStream(fileName, false)) {
            fos.write(bytes, 0, bytes.length);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void createFile(String nameFile) {
        File newFile = new File(nameFile);
        try {
            if (newFile.createNewFile()) {
                System.out.println("\n\nНовый файл'" + nameFile + "'.");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}