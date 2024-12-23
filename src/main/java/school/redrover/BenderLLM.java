package school.redrover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import school.redrover.api.OpenAIClient;
import school.redrover.data.PromptManager;
import school.redrover.data.ResponseCache;
import school.redrover.ui.CLI;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "BenderLLM",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Interact with Ostap Bender, the Great Strategist."
)
public class BenderLLM implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(BenderLLM.class);

    @CommandLine.Option(
            names = {"-p", "--prompt"},
            description = "Enter your question or select a predefined prompt by number."
    )
    private String userInput;

    private final OpenAIClient llm;
    private final PromptManager promptManager;
    private final ResponseCache cache;
    private final CLI cli;
    private final StringBuilder conversationHistory;

    public BenderLLM() {
        this.llm = new OpenAIClient(
                "https://api.openai.com/v1/completions",
                "your-api-key",
                "text-davinci-003",
                100,
                0.7
        );
        this.promptManager = new PromptManager();
        this.cache = new ResponseCache();
        this.cli = new CLI();
        this.conversationHistory = new StringBuilder("You are Ostap Bender, the Great Strategist.\n");
    }

    @Override
    public Integer call() {
        try {
            if (userInput == null || userInput.trim().isEmpty()) {
                cli.displayError("Prompt cannot be null or empty.", "Please enter a valid prompt.");
                return 1;
            }

            if ("exit".equalsIgnoreCase(userInput)) {
                System.out.println("Exiting BenderLLM. Goodbye!");
                return 0;
            }

            cli.displayAvailablePrompts(promptManager.getPrompts());
            String prompt = determinePrompt(userInput);

            String response = processPrompt(prompt);

            addToConversationHistory("User", prompt);
            addToConversationHistory("Bender", response);
            cli.displayResponse(prompt, response);
            cli.displayConversationHistory(conversationHistory.toString());

        } catch (IOException e) {
            cli.displayError("Error communicating with OpenAI: ", e.getMessage());
            logger.error("OpenAI Communication Error: {}", e.getMessage());
        } catch (Exception e) {
            cli.displayError("An unexpected error occurred: ", e.getMessage());
            logger.error("Unexpected Error: {}", e.getMessage());
        }

        return 0;
    }

    private String determinePrompt(String userInput) {
        // Grab the map of prompts
        Map<String, String> allPrompts = promptManager.getPrompts();

        try {
            int index = Integer.parseInt(userInput);
            var promptList = allPrompts.values().stream().toList();
            if (index >= 1 && index <= promptList.size()) {
                return promptList.get(index - 1);
            }
        } catch (NumberFormatException ignored) {
        }

        return allPrompts.getOrDefault(userInput, userInput);
    }

    private String processPrompt(String prompt) throws IOException {
        String response;

        if (cache.isCached(prompt)) {
            response = String.valueOf(cache.getResponse(prompt));
            logger.info("Prompt (from cache): {}", prompt);
            logger.info("Response (from cache): {}", response);
        } else {
            response = llm.getResponse(prompt);
            cache.saveResponse(prompt, response);
            logger.info("Prompt: {}", prompt);
            logger.info("Response: {}", response);
        }

        return response;
    }

    private void addToConversationHistory(String role, String message) {
        conversationHistory.append(role).append(": ").append(message).append("\n");
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BenderLLM()).execute(args);
        System.exit(exitCode);
    }
}