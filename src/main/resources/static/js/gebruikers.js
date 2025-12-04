"use strict";

/* ============================================================================
 * Helpers
 * ==========================================================================*/
function qs(selector) {
    return document.querySelector(selector);
}

const btnLogout = qs("#btnLogout");

async function apiFetch(url, options = {}) {
    return fetch(url, {
        credentials: "include",
        headers: {"Content-Type": "application/json"},
        ...options,
    });
}

function getCurrentUserRole() {
    return localStorage.getItem("rol"); // gezet door login.js
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
 * Globale modal
 * ==========================================================================*/

const modalOverlay = qs("#modalOverlay");
const modalTitle   = qs("#modalTitle");
const modalBody    = qs("#modalBody");
const modalCancel  = qs("#modalCancel");
const modalOk      = qs("#modalOk");

let modalOnOk = null;

function closeModal() {
    if (!modalOverlay) return;
    modalOverlay.classList.add("hidden");
    if (modalBody)  modalBody.innerHTML = "";
    if (modalTitle) modalTitle.textContent = "";
    modalOnOk = null;
    if (modalOk) modalOk.disabled = false; // reset knop
}

function openModal({ title, bodyHtml, okText = "Opslaan", showOk = true, onOk }) {
    if (!modalOverlay) return;

    if (modalTitle) modalTitle.textContent = title || "";
    if (modalBody)  modalBody.innerHTML = bodyHtml || "";

    if (modalOk) {
        modalOk.textContent = okText;
        modalOk.style.display = showOk ? "" : "none";
        modalOk.disabled = false; // wordt evt. door validatie uitgezet
    }

    modalOnOk = onOk || null;
    modalOverlay.classList.remove("hidden");
}

if (modalCancel) {
    modalCancel.addEventListener("click", closeModal);
}
if (modalOverlay) {
    modalOverlay.addEventListener("click", (e) => {
        if (e.target === modalOverlay) {
            closeModal();
        }
    });
}
if (modalOk) {
    modalOk.addEventListener("click", async () => {
        if (typeof modalOnOk === "function") {
            await modalOnOk();
        }
    });
}

/* ============================================================================
 * Realtime wachtwoord-validatie in modal
 * (hergebruikt voor "wachtwoord wijzigen" én "nieuwe gebruiker")
 * ==========================================================================*/
function enableModalPasswordValidation() {
    const pw1  = document.getElementById("modalPw1");
    const pw2  = document.getElementById("modalPw2");
    const msg  = document.getElementById("modalPwMsg"); // LET OP id
    const okBtn = document.getElementById("modalOk");

    if (!pw1 || !pw2 || !okBtn || !msg) return;

    function validate() {
        const v1 = pw1.value.trim();
        const v2 = pw2.value.trim();

        if (!v1 && !v2) {
            okBtn.disabled = true;
            setMsg(msg, null, "");
            return;
        }
        if (v1.length < 6) {
            okBtn.disabled = true;
            setMsg(msg, "error", "Wachtwoord is te kort (minimaal 6 tekens).");
            return;
        }
        if (v1 !== v2) {
            okBtn.disabled = true;
            setMsg(msg, "error", "Wachtwoorden komen niet overeen.");
            return;
        }

        okBtn.disabled = false;
        setMsg(msg, "ok", "OK ✔");
    }

    okBtn.disabled = true;    // start: uit
    setMsg(msg, null, "");    // start: leeg

    pw1.addEventListener("input", validate);
    pw2.addEventListener("input", validate);
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
        try {
            await apiFetch("/auth/logout", { method: "POST" });
        } finally {
            location.href = "index.html";
        }
    });
}

/* ============================================================================
 * Eigen wachtwoord wijzigen (iedereen)
 * ==========================================================================*/

const passwordForm = qs("#formWachtwoord");

if (passwordForm) {
    const msgEl = document.getElementById("pwFeedback");
    const btn   = passwordForm.querySelector('button[type="submit"]');

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
            body: JSON.stringify({ wachtwoord: newPassword }),
        });

        if (response.ok) {
            alert("Wachtwoord gewijzigd.");
            passwordForm.reset();
            setMsg(msgEl, null, "");
            if (btn) btn.disabled = true;
            validateOwnPw();
        } else if (response.status === 401) {
            window.location.href = "/index.html";
        } else if (response.status === 403) {
            alert("Geen rechten om dit te wijzigen.");
        } else {
            alert("Wijzigen mislukt.");
        }
    });
}

