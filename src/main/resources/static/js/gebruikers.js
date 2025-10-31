"use strict";

/* ============================================================================
 * Helpers
 * ==========================================================================*/

function qs(selector) {
    return document.querySelector(selector);
}
const btnLogout  = qs("#btnLogout");

async function apiFetch(url, options = {}) {
    // Zorg dat cookies/sessie meegaan
    return fetch(url, {
        credentials: "include",
        headers: {"Content-Type": "application/json"},
        ...options
    });
}

function getCurrentUserRole() {
    return localStorage.getItem("rol"); // door login.js gezet (USER/ADMIN)
}

function getCurrentUserId() {
    const raw = localStorage.getItem("gebruikerId");
    return raw ? Number(raw) : null;
}

function setMsg(el, type, text) {
    if (!el) return;
    el.textContent = text || "";
    el.classList.remove("error", "ok", "hidden");
    if (!text) {
        el.classList.add("hidden");
        return;
    }
    if (type === "error") el.classList.add("error");
    if (type === "ok") el.classList.add("ok");
}


/* ============================================================================
 * Navigatie
 * ==========================================================================*/

const backButton = qs("#btnTerug");
if (backButton) {
    backButton.addEventListener("click", () => (window.location.href = "/welkom.html"));
}

if (btnLogout) {
    btnLogout.addEventListener("click", async () => {
        const ok = window.confirm("Afmelden en terug naar login?");
        if (!ok) return;
        try { await apiFetch("/auth/logout", { method: "POST" }); }
        finally { location.href = "index.html"; }
    });
}

/* ============================================================================
 * Eigen wachtwoord wijzigen (iedereen)
 * ==========================================================================*/

const passwordForm = qs("#formWachtwoord");

if (passwordForm) {
    const msgEl = document.getElementById("pwFeedback");
    const btn = passwordForm.querySelector('button[type="submit"]');

    function validateOwnPw() {
        const w1 = passwordForm.w1.value.trim();
        const w2 = passwordForm.w2.value.trim();

        if (w1.length === 0 && w2.length === 0) {
            setMsg(msgEl, null, "");
            if (btn) btn.disabled = true;
            return;
        }
        if (w1.length < 6) {
            setMsg(msgEl, "error", "Wachtwoord is te kort (minimaal 6 tekens).");
            if (btn) btn.disabled = true;
            return;
        }
        if (w1 !== w2) {
            setMsg(msgEl, "error", "Wachtwoorden komen niet overeen.");
            if (btn) btn.disabled = true;
            return;
        }
        setMsg(msgEl, "ok", "Ziet er goed uit ✔");
        if (btn) btn.disabled = false;
    }

    passwordForm.w1.addEventListener("input", validateOwnPw);
    passwordForm.w2.addEventListener("input", validateOwnPw);
    // start state
    if (btn) btn.disabled = true;
    passwordForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const meId = getCurrentUserId();
        if (!meId) {
            alert("Geen gebruikers-id gevonden. Log opnieuw in.");
            window.location.href = "/index.html";
            return;
        }

        const newPassword = passwordForm.w1.value.trim();
        const repeatPassword = passwordForm.w2.value.trim();

        if (newPassword.length < 6) {
            alert("Wachtwoord te kort (minimaal 6 tekens).");
            return;
        }
        if (newPassword !== repeatPassword) {
            alert("Wachtwoorden komen niet overeen.");
            return;
        }

        const response = await apiFetch(`/gebruiker/${meId}/wachtwoord`, {
            method: "PUT",
            body: JSON.stringify({wachtwoord: newPassword})
        });

        if (response.ok) {
            alert("Wachtwoord gewijzigd.");
            passwordForm.reset();
            setMsg(msgEl, null, "");     // 👈 feedback leeg
            if (btn) btn.disabled = true; // 👈 knop weer uit
            validateOwnPw();             // 👈 beginstaat forceren
        } else if (response.status === 401) {
            window.location.href = "/index.html";
        } else if (response.status === 403) {
            alert("Geen rechten om dit te wijzigen.");
        } else {
            alert("Wijzigen mislukt.");
        }
    });
}

