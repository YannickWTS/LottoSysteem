"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const rol = localStorage.getItem("rol");
    if (rol !== "ADMIN") {
        window.location.href = "welkom.html";
        return;
    }

    const maandSelect = document.getElementById("maandSelect");
    const spelTypeSelect = document.getElementById("spelTypeSelect");
    const status = document.getElementById("status");
    const form = document.getElementById("potmail-form");

    // ---- Maanden vullen ----
    const maanden = [
        "januari", "februari", "maart", "april", "mei", "juni",
        "juli", "augustus", "september", "oktober", "november", "december"
    ];

    function buildMaand(offset) {
        const now = new Date();
        const d = new Date(now.getFullYear(), now.getMonth() + offset, 1);
        const jaar = d.getFullYear();
        const maandIndex = d.getMonth();
        const maandNaam = maanden[maandIndex];

         // komt overeen met BESTELLING.MAAND
        return `${maandNaam} ${jaar}`;
    }

    const opties = [buildMaand(0), buildMaand(1)];

    opties.forEach(m => {
        const opt = document.createElement("option");
        opt.value = m;
        opt.textContent = m;
        maandSelect.appendChild(opt);
    });

    // ---- Form submit ----
    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        status.textContent = "";

        const fileInput = document.getElementById("fileInput");
        const file = fileInput.files[0];

        if (!file) {
            status.textContent = "Selecteer een afbeelding.";
            return;
        }

        const formData = new FormData();
        formData.append("maandCode", maandSelect.value);
        formData.append("maandLabel", maandSelect.value);
        formData.append("spelType", spelTypeSelect.value);
        formData.append("file", file);

        try {
            const response = await fetch("/potmail/send", {
                method: "POST",
                body: formData
            });

            if (response.ok) {
                status.textContent = "Pot mail is verzonden!";
            } else {
                status.textContent = "Er ging iets mis tijdens het versturen.";
            }
        } catch (err) {
            console.error(err);
            status.textContent = "Netwerkfout bij versturen.";
        }
    });
});
