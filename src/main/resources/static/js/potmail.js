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
    const fileInput = document.getElementById("fileInput");

    // ---- Status helpers (matcht jouw CSS: .form-msg.ok / .form-msg.error) ----
    function resetStatus() {
        status.textContent = "";
        status.classList.add("hidden");
        status.classList.remove("ok", "error");
    }

    function showOk(msg) {
        status.textContent = msg;
        status.classList.remove("hidden");
        status.classList.remove("error");
        status.classList.add("ok");
    }

    function showError(msg) {
        status.textContent = msg;
        status.classList.remove("hidden");
        status.classList.remove("ok");
        status.classList.add("error");
    }

    // ---- Maanden vullen ----
    const maanden = [
        "januari", "februari", "maart", "april", "mei", "juni",
        "juli", "augustus", "september", "oktober", "november", "december"
    ];

    function buildMaand(offset) {
        const now = new Date();
        const d = new Date(now.getFullYear(), now.getMonth() + offset, 1);
        return `${maanden[d.getMonth()]} ${d.getFullYear()}`;
    }

    [buildMaand(0), buildMaand(1)].forEach(m => {
        const opt = document.createElement("option");
        opt.value = m;
        opt.textContent = m;
        maandSelect.appendChild(opt);
    });

    // start proper
    resetStatus();

    // ---- Form submit ----
    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        resetStatus();

        const file = fileInput.files[0];
        if (!file) {
            showError("Selecteer een afbeelding.");
            return;
        }
        if (!spelTypeSelect.value) {
            showError("Kies een speltype.");
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
                showOk("Pot mail is verzonden!");
                form.reset();
            } else {
                showError("Er ging iets mis tijdens het versturen.");
            }
        } catch (err) {
            console.error(err);
            showError("Netwerkfout bij versturen.");
        }
    });
});
