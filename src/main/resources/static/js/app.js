document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("benderForm");
    const promptInput = document.getElementById("prompt");
    const responseDiv = document.getElementById("response");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const prompt = promptInput.value.trim();

        promptInput.value = "";

        if (!prompt) {
            responseDiv.innerText = "Please enter a valid prompt.";
            responseDiv.style.color = "red";
            return;
        }

        responseDiv.innerText = "Loading...";
        responseDiv.style.color = "black";

        try {
            const response = await fetch("http://localhost:8081/bender/api/bender/ask", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(prompt),
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

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
