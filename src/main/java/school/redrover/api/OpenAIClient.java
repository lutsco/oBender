package school.redrover.api;

import okhttp3.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

public class OpenAIClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIClient.class);

    // Make these configurable via constructor or Spring injection
    private final String apiUrl;
    private final String apiKey;
    private final String model;
    private final int maxTokens;
    private final double temperature;

    private final OkHttpClient client;

    public OpenAIClient(String apiUrl, String apiKey, String model, int maxTokens, double temperature) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API key must be provided.");
        }

        this.client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        logger.info("OpenAIClient initialized with model={} maxTokens={} temperature={}", model, maxTokens, temperature);
    }

    public String getResponse(String prompt) throws IOException {
        String requestBody = buildRequestBody(prompt);
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        logger.debug("Sending prompt to OpenAI: {}", prompt);

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                logger.error("OpenAI API Error. HTTP {} - Body: {}", response.code(), responseBody);
                throw new IOException("OpenAI API Error: " + responseBody);
            }

            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            String result = jsonObject.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString().trim();

            logger.debug("OpenAI Response: {}", result);
            return result;
        }
    }

    private String buildRequestBody(String prompt) {
        JsonObject json = new JsonObject();
        json.addProperty("model", model);
        json.addProperty("prompt", prompt);
        json.addProperty("max_tokens", maxTokens);
        json.addProperty("temperature", temperature);
        return json.toString();
    }
}

