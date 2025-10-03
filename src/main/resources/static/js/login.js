"use strict"
console.log("✅ login.js is geladen");

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("loginForm");

    if (!form) {
        console.error("Login formulier niet gevonden!");
        return;
    }

    form.addEventListener("submit", async function (e) {
        e.preventDefault();

        const gebruikersnaam = document.getElementById("gebruikersnaam").value;
        const wachtwoord = document.getElementById("wachtwoord").value;

        const response = await fetch("/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include",
            body: JSON.stringify({
                gebruikersnaam: gebruikersnaam,   // <--- VERANDERD!
                wachtwoord: wachtwoord        // <--- VERANDERD!
            })
        });

        const resultaat = document.getElementById("resultaat");

        console.log("⚠️ Response status:", response.status);
        if (response.ok) {
            console.log("✅ Login gelukt, doorverwijzen...");

            // ⬇️ HIER SLA JE DE GEBRUIKER OP IN localStorage
            localStorage.setItem("gebruiker", gebruikersnaam);
            localStorage.setItem("rol", "ADMIN"); // of "USER" als je dat weet

            window.location.href = "welkom.html";
        } else {
            resultaat.textContent = "❌ Inloggen mislukt!";
            resultaat.classList.add("error");
        }
    });
});
