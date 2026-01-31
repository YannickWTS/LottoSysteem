"use strict";
console.log("✅ login.js is geladen");

// --- Login logica ---
document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("loginForm");

    // Toggle Password setup
    const toggleBtn = document.getElementById("togglePassword");
    const passwordInput = document.getElementById("wachtwoord");
    if (toggleBtn && passwordInput) {
        toggleBtn.addEventListener("click", () => {
            const type = passwordInput.getAttribute("type") === "password" ? "text" : "password";
            passwordInput.setAttribute("type", type);
            // Visuele indicatie: blauw (primary) als het zichtbaar is
            toggleBtn.style.color = (type === "text") ? "var(--primary)" : "";
        });
    }

    if (!form) return;

    form.addEventListener("submit", async function (e) {
        e.preventDefault();

        const gebruikersnaam = document.getElementById("gebruikersnaam").value.trim();
        const wachtwoord = document.getElementById("wachtwoord").value;
        const resultaat = document.getElementById("resultaat");

        const loginResponse = await fetch("/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ gebruikersnaam, wachtwoord })
        });

        if (!loginResponse.ok) {
            resultaat.textContent = "❌ Inloggen mislukt!";
            resultaat.classList.add("error");
            return;
        }

        const meRes = await fetch(`/gebruiker/${encodeURIComponent(gebruikersnaam)}`, {
            credentials: "include"
        });
        if (!meRes.ok) {
            alert("Kon gebruikersgegevens niet ophalen na login.");
            return;
        }
        const me = await meRes.json();

        localStorage.setItem("gebruiker", me.gebruikersnaam);
        localStorage.setItem("gebruikerId", String(me.id));
        localStorage.setItem("rol", (me.rol || "USER").toUpperCase());

        window.location.href = "welkom.html";
    });
});