const profielForm = document.getElementById("formProfiel");
if (profielForm) {
    const pMsg = document.getElementById("profielFeedback");

    function setPMsg(type, text) {
        setMsg(pMsg, type, text);
    }

    profielForm.addEventListener("input", () => {
        const naam = profielForm.gebruikersnaam.value.trim();
        if (!naam || naam.length < 2) {
            setPMsg("error", "Gebruikersnaam is te kort.");
        } else {
            setPMsg("ok", "OK ✔");
        }
    });

    /* ============================================================================
 * All: profiel wijzigen.
 * ==========================================================================*/
    profielForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const id = Number(profielForm.id.value);
        const naam = profielForm.gebruikersnaam.value.trim();
        if (naam.length < 2) {
            setPMsg("error", "Gebruikersnaam is te kort.");
            return;
        }

        const r = await fetch("/gebruiker/mijn-naam", {
            method: "PUT",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ gebruikersnaam: naam })
        });

        if (r.ok) {
            setPMsg("ok", "Profiel opgeslagen.");
            // houd UI & storage gelijk
            localStorage.setItem("gebruiker", naam);
            await loadUsersForAdmin(); // optioneel: refresh adminlijst als je admin bent
        } else if (r.status === 409) {
            setPMsg("error", "Gebruikersnaam bestaat al.");
        } else if (r.status === 401) {
            window.location.href = "/index.html";
        } else if (r.status === 403) {
            setPMsg("error", "Geen rechten om dit profiel te wijzigen.");
        } else {
            setPMsg("error", "Opslaan mislukt.");
        }
    });
}

/* ============================================================================
 * Admin: gebruikerslijst
 * ==========================================================================*/

async function loadUsersForAdmin() {
    const adminBlock = qs("#adminBlok");
    const tableBody = qs("#usersTbody");

    // Als je geen admin bent, gewoon niets tonen.
    if (getCurrentUserRole() !== "ADMIN") return;

    const response = await apiFetch("/gebruiker");

    if (response.status === 401) {
        window.location.href = "/index.html";
        return;
    }
    if (response.status === 403) {
        console.warn("403 op /gebruiker: back-end ziet je niet als ADMIN.");
        return;
    }

    adminBlock.classList.remove("hidden");

    const users = await response.json();
    tableBody.innerHTML = "";

    for (const user of users) {
        const row = document.createElement("tr");
        row.innerHTML = `
      <td>${user.id}</td>
      <td>${user.gebruikersnaam}</td>
      <td>
        <span class="role-pill" data-id="${user.id}" data-role="${user.rol}">
          ${user.rol}
        </span>
      </td>
      <td class="actions-cell">
        <div class="action-list">
          <span class="action-link act-password" data-id="${user.id}">Wachtwoord</span>
          <span class="action-link act-role" data-id="${user.id}" data-current="${user.rol}">Rol</span>
          <span class="action-link danger act-delete" data-id="${user.id}">Verwijder</span>
        </div>
        <div class="inline-slot"></div> <!-- popover komt hier -->
      </td>
    `;
        tableBody.appendChild(row);
    }
}

/* ============================================================================
 * Popover manager (maximaal één open)
 * ==========================================================================*/

let openSlot = null;

function clearInline(slot) {
    if (!slot) return;
    slot.innerHTML = "";
    slot.dataset.mode = "";
    if (openSlot === slot) openSlot = null;
}

function openPopover(slot, mode, html) {
    if (openSlot && openSlot !== slot) clearInline(openSlot);
    if (slot.dataset.mode === mode) {
        clearInline(slot);
        return;
    }
    slot.innerHTML = html;
    slot.dataset.mode = mode;
    openSlot = slot;
}

// Klik buiten popover → sluiten
document.addEventListener("click", (e) => {
    if (!openSlot) return;
    const pop = openSlot.querySelector(".popover");
    if (!pop) {
        openSlot = null;
        return;
    }

    const clickedInsidePopover = pop.contains(e.target);
    const clickedOnBadges = openSlot.previousElementSibling && openSlot.previousElementSibling.contains(e.target);

    if (!clickedInsidePopover && !clickedOnBadges) clearInline(openSlot);
});

/* ============================================================================
 * Click actions (admin-rijen)
 * ==========================================================================*/

