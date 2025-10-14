"use strict";

/* ───────────────────────── Helpers ───────────────────────── */
const qs  = (s) => document.querySelector(s);
const qsa = (s) => [...document.querySelectorAll(s)];

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
            if (data?.error)   msg = data.error;
        } catch {}
        throw new Error(msg);
    }
    if (res.status === 204) return null;
    const ct = res.headers.get("content-type") || "";
    return ct.includes("application/json") ? res.json() : res.text();
}

function escapeHtml(s) { return String(s).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
function escapeAttr(s) { return escapeHtml(s); }

/* ────────────────────── DOM refs / state ─────────────────── */
const tbody      = qs("#tbodyKlanten");
const btnNieuwe  = qs("#btnNieuweKlant");
const zoek       = qs("#inpZoek");
const btnLogout  = qs("#btnLogout");

let isAdmin = false;
let klanten = [];

let openSlot = null; // 1 popover tegelijk

/* ───────────────────── Popover manager ───────────────────── */
function clearInline(slot) {
    if (!slot) return;
    slot.innerHTML = "";
    slot.dataset.mode = "";
    if (openSlot === slot) openSlot = null;
}

function openPopover(slot, mode, html) {
    if (openSlot && openSlot !== slot) clearInline(openSlot);
    if (slot.dataset.mode === mode) { clearInline(slot); return; }
    slot.innerHTML = html;
    slot.dataset.mode = mode;
    openSlot = slot;
}

// klik-buiten => sluiten
document.addEventListener("click", (e) => {
    if (!openSlot) return;
    const pop = openSlot.querySelector(".popover");
    const onPopover = pop && pop.contains(e.target);
    const onBadges  = openSlot.previousElementSibling?.contains(e.target);
    if (!onPopover && !onBadges) clearInline(openSlot);
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
    if (btnNieuwe) btnNieuwe.addEventListener("click", openCreatePopover);
    if (zoek) zoek.addEventListener("input", render);

    // 3) data
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
        k.naam.toLowerCase().includes(q) || k.email.toLowerCase().includes(q)
    );

    if (!list.length) {
        tbody.innerHTML = `<tr><td colspan="3">Geen klanten gevonden.</td></tr>`;
        return;
    }

    tbody.innerHTML = list.map(rowHtml).join("");
    tbody.querySelectorAll("[data-act]").forEach(el => el.addEventListener("click", onRowAction));
}

function rowHtml(k) {
    const actions = isAdmin
        ? `
      <div class="action-list">
        <span class="action-link" data-act="edit" data-id="${k.id}">Bewerk</span>
        <span class="action-link danger" data-act="delete" data-id="${k.id}">Verwijder</span>
      </div>
      <div class="inline-slot" id="slot-${k.id}"></div>
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

    if (act === "edit")   openEditPopover(id);
    if (act === "delete") onDelete(id);
}

function openEditPopover(id) {
    const k = klanten.find(x => x.id === id);
    const slot = qs(`#slot-${id}`);
    if (!slot) return;

    openPopover(slot, "edit", `
    <div class="popover">
      <div class="row" style="grid-template-columns: 80px 1fr;">
        <label>Naam</label>
        <input type="text" id="edit-naam-${id}" value="${escapeAttr(k.naam)}">
      </div>
      <div class="row" style="grid-template-columns: 80px 1fr;">
        <label>Email</label>
        <input type="text" id="edit-email-${id}" value="${escapeAttr(k.email)}">
      </div>
      <div class="row" style="grid-template-columns: 1fr auto auto;">
        <div></div>
        <button type="button" class="btn-cancel" data-id="${id}">Annuleren</button>
        <button type="button" class="btn-save" data-id="${id}">Opslaan</button>
      </div>
      <p id="msg-${id}" class="form-msg" style="margin-top:6px;"></p>
    </div>
  `);

    slot.querySelector(".btn-cancel").addEventListener("click", () => clearInline(slot));
    slot.querySelector(".btn-save").addEventListener("click", () => onSave(id));
}

async function onSave(id) {
    const naam = qs(`#edit-naam-${id}`).value.trim();
    const email = qs(`#edit-email-${id}`).value.trim();
    const msg = qs(`#msg-${id}`);

    if (!naam || !email) { setMsg(msg, "error", "Naam en e-mail zijn verplicht."); return; }

    const ok = window.confirm(`Wijzigingen opslaan voor “${naam}”?`);
    if (!ok) return;

    try {
        await apiFetch(`/klanten/${id}`, { method: "PUT", body: JSON.stringify({ id, naam, email }) });
        const k = klanten.find(x => x.id === id);
        k.naam = naam; k.email = email;
        setMsg(msg, "ok", "Opgeslagen.");
        setTimeout(() => { clearInline(qs(`#slot-${id}`)); render(); }, 120);
    } catch (err) {
        setMsg(msg, "error", err.message || "Opslaan mislukt.");
    }
}

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

/* ────────────────── Create-popover (header) ───────────────── */
function openCreatePopover() {
    if (!isAdmin) return;
    const slot = qs("#adminBlok .create-slot");
    openPopover(slot, "create", `
    <div class="popover" style="right:0; top:48px; width:420px;">
      <div class="row" style="grid-template-columns: 100px 1fr;">
        <label>Naam</label>
        <input type="text" id="new-naam">
      </div>
      <div class="row" style="grid-template-columns: 100px 1fr;">
        <label>Email</label>
        <input type="text" id="new-email">
      </div>
      <div class="row" style="grid-template-columns: 1fr auto auto;">
        <div></div>
        <button type="button" class="btn-cancel-create">Annuleren</button>
        <button type="button" class="btn-create">Toevoegen</button>
      </div>
      <p id="createMsg" class="form-msg" style="margin-top:6px;"></p>
    </div>
  `);

    slot.querySelector(".btn-cancel-create").addEventListener("click", () => clearInline(slot));
    slot.querySelector(".btn-create").addEventListener("click", onCreate);
}

async function onCreate() {
    const slot = qs("#adminBlok .create-slot");
    const naam = qs("#new-naam").value.trim();
    const email = qs("#new-email").value.trim();
    const msg = qs("#createMsg");

    if (!naam || !email) { setMsg(msg, "error", "Naam en e-mail zijn verplicht."); return; }
    if (!window.confirm(`Klant “${naam}” toevoegen?`)) return;

    try {
        const id = await apiFetch("/klanten", { method: "POST", body: JSON.stringify({ naam, email }) });
        klanten.push({ id, naam, email });
        clearInline(slot);
        render();
    } catch (err) {
        setMsg(msg, "error", err.message || "Toevoegen mislukt.");
    }
}

/* ───────────────────── Small UI helper ───────────────────── */
function setMsg(el, type, text) {
    if (!el) return;
    el.textContent = text || "";
    el.classList.remove("error", "ok");
    if (!text) return;
    if (type === "error") el.classList.add("error");
    if (type === "ok")    el.classList.add("ok");
}
