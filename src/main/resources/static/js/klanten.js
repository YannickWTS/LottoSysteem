"use strict";

/* ───────────────────────── Helpers ───────────────────────── */
const qs  = (s) => document.querySelector(s);
const qsa = (s) => [...document.querySelectorAll(s)];

class ApiError extends Error {
    constructor(status, message) {
        super(message);
        this.status = status;
    }
}

async function apiFetch(url, options = {}) {
    const opts = {
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        ...options
    };
    const res = await fetch(url, opts);

    if (!res.ok) {
        let msg = `HTTP ${res.status}`;
        try {
            const data = await res.json();
            if (data?.message) msg = data.message;
            else if (data?.error) msg = data.error;
        } catch {}
        throw new ApiError(res.status, msg);
    }

    if (res.status === 204) return null;
    const ct = res.headers.get("content-type") || "";
    return ct.includes("application/json") ? res.json() : res.text();
}

function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, c => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "\"": "&quot;",
        "'": "&#39;"
    }[c]));
}
function escapeAttr(s) { return escapeHtml(s); }

function setMsg(el, type, text) {
    if (!el) return;
    el.textContent = text || "";
    el.classList.remove("error", "ok");
    if (!text) return;
    if (type === "error") el.classList.add("error");
    if (type === "ok")    el.classList.add("ok");
}

/* ────────────────────── DOM refs / state ─────────────────── */
const tbody     = qs("#tbodyKlanten");
const btnNieuwe = qs("#btnNieuweKlant");
const zoek      = qs("#inpZoek");
const btnLogout = qs("#btnLogout");

// modal refs
const modalOverlay = qs("#modalOverlay");
const modalTitle   = qs("#modalTitle");
const modalBody    = qs("#modalBody");
const modalCancel  = qs("#modalCancel");
const modalOk      = qs("#modalOk");

let modalOkHandler = null;

let isAdmin = false;
let klanten = [];

/* ───────────────────── Modal helpers ─────────────────────── */
function closeModal() {
    modalOverlay.classList.add("hidden");
    modalBody.innerHTML = "";
    modalTitle.textContent = "";
    modalOkHandler = null;
}

function openModal({ title, bodyHtml, okText = "Opslaan", showCancel = true, onOk }) {
    modalTitle.textContent = title;
    modalBody.innerHTML = bodyHtml;
    modalOk.textContent = okText;
    modalCancel.style.display = showCancel ? "" : "none";
    modalOverlay.classList.remove("hidden");
    modalOkHandler = onOk || null;
}

// klik op achtergrond => sluiten
modalOverlay?.addEventListener("click", (e) => {
    if (e.target === modalOverlay) {
        closeModal();
    }
});

// knoppen in modal
modalCancel?.addEventListener("click", () => closeModal());
modalOk?.addEventListener("click", async () => {
    if (!modalOkHandler) return;
    await modalOkHandler();
});

// ESC sluit modal
document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !modalOverlay.classList.contains("hidden")) {
        closeModal();
    }
});

/* ───────────────────────── Init ──────────────────────────── */
init();

async function init() {
    // 1) sessie/rol
    try {
        const me = await apiFetch("/gebruiker/admin"); // {id, gebruikersnaam, rol}
        isAdmin = String(me.rol || "").toUpperCase() === "ADMIN";
    } catch {
        location.href = "index.html";
        return;
    }

    // 2) events
    if (btnLogout) {
        btnLogout.addEventListener("click", async () => {
            const ok = window.confirm("Afmelden en terug naar login?");
            if (!ok) return;
            try { await apiFetch("/auth/logout", { method: "POST" }); }
            finally { location.href = "index.html"; }
        });
    }

    if (btnNieuwe) {
        btnNieuwe.addEventListener("click", openCreateModal);
    }

    if (zoek) {
        zoek.addEventListener("input", render);
    }

    // 3) data laden
    await load();
}

async function load() {
    klanten = await apiFetch("/klanten");
    render();
}

/* ────────────────────── Tabel rendering ──────────────────── */
function render() {
    const q = (zoek?.value || "").toLowerCase();
    const list = (klanten || []).filter(k =>
        (k.naam || "").toLowerCase().includes(q)
    );

    if (!list.length) {
        tbody.innerHTML = `<tr><td colspan="3">Geen klanten gevonden.</td></tr>`;
        return;
    }

    tbody.innerHTML = list.map(rowHtml).join("");

    // delegatie per rij
    tbody.querySelectorAll("[data-act]").forEach(el =>
        el.addEventListener("click", onRowAction)
    );
}

