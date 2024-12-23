// Wait until the DOM is fully loaded
document.addEventListener("DOMContentLoaded", () => {
    // Select the form and the input elements
    const form = document.getElementById("benderForm");
    const promptInput = document.getElementById("prompt");
    const responseDiv = document.getElementById("response");

    // Add an event listener to the form for the "submit" event
    form.addEventListener("submit", async (event) => {
        event.preventDefault(); // Prevent the form from refreshing the page

        // Get the user's input
        const prompt = promptInput.value.trim();

        // Clear the input field
        promptInput.value = "";

        // Validate input
        if (!prompt) {
            responseDiv.innerText = "Please enter a valid prompt.";
            responseDiv.style.color = "red";
            return;
        }

        // Display a loading message
        responseDiv.innerText = "Loading...";
        responseDiv.style.color = "black";

        try {
            // Send a POST request to the backend
            const response = await fetch("http://localhost:8081/api/bender/ask", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(prompt),
            });

            // Check if the response is OK
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            // Parse and display the response
            const responseText = await response.text();
            responseDiv.innerText = responseText;
            responseDiv.style.color = "black";
        } catch (error) {
            console.error("Error communicating with the backend:", error);
            responseDiv.innerText = "An error occurred. Please try again.";
            responseDiv.style.color = "red";
        }
    });
});
