package school.redrover.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PromptManager {

    private static final Logger logger = LoggerFactory.getLogger(PromptManager.class);
    private final Map<String, String> prompts = new HashMap<>();

    public PromptManager() {
        // Add predefined prompts
        prompts.put("success", "What is the key to success?");
        prompts.put("strategy", "How would you handle a complex strategy?");
        prompts.put("escape", "Plan a bold escape from a difficult situation.");
        prompts.put("money-making", "Generate a money-making plan.");
        prompts.put("argument", "Give advice for winning an argument.");
        prompts.put("setback", "How would you handle a sudden setback?");
    }

    public Map<String, String> getPrompts(String userInput) {
        return prompts; // Return the map of all prompts
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

    public void displayScenarios() {
        System.out.println("Predefined scenarios:");
        for (Map.Entry<String, String> entry : prompts.entrySet()) {
            System.out.println("- " + entry.getKey() + ": " + entry.getValue());
        }
    }

    public void searchPrompts(String keyword) {
        System.out.println("Search results for keyword: " + keyword);
        for (Map.Entry<String, String> entry : prompts.entrySet()) {
            if (entry.getValue().toLowerCase().contains(keyword.toLowerCase())) {
                System.out.println("- " + entry.getKey() + ": " + entry.getValue());
            }
        }
    }

    public void loadPrompts(String language) {
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/prompts_" + language + ".properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find prompts for language: " + language);
                return;
            }
            properties.load(input);
            for (String key : properties.stringPropertyNames()) {
                prompts.put(key, properties.getProperty(key));
            }
            logger.info("Prompts loaded for language: {}", language);
        } catch (IOException ex) {
            logger.error("Error loading prompts for language: {}", language, ex);
        }
    }
}
