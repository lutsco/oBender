package school.redrover.ui;

import picocli.CommandLine;
import java.util.Map;

import static org.fusesource.jansi.Ansi.ansi;

@CommandLine.Command(name = "BenderCLI", mixinStandardHelpOptions = true, version = "1.0",
        description = "Interact with Ostap Bender, the Great Strategist.")
public class CLI implements Runnable {

    @CommandLine.Option(names = {"-p", "--prompt"}, description = "Enter your question or select a predefined prompt by number.")
    private String userInput;

    public void displayAvailablePrompts(Map<String, String> prompts) {
        System.out.println("Available prompts:");
        int index = 1;
        for (Map.Entry<String, String> entry : prompts.entrySet()) {
            System.out.println(index++ + ". " + entry.getValue());
        }
        System.out.println("Type the number of your choice or enter a custom question.");
    }



    public void displayResponse(String prompt, String response) {
        System.out.println(ansi().fgGreen().a("Prompt: ").fgYellow().a(prompt).reset());
        System.out.println(ansi().fgGreen().a("Bender's Response: ").fgCyan().a(formatResponse(response)).reset());
    }

    public void displayError(String errorMessage, String suggestion) {
        System.out.println(ansi().fgRed().a("Error: ").fgBrightRed().a(errorMessage).reset());
        if (suggestion != null && !suggestion.isEmpty()) {
            System.out.println(ansi().fgYellow().a("Suggestion: ").reset() + suggestion);
        }
    }

    public void displayConversationHistory(String history) {
        System.out.println(ansi().fgBrightBlue().a("Conversation History:").reset());
        String[] lines = history.split("\n");
        for (String line : lines) {
            System.out.println(ansi().fgCyan().a(line).reset());
        }
    }

    private String formatResponse(String response) {
        int maxLength = 80; // Max characters per line
        String[] words = response.split(" ");
        StringBuilder formattedResponse = new StringBuilder();

        int currentLength = 0;
        for (String word : words) {
            if (currentLength + word.length() + 1 > maxLength) {
                formattedResponse.append("\n");
                currentLength = 0;
            }
            formattedResponse.append(word).append(" ");
            currentLength += word.length() + 1;
        }

        return formattedResponse.toString().trim();
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
        } else {
            System.out.println("Welcome to BenderCLI! Use --help for options.");
        }
    }
}
