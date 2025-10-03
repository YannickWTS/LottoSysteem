document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("loginForm");

    if (!form) {
        console.error("Formulier 'loginForm' niet gevonden!");
        return;
    }

    form.addEventListener("submit", async function (e) {
        e.preventDefault();

        const gebruikersnaam = document.getElementById("gebruikersnaam").value;
        const wachtwoord = document.getElementById("wachtwoord").value;
        const rol = document.getElementById("rol").value;

        const response = await fetch("/gebruiker", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ gebruikersnaam, wachtwoord, rol })
        });

        const resultDiv = document.getElementById("resultaat");

        if (response.status === 201) {
            const location = response.headers.get("Location");
            resultDiv.textContent = `✅ Gebruiker aangemaakt! ID: ${location}`;
            resultDiv.classList.remove("error");
        } else if (response.status === 403) {
            resultDiv.textContent = "⛔ Je hebt geen rechten om dit te doen (403)";
            resultDiv.classList.add("error");
        } else if (response.status === 401) {
            resultDiv.textContent = "🔒 Je bent niet ingelogd (401)";
            resultDiv.classList.add("error");
        } else {
            const errorText = await response.text();
            resultDiv.textContent = `❌ Fout bij aanmaken: ${response.status} ${errorText}`;
            resultDiv.classList.add("error");
        }
    });
});
