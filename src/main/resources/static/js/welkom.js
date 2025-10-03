"use strict"
document.addEventListener("DOMContentLoaded", async () => {
    const gebruiker = localStorage.getItem("gebruiker");
    const rol = localStorage.getItem("rol");

    if (!gebruiker) {
        window.location.href = "index.html";
        return;
    }

    document.getElementById("gebruiker").textContent = gebruiker;

    if (rol !== "ADMIN") {
        document.querySelectorAll(".admin-only").forEach(el => el.style.display = "none");
    }

    document.getElementById("logoutBtn").addEventListener("click", async () => {
        await fetch("/auth/logout", {
            method: "POST",
            credentials: "include"
        });

        localStorage.removeItem("gebruiker");
        localStorage.removeItem("rol");
        window.location.href = "index.html";
    });

    await toonWillekeurigeQuote();
});

async function toonWillekeurigeQuote() {
    try {
        const res = await fetch("data/quotes.json");
        const quotes = await res.json();

        if (Array.isArray(quotes) && quotes.length > 0) {
            const index = Math.floor(Math.random() * quotes.length);
            document.getElementById("quote").textContent = quotes[index];
        } else {
            document.getElementById("quote").textContent = "✨ Een positieve dag gewenst!";
        }
    } catch (err) {
        console.error("Fout bij laden van quotes:", err);
        document.getElementById("quote").textContent = "🌱 Vandaag is een nieuw begin.";
    }
}