function rowHtml(k) {
    const actions = isAdmin
        ? `
      <div class="action-list">
        <span class="action-link" data-act="edit" data-id="${k.id}">Bewerk</span>
        <span class="action-link danger" data-act="delete" data-id="${k.id}">Verwijder</span>
      </div>
    `
        : `<em style="color:var(--muted)">—</em>`;

    return `
    <tr>
      <td>${escapeHtml(k.naam)}</td>
      <td>${escapeHtml(k.email)}</td>
      <td class="actions-cell">${actions}</td>
    </tr>
  `;
}

/* ────────────────────── Rij-acties ───────────────────────── */
function onRowAction(e) {
    const act = e.currentTarget.dataset.act;
    const id  = Number(e.currentTarget.dataset.id);
    if (!act || !id) return;

    if (act === "edit")   openEditModal(id);
    if (act === "delete") onDelete(id);
}

/* ───────────── Modal: klant bewerken ─────────────────────── */
function openEditModal(id) {
    const k = klanten.find(x => x.id === id);
    if (!k) return;

    openModal({
        title: "Klant bewerken",
        okText: "Opslaan",
        bodyHtml: `
        <div class="form-col">
            <label>Naam
                <input type="text" id="modalNaam" value="${escapeAttr(k.naam)}">
            </label>
            <label>Email
                <input type="text" id="modalEmail" value="${escapeAttr(k.email)}">
            </label>
            <p id="modalMsg" class="form-msg" style="margin-top:6px;"></p>
        </div>
        `,
        onOk: async () => {
            const naam  = qs("#modalNaam").value.trim();
            const email = qs("#modalEmail").value.trim();
            const msg   = qs("#modalMsg");

            if (!naam || !email) {
                setMsg(msg, "error", "Naam en e-mail zijn verplicht.");
                return;
            }

            try {
                await apiFetch(`/klanten/${id}`, {
                    method: "PUT",
                    body: JSON.stringify({ id, naam, email })
                });

                // lokaal bijwerken
                k.naam = naam;
                k.email = email;

                closeModal();
                render();
            } catch (err) {
                if (err.status === 409) {
                    setMsg(msg, "error", "E-mail bestaat al bij een andere klant.");
                } else {
                    setMsg(msg, "error", err.message || "Opslaan mislukt.");
                }
            }
        }
    });
}

/* ───────────── Modal: nieuwe klant ───────────────────────── */
function openCreateModal() {
    if (!isAdmin) return;

    openModal({
        title: "Nieuwe klant",
        okText: "Toevoegen",
        bodyHtml: `
        <div class="form-col">
            <label>Naam
                <input type="text" id="modalNaam">
            </label>
            <label>Email
                <input type="text" id="modalEmail">
            </label>
            <p id="modalMsg" class="form-msg" style="margin-top:6px;"></p>
        </div>
        `,
        onOk: async () => {
            const naam  = qs("#modalNaam").value.trim();
            const email = qs("#modalEmail").value.trim();
            const msg   = qs("#modalMsg");

            if (!naam || !email) {
                setMsg(msg, "error", "Naam en e-mail zijn verplicht.");
                return;
            }

            try {
                const id = await apiFetch("/klanten", {
                    method: "POST",
                    body: JSON.stringify({ naam, email })
                });

                klanten.push({ id, naam, email });
                closeModal();
                render();
            } catch (err) {
                if (err.status === 409) {
                    setMsg(msg, "error", "E-mail bestaat al bij een andere klant.");
                } else {
                    setMsg(msg, "error", err.message || "Toevoegen mislukt.");
                }
            }
        }
    });
}

/* ───────────── Verwijderen ───────────────────────────────── */
async function onDelete(id) {
    const k = klanten.find(x => x.id === id);
    if (!window.confirm(`Klant “${k?.naam ?? id}” verwijderen?`)) return;

    try {
        await apiFetch(`/klanten/${id}`, { method: "DELETE" });
        klanten = klanten.filter(x => x.id !== id);
        render();
    } catch (err) {
        alert(err.message || "Verwijderen mislukt.");
    }
}