document.addEventListener("click", async (e) => {

    // Verwijderen – debounce tegen confirm-spam
    if (e.target.matches(".act-delete")) {
        e.stopPropagation();
        if (document.body.dataset.deleting === "1") return;
        document.body.dataset.deleting = "1";

        const id = e.target.dataset.id;
        try {
            if (!confirm("Gebruiker verwijderen?")) return;
            const resp = await apiFetch(`/gebruiker/${id}`, {method: "DELETE"});
            if (resp.ok) {
                await loadUsersForAdmin();
            } else if (resp.status === 401) {
                window.location.href = "/index.html";
            } else if (resp.status === 403) {
                alert("Geen rechten om te verwijderen.");
            } else {
                alert("Verwijderen mislukt.");
            }
        } finally {
            document.body.dataset.deleting = "0";
        }
        return;
    }

    // Wachtwoord-popover
    if (e.target.matches(".act-password")) {
        e.stopPropagation();
        const id = e.target.dataset.id;
        const cell = e.target.closest(".actions-cell");
        const slot = cell.querySelector(".inline-slot");

        openPopover(slot, "password", `
      <div class="popover">
        <div class="stack">
          <label>Nieuw</label>
          <div class="field"><input type="password" class="pw1" placeholder="min. 6 tekens"></div>

          <label>Herhaal</label>
          <div class="field"><input type="password" class="pw2" placeholder="herhaal"></div>
        </div>
        <div class="actions">
          <button class="save-password" data-id="${id}" disabled>Opslaan</button>
        </div>
         <p class="form-msg pw-msg hidden" aria-live="polite" style="margin-top:6px;"></p>
      </div>
    `);
        return;
    }

    // Rol-popover
    if (e.target.matches(".act-role")) {
        e.stopPropagation();
        const id = e.target.dataset.id;
        const current = (e.target.dataset.current || "USER").toUpperCase();
        const cell = e.target.closest(".actions-cell");
        const slot = cell.querySelector(".inline-slot");

        openPopover(slot, "role", `
      <div class="popover">
        <div class="row">
          <label>Rol</label>
          <select class="role-select" data-initial="${current}">
            <option ${current === "USER" ? "selected" : ""}>USER</option>
            <option ${current === "ADMIN" ? "selected" : ""}>ADMIN</option>
          </select>
          <button class="save-role" data-id="${id}" disabled>Opslaan</button>
        </div>
      </div>
    `);
        return;
    }

    // Opslaan wachtwoord
    if (e.target.matches(".save-password")) {
        e.stopPropagation();
        const id = e.target.dataset.id;
        const slot = e.target.closest(".inline-slot");
        const pw1 = slot.querySelector(".pw1").value.trim();

        const res = await apiFetch(`/gebruiker/${id}/wachtwoord`, {
            method: "PUT",
            body: JSON.stringify({wachtwoord: pw1})
        });

        if (res.ok) {
            clearInline(slot);
            alert("Wachtwoord aangepast.");
        } else if (res.status === 401) {
            window.location.href = "/index.html";
        } else if (res.status === 403) {
            alert("Geen rechten om wachtwoord te wijzigen.");
        } else {
            alert("Reset mislukt.");
        }
        return;
    }

    // Opslaan rol
    if (e.target.matches(".save-role")) {
        e.stopPropagation();
        const id = e.target.dataset.id;
        const slot = e.target.closest(".inline-slot");
        const select = slot.querySelector(".role-select");
        const newRole = select.value.toUpperCase();

        const res = await apiFetch(`/gebruiker/${id}/rol`, {
            method: "PUT",
            body: JSON.stringify({rol: newRole})
        });

        if (res.ok) {
            // update pill + data-current op badge
            const row = e.target.closest("tr");
            row.querySelector(".role-pill").textContent = newRole;
            const roleBadge = row.querySelector(".act-role");
            if (roleBadge) roleBadge.dataset.current = newRole;

            clearInline(slot);
            alert("Rol opgeslagen.");
        } else if (res.status === 401) {
            window.location.href = "/index.html";
        } else if (res.status === 403) {
            alert("Geen rechten om rol te wijzigen.");
        } else {
            alert("Rol wijzigen mislukt.");
        }
    }
});

/* ============================================================================
 * Live validatie (wachtwoord en rol)
 * ==========================================================================*/

document.addEventListener("input", (e) => {
    // Password validatie (rij-popover)
    if (e.target.matches(".pw1, .pw2")) {
        const pop = e.target.closest(".popover");
        if (!pop) return;

        const pw1 = pop.querySelector(".pw1").value.trim();
        const pw2 = pop.querySelector(".pw2").value.trim();
        const btn = pop.querySelector(".save-password");
        const msg = pop.querySelector(".pw-msg");

        if (!pw1 && !pw2) {
            if (btn) btn.disabled = true;
            setMsg(msg, null, "");
            return;
        }
        if (pw1.length < 6) {
            if (btn) btn.disabled = true;
            setMsg(msg, "error", "Wachtwoord is te kort (minimaal 6 tekens).");
            return;
        }
        if (pw1 !== pw2) {
            if (btn) btn.disabled = true;
            setMsg(msg, "error", "Wachtwoorden komen niet overeen.");
            return;
        }
        if (btn) btn.disabled = false;
        setMsg(msg, "ok", "OK ✔");
    }
});


document.addEventListener("change", (e) => {
    // Rol wijziging
    if (e.target.matches(".role-select")) {
        const select = e.target;
        const initial = (select.dataset.initial || "").toUpperCase();
        const current = select.value.toUpperCase();
        const btn = select.closest(".popover").querySelector(".save-role");
        btn.disabled = (current === initial);
    }
});

/* ============================================================================
 * Nieuwe gebruiker (popover bij de knop)
 * ==========================================================================*/

