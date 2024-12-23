package school.redrover.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseCache {

    private static final Logger logger = LoggerFactory.getLogger(ResponseCache.class);

    private static final Path CONVERSATION_FILE = Path.of(
            System.getProperty("response.cache.path", "logs/conversations.jsonl")
    );

    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

    private final Gson gson = new Gson();

    public ResponseCache() {
        loadCachedConversations();
    }

    private void loadCachedConversations() {
        if (!Files.exists(CONVERSATION_FILE)) {
            return;
        }

        try {
            Files.lines(CONVERSATION_FILE).forEach(this::parseAndLoadLine);
            logger.info("Loaded cached conversations from file: {}", CONVERSATION_FILE);
        } catch (IOException e) {
            logger.error("Failed to load conversations from file: {}", e.getMessage());
        }
    }

    private void parseAndLoadLine(String line) {
        try {
            JsonObject obj = gson.fromJson(line, JsonObject.class);
            if (obj == null || !obj.has("prompt") || !obj.has("response")) {
                logger.warn("Skipping invalid JSON line (missing prompt/response): {}", line);
                return;
            }

            String prompt = obj.get("prompt").getAsString();
            String response = obj.get("response").getAsString();

            cache.computeIfAbsent(prompt, k -> new ArrayList<>()).add(response);

        } catch (JsonSyntaxException e) {
            logger.warn("Skipping invalid JSON line in cache: {}", line, e);
        }
    }

    public void saveResponse(String prompt, String response) {
        cache.computeIfAbsent(prompt, k -> new ArrayList<>()).add(response);

        saveConversationToFile(prompt, response);
    }

    public List<String> getResponse(String prompt) {
        return cache.getOrDefault(prompt, Collections.emptyList());
    }

    public boolean isCached(String prompt) {
        return cache.containsKey(prompt);
    }

    private void saveConversationToFile(String prompt, String response) {
        try {
            if (CONVERSATION_FILE.getParent() != null && !Files.exists(CONVERSATION_FILE.getParent())) {
                Files.createDirectories(CONVERSATION_FILE.getParent());
            }

            JsonObject obj = new JsonObject();
            obj.addProperty("prompt", prompt);
            obj.addProperty("response", response);
            obj.addProperty("timestamp", Instant.now().toString());

            String jsonLine = gson.toJson(obj) + System.lineSeparator();

            Files.writeString(CONVERSATION_FILE, jsonLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            logger.debug("Saved conversation to file: {}", CONVERSATION_FILE);
        } catch (IOException e) {
            logger.error("Failed to save conversation to file: {}", e.getMessage());
        }
    }
}
