"use strict";
console.log("✅ login.js is geladen");
document.title = ("version").innerText = "V" + window.appInfo.version;

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("loginForm");
    if (!form) return;

    form.addEventListener("submit", async function (e) {
        e.preventDefault();

        const gebruikersnaam = document.getElementById("gebruikersnaam").value.trim();
        const wachtwoord    = document.getElementById("wachtwoord").value;
        const resultaat     = document.getElementById("resultaat");

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

        // ✅ Haal nu jouw gegevens op (id + rol) en bewaar die
        const meRes = await fetch(`/gebruiker/${encodeURIComponent(gebruikersnaam)}`, {
            credentials: "include"
        });
        if (!meRes.ok) {
            alert("Kon gebruikersgegevens niet ophalen na login.");
            return;
        }
        const me = await meRes.json(); // Verwacht {id, gebruikersnaam, rol, ... }

        localStorage.setItem("gebruiker", me.gebruikersnaam);
        localStorage.setItem("gebruikerId", String(me.id));
        localStorage.setItem("rol", (me.rol || "USER").toUpperCase());

        window.location.href = "welkom.html";
    });
});