// Popover-slot naast de knop in de admin-header
function getCreateSlot() {
    const head = document.querySelector("#adminBlok .adminHead");
    if (!head) return null;
    let slot = head.querySelector(".create-slot");
    if (!slot) {
        slot = document.createElement("div");
        slot.className = "inline-slot create-slot"; // hergebruikt popover styles
        head.appendChild(slot);
    }
    return slot;
}

// Open/close + opslaan/annuleren
document.addEventListener("click", async (e) => {
    // Open popover
    if (e.target.matches("#btnNieuweUser")) {
        e.stopPropagation();
        const slot = getCreateSlot();
        if (!slot) return;

        openPopover(slot, "create", `
      <div class="popover"">
        <div class="row" style="grid-template-columns: 120px 1fr;">
          <label>Gebruikersnaam</label>
          <input type="text" class="nu-name" placeholder="bv. jan" />
        </div>
        <div class="row" style="grid-template-columns: 120px 1fr;">
          <label>Wachtwoord</label>
          <input type="password" class="nu-pass1" placeholder="min. 6 tekens" />
        </div>
        <div class="row" style="grid-template-columns: 120px 1fr;">
          <label>Herhaal</label>
          <input type="password" class="nu-pass2" placeholder="herhaal" />
        </div>
        <div class="row" style="grid-template-columns: 120px 1fr;">
          <label>Rol</label>
          <select class="nu-role">
            <option selected>USER</option>
            <option>ADMIN</option>
          </select>
        </div>

        <div class="actions" style="margin-top:8px;">
          <button class="nu-save" disabled>Opslaan</button>
          <button class="nu-cancel danger" type="button">Annuleren</button>
        </div>

        <!-- 👇 feedbackregel -->
        <p class="form-msg nu-msg hidden" aria-live="polite" style="margin-top:6px;"></p>
      </div>
    `);
        return;
    }

    // Annuleren
    if (e.target.matches(".nu-cancel")) {
        const slot = e.target.closest(".inline-slot");
        clearInline(slot);
        return;
    }

    // Opslaan (POST /gebruiker)
    if (e.target.matches(".nu-save")) {
        e.stopPropagation();
        const slot = e.target.closest(".inline-slot");
        const name = slot.querySelector(".nu-name").value.trim();
        const p1 = slot.querySelector(".nu-pass1").value.trim();
        const role = (slot.querySelector(".nu-role").value || "USER").toUpperCase();

        const res = await apiFetch("/gebruiker", {
            method: "POST",
            body: JSON.stringify({gebruikersnaam: name, wachtwoord: p1, rol: role})
        });

        if (res.ok) {
            clearInline(slot);
            await loadUsersForAdmin();
        } else if (res.status === 400) {
            alert("Validatiefout bij aanmaken (controleer velden).");
        } else if (res.status === 409) {
            alert("Gebruikersnaam bestaat al.");
        } else if (res.status === 401) {
            window.location.href = "/index.html";
        } else if (res.status === 403) {
            alert("Je hebt geen rechten om een gebruiker aan te maken.");
        } else {
            alert("Aanmaken mislukt.");
        }
        return;
    }
});

// Live validatie in create-popover
document.addEventListener("input", (e) => {
    if (e.target.matches(".nu-name, .nu-pass1, .nu-pass2, .nu-role")) {
        const slot = e.target.closest(".inline-slot");
        if (!slot) return;
        const name = slot.querySelector(".nu-name").value.trim();
        const p1 = slot.querySelector(".nu-pass1").value.trim();
        const p2 = slot.querySelector(".nu-pass2").value.trim();
        const btn = slot.querySelector(".nu-save");
        const valid = name.length >= 2 && p1.length >= 6 && p1 === p2;
        btn.disabled = !valid;
    }
});

async function loadMyProfile() {
    const f = document.getElementById("formProfiel");
    if (!f) return;

    // Optie A: endpoint dat de ingelogde user geeft (heb je al): /gebruiker/admin
    const res = await apiFetch("/gebruiker/admin");
    if (res.status === 401) {
        window.location.href = "/index.html";
        return;
    }
    if (!res.ok) {
        console.warn("Kon profiel niet laden");
        return;
    }

    const u = await res.json();
    f.id.value = u.id;
    f.gebruikersnaam.value = u.gebruikersnaam;
    f.rol.value = (u.rol || "").toUpperCase();

    // Handig om localStorage in sync te houden:
    localStorage.setItem("gebruikerId", String(u.id));
    localStorage.setItem("gebruiker", u.gebruikersnaam);
    localStorage.setItem("rol", u.rol?.toUpperCase() || "USER");
}

/* ============================================================================
 * Init
 * ==========================================================================*/

loadUsersForAdmin();
loadMyProfile();
