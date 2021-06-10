import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static final String SERVICE_URL = "https://api.nasa.gov/planetary/apod?api_key=pPsb2pIy4gPSdhBRh0xhNYi0JWUq9lnYhpKPa3dD";
    public static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setUserAgent("Test service")
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build();

        // создание объекта запроса:
        HttpGet request = new HttpGet(SERVICE_URL);
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        try {
            // отправка запроса:
            CloseableHttpResponse response = httpClient.execute(request);
            // вывод полученных заголовков:
            Arrays.stream(response.getAllHeaders()).forEach(System.out::println);

            // преобразование json
            mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            List<NASAData> jsonNasaData = mapper.readValue(response.getEntity().getContent(), new TypeReference<>() {
            });
            
            // запись в файл
            String json = listToJson(jsonNasaData);
            try (FileWriter file = new FileWriter("ImageData.json")) {
                file.write(json);
                file.flush();
            } catch (IOException io) {
                io.printStackTrace();
            }

            // url из прочитанного json:
            String JPG_URL = jsonNasaData.get(0).getUrl();

            // новый объекта запроса:
            HttpGet requestJpg = new HttpGet(JPG_URL);
            requestJpg.setHeader(HttpHeaders.ACCEPT, ContentType.IMAGE_JPEG.getMimeType());
            // отправка запроса:
            response = httpClient.execute(requestJpg);

            // вывод полученных заголовков
            Arrays.stream(response.getAllHeaders()).forEach(System.out::println);

            // тело ответа в виде массива байт
            byte[] bytes = response.getEntity().getContent().readAllBytes();

            // имя файла
            String fileName = "C:/Users/User/Desktop/" + JPG_URL.substring(JPG_URL.lastIndexOf("/") + 1);

            // создание файла
            createFile(fileName);

            // запись в файл
            try (FileOutputStream fos = new FileOutputStream(fileName, false)) {
                fos.write(bytes,0,bytes.length);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void createFile(String nameFile) {
        File newFile = new File(nameFile);
        try {
            if (newFile.createNewFile()) {
                System.out.println("\n\nNew file'" + nameFile + " was created'.");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static String listToJson(List<NASAData> list) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(list);
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(json);
        return gson.toJson(je);
    }
}