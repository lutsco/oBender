package school.redrover.api;

import okhttp3.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class OpenAIClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIClient.class);
    private static final String API_URL = "https://api.openai.com/v1/completions";
    private static final String API_KEY;

    // Static block to initialize the API key
    static {
        API_KEY = loadApiKey();
    }

    // Instance-level OkHttpClient for better reusability
    private final OkHttpClient client;

    public OpenAIClient() {
        this.client = new OkHttpClient();
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalStateException("API key is not set in local.properties or environment variables.");
        }
        logger.info("OpenAIClient initialized successfully.");
    }

    /**
     * Load API key from environment variables or local.properties.
     *
     * @return the API key
     */
    private static String loadApiKey() {
        // Attempt to load from environment variable first
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            logger.info("Loaded API key from environment variables.");
            return apiKey;
        }

        // Fallback to local.properties
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("local.properties")) {
            properties.load(fis);
            apiKey = properties.getProperty("local.openai.api_key");
            if (apiKey != null && !apiKey.isEmpty()) {
                logger.info("Loaded API key from local.properties.");
                return apiKey;
            }
        } catch (IOException e) {
            logger.error("Error loading API key from local.properties: {}", e.getMessage());
        }

        logger.error("API key not found. Please set it in environment variables or local.properties.");
        return null;
    }

    /**
     * Send a prompt to OpenAI's API and get a response.
     *
     * @param prompt the input prompt
     * @return the generated response
     * @throws IOException if an error occurs during the API request
     */
    public String getResponse(String prompt) throws IOException {
        String json = buildRequestBody(prompt);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        logger.info("Sending prompt to OpenAI: {}", prompt);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Unexpected response code: {} - {}", response.code(), response.message());
                throw new IOException("Unexpected response code: " + response.code());
            }

            String responseBody = response.body().string();
            logger.debug("Raw response body: {}", responseBody);

            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            if (!jsonObject.has("choices") || jsonObject.getAsJsonArray("choices").size() == 0) {
                logger.warn("Invalid response format: {}", responseBody);
                throw new IOException("Invalid response format: " + responseBody);
            }

            String result = jsonObject.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString().trim();
            logger.info("Received response from OpenAI: {}", result);
            return result;
        }
    }

    /**
     * Build the JSON request body for the OpenAI API.
     *
     * @param prompt the input prompt
     * @return the JSON string
     */
    private String buildRequestBody(String prompt) {
        JsonObject json = new JsonObject();
        json.addProperty("model", "text-davinci-003");
        json.addProperty("prompt", prompt);
        json.addProperty("max_tokens", 100);
        json.addProperty("temperature", 0.7);
        return json.toString();
    }

    /**
     * Send a prompt to OpenAI's API with retry logic for rate limits.
     *
     * @param prompt the input prompt
     * @return the generated response
     * @throws IOException if an error occurs during the API request
     */
    public String getResponseWithRetry(String prompt) throws IOException {
        int maxRetries = 3;
        int retryDelay = 1000; // Initial delay in milliseconds

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return getResponse(prompt);
            } catch (IOException e) {
                if (attempt < maxRetries && e.getMessage().contains("429")) {
                    logger.warn("Rate limit reached. Retrying in {} ms... (Attempt {}/{})", retryDelay, attempt, maxRetries);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Retry interrupted", ie);
                    }
                    retryDelay *= 2; // Exponential backoff
                } else {
                    throw e;
                }
            }
        }
        throw new IOException("Max retries exceeded");
    }
}
