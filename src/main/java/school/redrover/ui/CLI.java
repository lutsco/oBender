package school.redrover.ui;

import picocli.CommandLine;
import java.util.Map;

import static org.fusesource.jansi.Ansi.ansi;

@CommandLine.Command(
        name = "BenderCLI",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Interact with Ostap Bender, the Great Strategist."
)
public class CLI implements Runnable {

    @CommandLine.Option(
            names = {"-p", "--prompt"},
            description = "Enter your question or select a predefined prompt by number."
    )
    private String userInput;
    private int maxLineLength = 80;
    private boolean showPromptKeys = false;

    public void displayAvailablePrompts(Map<String, String> prompts) {
        System.out.println("Available prompts:");
        int index = 1;
        for (Map.Entry<String, String> entry : prompts.entrySet()) {
            if (showPromptKeys) {
                System.out.printf("%d. (%s) %s%n", index++, entry.getKey(), entry.getValue());
            } else {
                System.out.println(index++ + ". " + entry.getValue());
            }
        }
        System.out.println("Type the number of your choice or enter a custom question.");
    }

    public void displayResponse(String prompt, String response) {
        System.out.println(ansi()
                .fgGreen().a("Prompt: ")
                .fgYellow().a(prompt)
                .reset()
        );
        String wrappedResponse = formatResponse(response, maxLineLength);
        System.out.println(ansi()
                .fgGreen().a("Bender's Response: ")
                .fgCyan().a(wrappedResponse)
                .reset()
        );
    }

    public void displayError(String errorMessage, String suggestion) {
        System.out.println(ansi()
                .fgRed().a("Error: ")
                .fgBrightRed().a(errorMessage)
                .reset()
        );
        if (suggestion != null && !suggestion.isEmpty()) {
            System.out.println(ansi()
                    .fgYellow().a("Suggestion: ")
                    .reset()
                    .toString() + suggestion
            );
        }
    }

    public void displayConversationHistory(String history) {
        System.out.println(ansi().fgBrightBlue().a("Conversation History:").reset());
        String[] lines = history.split("\n");
        for (String line : lines) {
            System.out.println(ansi().fgCyan().a(line).reset());
        }
    }

    private String formatResponse(String response, int maxLineLength) {
        String[] words = response.split(" ");
        StringBuilder formatted = new StringBuilder();

        int currentLength = 0;
        for (String word : words) {
            if (currentLength + word.length() + 1 > maxLineLength) {
                formatted.append("\n");
                currentLength = 0;
            }
            formatted.append(word).append(" ");
            currentLength += word.length() + 1;
        }

        return formatted.toString().trim();
    }

    private String formatResponse(String response) {
        return formatResponse(response, this.maxLineLength);
    }

    public boolean isValidPromptSelection(String input, int maxOptions) {
        try {
            int choice = Integer.parseInt(input);
            return choice >= 1 && choice <= maxOptions;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void run() {
        if (userInput != null) {
            System.out.println("User provided input: " + userInput);
            // TODO: Possibly call your LLM or prompt manager with userInput
        } else {
            System.out.println("Welcome to BenderCLI! Use --help for options.");
        }
    }

    public int getMaxLineLength() {
        return maxLineLength;
    }

    public void setMaxLineLength(int maxLineLength) {
        this.maxLineLength = maxLineLength;
    }

    public boolean isShowPromptKeys() {
        return showPromptKeys;
    }

    public void setShowPromptKeys(boolean showPromptKeys) {
        this.showPromptKeys = showPromptKeys;
    }
}