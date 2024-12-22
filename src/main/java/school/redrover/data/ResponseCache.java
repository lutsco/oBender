package school.redrover.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseCache {

    private static final Logger logger = LoggerFactory.getLogger(ResponseCache.class);
    private static final Path CONVERSATION_FILE = Path.of(
            System.getProperty("response.cache.path", "logs/conversations.txt")
    );

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public ResponseCache() {
        try {
            if (Files.exists(CONVERSATION_FILE)) {
                Files.lines(CONVERSATION_FILE)
                        .filter(line -> line.startsWith("Prompt: "))
                        .forEach(line -> {
                            String[] parts = line.split("Prompt: |Response: ", 3);
                            if (parts.length == 3) {
                                cache.put(parts[1].trim(), parts[2].trim());
                            }
                        });
                logger.info("Loaded cached conversations from file.");
            }
        } catch (IOException e) {
            logger.error("Failed to load conversations from file: {}", e.getMessage());
        }
    }

    public void saveResponse(String prompt, String response) {
        cache.put(prompt, response);
        saveConversationToFile(prompt, response);
    }

    public String getResponse(String prompt) {
        return cache.get(prompt);
    }

    public boolean isCached(String prompt) {
        return cache.containsKey(prompt);
    }

    private void saveConversationToFile(String prompt, String response) {
        try {
            if (!Files.exists(CONVERSATION_FILE.getParent())) {
                Files.createDirectories(CONVERSATION_FILE.getParent());
            }

            String entry = String.format("Prompt: %s%nResponse: %s%n%n", prompt, response);
            Files.writeString(CONVERSATION_FILE, entry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            logger.info("Saved conversation to file: {}", CONVERSATION_FILE);
        } catch (IOException e) {
            logger.error("Failed to save conversation to file: {}", e.getMessage());
        }
    }
}
