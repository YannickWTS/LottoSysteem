"use strict";
console.log("✅ login.js geladen");

document.addEventListener("DOMContentLoaded", function () {

    // 1. Versie rechtsonder instellen (Electron)
    const versionSpan = document.getElementById("version");

    if (versionSpan && window.appInfo && window.appInfo.version) {
        versionSpan.innerText = "v" + window.appInfo.version;
    } else {
        console.log("ℹ️ Geen appInfo.version gevonden (waarschijnlijk browser mode)");
    }

    // 2. Login logica
    const form = document.getElementById("loginForm");
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
            alert("Kon gebruikersgegevens niet ophalen.");
            return;
        }

        const me = await meRes.json();

        localStorage.setItem("gebruiker", me.gebruikersnaam);
        localStorage.setItem("gebruikerId", String(me.id));
        localStorage.setItem("rol", (me.rol || "USER").toUpperCase());

        window.location.href = "welkom.html";
    });
});
