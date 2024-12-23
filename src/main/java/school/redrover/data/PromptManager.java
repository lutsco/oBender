package school.redrover.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PromptManager {

    private static final Logger logger = LoggerFactory.getLogger(PromptManager.class);
    private final Map<String, String> prompts = new HashMap<>();

    public PromptManager() {
        loadDefaultPrompts();
    }

    private void loadDefaultPrompts() {
        try (InputStream input = getClass().getResourceAsStream("/prompts_default.properties")) {
            if (input == null) {
                logger.warn("prompts_default.properties not found in resources. Using fallback prompts.");
                prompts.put("success", "What is the key to success?");
                prompts.put("strategy", "How would you handle a complex strategy?");
                prompts.put("escape", "Plan a bold escape from a difficult situation.");
                prompts.put("money-making", "Generate a money-making plan.");
                prompts.put("argument", "Give advice for winning an argument.");
                prompts.put("setback", "How would you handle a sudden setback?");
                return;
            }

            Properties properties = new Properties();
            properties.load(input);

            for (String key : properties.stringPropertyNames()) {
                prompts.put(key, properties.getProperty(key));
            }
            logger.info("Loaded default prompts from prompts_default.properties");
        } catch (IOException e) {
            logger.error("Error reading prompts_default.properties", e);
        }
    }

    public Map<String, String> getPrompts() {
        return Collections.unmodifiableMap(prompts);
    }

    public String getPrompt(String key) {
        return prompts.get(key);
    }

    public void addPrompt(String key, String prompt) {
        prompts.put(key, prompt);
        logger.info("Prompt added: {} - {}", key, prompt);
    }

    public void updatePrompt(String key, String newPrompt) {
        if (prompts.containsKey(key)) {
            prompts.put(key, newPrompt);
            logger.info("Prompt updated: {} - {}", key, newPrompt);
        } else {
            logger.warn("Prompt key not found: {}", key);
        }
    }

    public void removePrompt(String key) {
        if (prompts.containsKey(key)) {
            prompts.remove(key);
            logger.info("Prompt removed: {}", key);
        } else {
            logger.warn("Prompt key not found: {}", key);
        }
    }

    public Map<String, String> searchPrompts(String keyword) {
        Map<String, String> results = new HashMap<>();
        String lowerCaseKeyword = keyword.toLowerCase();
        for (Map.Entry<String, String> entry : prompts.entrySet()) {
            if (entry.getValue().toLowerCase().contains(lowerCaseKeyword)) {
                results.put(entry.getKey(), entry.getValue());
            }
        }
        return results;
    }
}
