package school.redrover.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import school.redrover.api.OpenAIClient;
import school.redrover.data.ResponseCache;

@RestController
@RequestMapping("/api/bender")
public class BenderController {
    private static final Logger
            logger = LoggerFactory.getLogger(BenderController.class);

    private final OpenAIClient llm = new OpenAIClient(
            "https://api.openai.com/v1/completions",
            "your-api-key",
            "text-davinci-003",
            100,
            0.7
    );
    private final ResponseCache cache = new ResponseCache();

    @PostMapping("/ask")
    public String askBender(@RequestBody String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            logger.warn("Received empty or null prompt.");
            return "Error: Prompt cannot be null or empty.";
        }
        logger.info("Received prompt: {}", prompt);

        if (cache.isCached(prompt)) {
            logger.info("Returning cached response for prompt: {}", prompt);
            return String.valueOf(cache.getResponse(prompt));
        }

        try {
            String response = llm.getResponse(prompt);
            if (response == null || response.trim().isEmpty()) {
                logger.warn("Received empty response from OpenAI for prompt: {}", prompt);
                return "Error: Received an empty response from the AI.";
            }
            cache.saveResponse(prompt, response);
            logger.info("Response generated and cached: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Error generating response: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/ask")
    public String handleGetRequest() {
        return "This endpoint only supports POST requests. Please submit a valid prompt using POST.";
    }
}