/* ============================================================================
 * Profiel (mijn gebruikersnaam)
 * ==========================================================================*/

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

    profielForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const naam = profielForm.gebruikersnaam.value.trim();
        if (naam.length < 2) {
            setPMsg("error", "Gebruikersnaam is te kort.");
            return;
        }

        const r = await fetch("/gebruiker/mijn-naam", {
            method: "PUT",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ gebruikersnaam: naam }),
        });

        if (r.ok) {
            setPMsg("ok", "Profiel opgeslagen.");
            localStorage.setItem("gebruiker", naam);
            await loadUsersForAdmin(); // als je admin bent, refresht dit de tabel
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
    const tableBody  = qs("#usersTbody");

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
      </td>
    `;
        tableBody.appendChild(row);
    }
}

/* ============================================================================
 * Admin-acties via MODAL
 * ==========================================================================*/

document.addEventListener("click", async (e) => {
    /* --- Verwijderen --- */
    if (e.target.matches(".act-delete")) {
        e.stopPropagation();
        if (document.body.dataset.deleting === "1") return;
        document.body.dataset.deleting = "1";

        const id = e.target.dataset.id;
        try {
            if (!confirm("Gebruiker verwijderen?")) return;
            const resp = await apiFetch(`/gebruiker/${id}`, { method: "DELETE" });
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

    /* --- Wachtwoord wijzigen (andere gebruiker) --- */
    if (e.target.matches(".act-password")) {
        e.stopPropagation();
        const id = e.target.dataset.id;

        openModal({
            title: `Wachtwoord wijzigen (id ${id})`,
            bodyHtml: `
        <div class="stack">
          <label for="modalPw1">Nieuw wachtwoord</label>
          <input id="modalPw1" type="password" placeholder="min. 6 tekens">

          <label for="modalPw2">Herhaal wachtwoord</label>
          <input id="modalPw2" type="password" placeholder="herhaal">
        </div>
        <p id="modalPwMsg" class="form-msg" aria-live="polite"></p>
      `,
            okText: "Opslaan",
            onOk: async () => {
                const pw1 = qs("#modalPw1").value.trim();
                const pw2 = qs("#modalPw2").value.trim();
                const msg = qs("#modalPwMsg");

                if (!pw1 || !pw2) {
                    setMsg(msg, "error", "Beide velden zijn verplicht.");
                    return;
                }
                if (pw1.length < 6) {
                    setMsg(msg, "error", "Wachtwoord is te kort (minimaal 6 tekens).");
                    return;
                }
                if (pw1 !== pw2) {
                    setMsg(msg, "error", "Wachtwoorden komen niet overeen.");
                    return;
                }

                const res = await apiFetch(`/gebruiker/${id}/wachtwoord`, {
                    method: "PUT",
                    body: JSON.stringify({ wachtwoord: pw1 }),
                });

                if (res.ok) {
                    alert("Wachtwoord aangepast.");
                    closeModal();
                } else if (res.status === 401) {
                    window.location.href = "/index.html";
                } else if (res.status === 403) {
                    setMsg(msg, "error", "Geen rechten om wachtwoord te wijzigen.");
                } else {
                    setMsg(msg, "error", "Opslaan mislukt.");
                }
            },
        });

        // 🔄 realtime validatie activeren
        enableModalPasswordValidation();
        return;
    }

    /* --- Rol wijzigen --- */
    if (e.target.matches(".act-role")) {
        e.stopPropagation();
        const id = e.target.dataset.id;
        const current = (e.target.dataset.current || "USER").toUpperCase();
        const row = e.target.closest("tr");

        openModal({
            title: `Rol wijzigen (id ${id})`,
            bodyHtml: `
        <div class="stack">
          <label for="modalRole">Rol</label>
          <select id="modalRole">
            <option value="USER">USER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>
        <p id="modalMsg" class="form-msg" aria-live="polite"></p>
      `,
            okText: "Opslaan",
            onOk: async () => {
                const select = qs("#modalRole");
                const msg = qs("#modalMsg");
                let newRole = (select.value || "USER").toUpperCase();

                if (newRole === current) {
                    closeModal();
                    return;
                }

                const res = await apiFetch(`/gebruiker/${id}/rol`, {
                    method: "PUT",
                    body: JSON.stringify({ rol: newRole }),
                });

                if (res.ok) {
                    const pill = row.querySelector(".role-pill");
                    if (pill) pill.textContent = newRole;
                    const roleLink = row.querySelector(".act-role");
                    if (roleLink) roleLink.dataset.current = newRole;

                    closeModal();
                } else if (res.status === 401) {
                    window.location.href = "/index.html";
                } else if (res.status === 403) {
                    setMsg(msg, "error", "Geen rechten om rol te wijzigen.");
                } else {
                    setMsg(msg, "error", "Rol wijzigen mislukt.");
                }
            },
        });

        const roleSelect = qs("#modalRole");
        if (roleSelect) roleSelect.value = current;

        return;
    }

    /* --- Nieuwe gebruiker --- */
    if (e.target.matches("#btnNieuweUser")) {
        e.stopPropagation();

        openModal({
            title: "Nieuwe gebruiker",
            bodyHtml: `
        <div class="stack">
          <label for="modalName">Gebruikersnaam</label>
          <input id="modalName" type="text" placeholder="bv. jan">

          <label for="modalPw1">Wachtwoord</label>
          <input id="modalPw1" type="password" placeholder="min. 6 tekens">

          <label for="modalPw2">Herhaal wachtwoord</label>
          <input id="modalPw2" type="password" placeholder="herhaal">

          <label for="modalRole">Rol</label>
          <select id="modalRole">
            <option value="USER" selected>USER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>
        <p id="modalPwMsg" class="form-msg" aria-live="polite"></p>
      `,
            okText: "Opslaan",
            onOk: async () => {
                const name = qs("#modalName").value.trim();
                const pw1  = qs("#modalPw1").value.trim();
                const pw2  = qs("#modalPw2").value.trim();
                const role = (qs("#modalRole").value || "USER").toUpperCase();
                const msg  = qs("#modalPwMsg");

                if (name.length < 2) {
                    setMsg(msg, "error", "Gebruikersnaam is te kort (min. 2 tekens).");
                    return;
                }
                if (pw1.length < 6) {
                    setMsg(msg, "error", "Wachtwoord is te kort (minimaal 6 tekens).");
                    return;
                }
                if (pw1 !== pw2) {
                    setMsg(msg, "error", "Wachtwoorden komen niet overeen.");
                    return;
                }

                const res = await apiFetch("/gebruiker", {
                    method: "POST",
                    body: JSON.stringify({
                        gebruikersnaam: name,
                        wachtwoord: pw1,
                        rol: role,
                    }),
                });

                if (res.ok) {
                    closeModal();
                    await loadUsersForAdmin();
                } else if (res.status === 400) {
                    setMsg(msg, "error", "Validatiefout (controleer de velden).");
                } else if (res.status === 409) {
                    setMsg(msg, "error", "Gebruikersnaam bestaat al.");
                } else if (res.status === 401) {
                    window.location.href = "/index.html";
                } else if (res.status === 403) {
                    setMsg(msg, "error", "Je hebt geen rechten om een gebruiker aan te maken.");
                } else {
                    setMsg(msg, "error", "Aanmaken mislukt.");
                }
            },
        });

        // 🔄 ook hier realtime paswoord-check
        enableModalPasswordValidation();
        return;
    }
});

/* ============================================================================
 * Profiel van ingelogde gebruiker laden
 * ==========================================================================*/

async function loadMyProfile() {
    const f = document.getElementById("formProfiel");
    if (!f) return;

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

    localStorage.setItem("gebruikerId", String(u.id));
    localStorage.setItem("gebruiker", u.gebruikersnaam);
    localStorage.setItem("rol", u.rol?.toUpperCase() || "USER");
}

/* ============================================================================
 * Init
 * ==========================================================================*/

loadUsersForAdmin();
loadMyProfile();
