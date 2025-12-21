"use strict";

/* ============================================================================
 * Helpers
 * ==========================================================================*/
const qs = (s) => document.querySelector(s);
const api = (url, opt = {}) =>
    fetch(url, {
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        ...opt,
    });

const isAdmin = () => (localStorage.getItem("rol") || "").toUpperCase() === "ADMIN";
const getUserId = () => Number(localStorage.getItem("gebruikerId") || 0);

function setMsg(el, type, text) {
    if (!el) return;
    el.textContent = text || "";
    el.classList.remove("hidden", "ok", "error");
    if (!text) {
        el.classList.add("hidden");
        return;
    }
    if (type === "ok") el.classList.add("ok");
    if (type === "error") el.classList.add("error");
}

// "oktober 2025" (nl-BE, lowercase)
function monthNameNlBE(date = new Date()) {
    return new Intl.DateTimeFormat("nl-BE", { month: "long", year: "numeric" })
        .format(date)
        .toLowerCase();
}

// Vul de maand-select met huidige maand + 2 volgende maanden
function buildMaandOptions() {
    if (!inpMaand) return;
    const now = new Date();
    inpMaand.innerHTML = "";

    for (let i = 0; i < 3; i++) {
        const d = new Date(now.getFullYear(), now.getMonth() + i, 1);
        const label = monthNameNlBE(d); // bv "november 2025"

        const opt = document.createElement("option");
        opt.value = label;
        opt.textContent = label;
        if (i === 0) opt.selected = true; // standaard = huidige maand

        inpMaand.appendChild(opt);
    }
}

// Format ISO LocalDateTime -> "DD/MM/YYYY HH:MM" (nl-BE, zonder seconden)
function fmtLocalDateTimeToNl(d) {
    if (!d) return "";
    const dt = new Date(d);

    // fallback als parsing faalt (zou normaal niet gebeuren)
    if (Number.isNaN(dt.getTime())) return String(d);

    return dt.toLocaleString("nl-BE", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
        // seconds en ms niet tonen
    });
}

const NL_MONTHS = [
    "januari", "februari", "maart", "april", "mei", "juni",
    "juli", "augustus", "september", "oktober", "november", "december"
];

function maandKey(str) {
    if (!str) return 0;
    const [mnaam, j] = String(str).trim().split(/\s+/);
    const y = Number(j || 0);
    const m = NL_MONTHS.indexOf((mnaam || "").toLowerCase()) + 1;
    return y * 100 + (m || 0); // 202510 etc.
}

function cmpStr(a, b) { return String(a || "").localeCompare(String(b || ""), "nl", { sensitivity: "base" }); }
function cmpNum(a, b) { return (Number(a) || 0) - (Number(b) || 0); }
function cmpBool(a, b) { return (a === b) ? 0 : (a ? 1 : -1); } // Nee < Ja


/* ============================================================================
 * UI refs
 * ==========================================================================*/
const form = qs("#formBestelling");
const selSpel = qs("#selSpel");
const inpMaand = qs("#inpMaand");
const chkBetaald = qs("#chkBetaald");
const btnOpslaan = qs("#btnOpslaan");
const btnLogout = qs("#btnLogout");
const formMsg = qs("#formMsg");
const tbody = qs("#bestelTbody");

// Combobox-onderdelen (klant zoeken)
const klantSearch = qs("#klantSearch");
const klantList = qs("#klantList");

/* ============================================================================
 * State
 * ==========================================================================*/
let bestellingen = [];                 // ruwe lijst uit de API (verrijkt)
let sortState = { col: null, dir: 0 }; // 0 = normaal, 1 = asc, -1 = desc
let klantenCache = [];                 // [{id, label, naam}, ...]
let klantenMap = {};                   // id -> naam
let selectedKlantId = 0;

const medewerkerCache = new Map(); // id -> gebruikersnaam (lazy geladen)

/* ============================================================================
 * Init
 * ==========================================================================*/
qs("#btnRefresh")?.addEventListener("click", loadBestellingen);
qs("#btnTerug")?.addEventListener("click", () => (location.href = "welkom.html"));

if (btnLogout) {
    btnLogout.addEventListener("click", async () => {
        const ok = window.confirm("Afmelden en terug naar login?");
        if (!ok) return;
        try {
            await api("/auth/logout", { method: "POST" });
        } finally {
            location.href = "index.html";
        }
    });
}

// Maand-keuze opbouwen (huidige maand + 2 volgende)
buildMaandOptions();


// ▼ Klanten laden
async function loadKlanten() {
    const r = await api("/klanten");
    if (r.status === 401) { location.href = "/index.html"; return; }
    const klanten = await r.json();

    // label voor tonen, maar filteren doen we op NAAM
    klantenCache = klanten
        .map(k => {
            const displayNaam =
                k.naam
                    ? (k.voornaam ? `${k.voornaam} ${k.naam}` : k.naam).trim()
                    : (k.voornaam || `Klant #${k.id}`);
            const label = k.email ? `${displayNaam} (${k.email})` : displayNaam;
            return {
                id: k.id,
                label,               // tonen
                naam: displayNaam,   // naam
                naamLower: displayNaam.toLowerCase() // filter-key
            };
        })
        .sort((a, b) => a.naam.localeCompare(b.naam, "nl", { sensitivity: "base" }));

    klantenMap = Object.fromEntries(klantenCache.map(k => [k.id, k.naam]));
    renderKlantList("");   // start: leeg tot je typt
    validate();
}

// ▼ Popover-lijst alleen op NAAM filteren
function renderKlantList(filter) {
    const f = filter.trim().toLowerCase();
    const lijst = f
        ? klantenCache.filter(k => k.naamLower.includes(f)) // alleen naam
        : []; // geen filter = lijst dicht/geen items

    klantList.innerHTML = lijst
        .map((k, i) => `<div class="combo-item${i === 0 ? " active" : ""}" data-id="${k.id}">${k.label}</div>`)
        .join("");

    klantList.classList.toggle("hidden", lijst.length === 0);
}

klantSearch.addEventListener("input", (e) => {
    selectedKlantId = 0;
    renderKlantList(e.target.value);
    validate();
});

// Klikselectie
klantList.addEventListener("click", (e) => {
    const item = e.target.closest(".combo-item");
    if (!item) return;
    selectedKlantId = Number(item.dataset.id);
    const k = klantenCache.find((x) => x.id === selectedKlantId);
    klantSearch.value = k ? k.label : "";
    klantList.classList.add("hidden");
    validate();
});

// Keyboard-navigatie ↑/↓/Enter/Escape
klantSearch.addEventListener("keydown", (e) => {
    const items = [...klantList.querySelectorAll(".combo-item")];
    if (items.length === 0) return;
    let idx = items.findIndex((x) => x.classList.contains("active"));
    if (idx < 0) idx = 0;

    if (e.key === "ArrowDown") {
        e.preventDefault();
        const n = items[Math.min(idx + 1, items.length - 1)];
        items.forEach((x) => x.classList.remove("active"));
        n.classList.add("active");
        n.scrollIntoView({ block: "nearest" });
    }
    if (e.key === "ArrowUp") {
        e.preventDefault();
        const p = items[Math.max(idx - 1, 0)];
        items.forEach((x) => x.classList.remove("active"));
        p.classList.add("active");
        p.scrollIntoView({ block: "nearest" });
    }
    if (e.key === "Enter") {
        e.preventDefault();
        const cur = items[idx];
        cur?.click();
    }
    if (e.key === "Escape") {
        klantList.classList.add("hidden");
    }
});

// Click buiten → lijst sluiten
document.addEventListener("click", (e) => {
    if (!klantList.contains(e.target) && e.target !== klantSearch) {
        klantList.classList.add("hidden");
    }
});


/* ============================================================================
 * Validatie
 * ==========================================================================*/
function validate() {
    const ok =
        selectedKlantId > 0 &&
        selSpel.value.trim().length > 0 &&
        inpMaand.value.trim().length > 0;

    btnOpslaan.disabled = !ok;
    setMsg(
        formMsg,
        ok ? "ok" : "error",
        ok ? "Klaar om op te slaan ✔" : "Vul alle velden correct in."
    );
}

selSpel.addEventListener("change", validate);

/* ============================================================================
 * Namen van medewerkers (lazy fetch + cache)
 * ==========================================================================*/
async function getMedewerkerNaam(medewerkerId) {
    if (!medewerkerId) return "";
    if (medewerkerCache.has(medewerkerId)) return medewerkerCache.get(medewerkerId);

    // Probeer een detail-endpoint te lezen; val terug op "#id" bij 403/404
    try {
        const r = await api(`/gebruiker/${medewerkerId}`);
        if (r.ok) {
            const u = await r.json();
            const naam = u.gebruikersnaam || `#${medewerkerId}`;
            medewerkerCache.set(medewerkerId, naam);
            return naam;
        }
        const fallback = `#${medewerkerId}`;
        medewerkerCache.set(medewerkerId, fallback);
        return fallback;
    } catch {
        const fallback = `#${medewerkerId}`;
        medewerkerCache.set(medewerkerId, fallback);
        return fallback;
    }
}

/* ============================================================================
 * Bestellingen laden + renderen (met namen i.p.v. IDs)
 * ==========================================================================*/
async function loadBestellingen() {
    const r = await api("/bestelling");
    if (r.status === 401) { location.href = "/index.html"; return; }
    const lijst = await r.json();

    bestellingen = lijst.map((b, i) => {
        const klantNaam = b.klantNaam || klantenMap[b.klantId] || `#${b.klantId}`;
        const medewerkerNaam = b.medewerkerNaam || (b.medewerkerId ? `#${b.medewerkerId}` : "-");
        return {
            __idx: i,                 // originele positie (voor "normaal")
            id: b.id,
            klantNaam,
            spelType: b.spelType,
            maand: b.maand,
            maandKey: maandKey(b.maand),
            datumIso: String(b.datumRegistratie), // ISO LocalDateTime string
            betaald: !!b.betaald,
            medewerkerNaam,
            raw: b                    // originele record voor acties
        };
    });

    renderBestellingen();
}

function renderBestellingen() {
    const rows = [...bestellingen];

    if (sortState.dir !== 0 && sortState.col) {
        const dir = sortState.dir;
        rows.sort((a, b) => {
            let c = 0;
            switch (sortState.col) {
                case "id":         c = cmpNum(a.id, b.id); break;
                case "klant":      c = cmpStr(a.klantNaam, b.klantNaam); break;
                case "spel":       c = cmpStr(a.spelType, b.spelType); break;
                case "maand":      c = cmpNum(a.maandKey, b.maandKey); break;
                case "datum":      c = cmpStr(a.datumIso, b.datumIso); break; // ISO asc
                case "betaald":    c = cmpBool(a.betaald, b.betaald); break;  // Nee→Ja
                case "medewerker": c = cmpStr(a.medewerkerNaam, b.medewerkerNaam); break;
            }
            return dir * c || cmpNum(a.__idx, b.__idx); // stabiel
        });
    } else {
        rows.sort((a, b) => cmpNum(a.__idx, b.__idx)); // normaal
    }

    // pijltje in header updaten (optioneel, als je .sort-span zou hebben)
    document.querySelectorAll("th.sortable .sort").forEach(s => s.textContent = "");
    if (sortState.col && sortState.dir) {
        const el = document.querySelector(`th.sortable[data-col="${sortState.col}"] .sort`);
        if (el) el.textContent = sortState.dir > 0 ? "▲" : "▼";
    }

    // tbody vullen
    tbody.innerHTML = "";
    for (const r of rows) {
        const b = r.raw;
        const acties = [];
        if (!b.betaald) {
            acties.push(`<span class="action-link act-betaal" data-id="${b.id}">Markeer betaald</span>`);
            if (isAdmin()) {
                acties.push(`<span class="action-link danger act-del" data-id="${b.id}">Verwijder</span>`);
            }
        }
// als b.betaald === true → geen acties behalve "-" in de cel


        const tr = document.createElement("tr");
        tr.innerHTML = `
      <td>${r.klantNaam}</td>
      <td>${r.spelType}</td>
      <td>${r.maand}</td>
      <td>${fmtLocalDateTimeToNl(r.datumIso)}</td>
      <td>${r.betaald ? "Ja" : "Nee"}</td>
      <td>${r.medewerkerNaam}</td>
      <td class="actions-cell"><div class="action-list">${acties.join("") || "-"}</div></td>
    `;
        tbody.appendChild(tr);
    }
}


/* ============================================================================
 * Tabel-acties
 * ==========================================================================*/
document.addEventListener("click", async (e) => {
    // Markeer betaald (toegestaan voor alle users)
    if (e.target.matches(".act-betaal")) {
        const id = e.target.dataset.id;
        if (!confirm(`Bestelling #${id} als betaald markeren?`)) return;

        const r = await api(`/bestelling/${id}/betaald`, {
            method: "PUT",
            body: JSON.stringify({ betaald: true }),
        });

        if (r.ok) {
            await loadBestellingen();
            alert("Gemarkeerd als betaald.");
        } else if (r.status === 401) {
            location.href = "/index.html";
        } else if (r.status === 403) {
            alert("Geen rechten om te wijzigen.");
        } else {
            alert("Wijzigen mislukt.");
        }
        return;
    }

    // Verwijderen (alleen zichtbaar voor admin)
    if (e.target.matches(".act-del")) {
        const id = e.target.dataset.id;
        if (!confirm(`Bestelling #${id} verwijderen?`)) return;

        const r = await api(`/bestelling/${id}`, { method: "DELETE" });
        if (r.ok) {
            await loadBestellingen();
        } else if (r.status === 401) {
            location.href = "/index.html";
        } else if (r.status === 403) {
            alert("Geen rechten om te verwijderen.");
        } else {
            alert("Verwijderen mislukt.");
        }
    }
});

/* ============================================================================
 * Form submit
 * ==========================================================================*/
form.addEventListener("submit", async (e) => {
    e.preventDefault();
    validate();
    if (btnOpslaan.disabled) return;

    const payload = {
        klantId: selectedKlantId,
        spelType: selSpel.value.trim(),
        maand: inpMaand.value.trim(), // "oktober 2025"
        betaald: !!chkBetaald.checked,
    };

    const r = await api("/bestelling", { method: "POST", body: JSON.stringify(payload) });
    if (r.ok) {
        form.reset();
        // Reset UI
        selectedKlantId = 0;
        klantSearch.value = "";
        buildMaandOptions();   // opnieuw huidige + 2 volgende maanden
        validate();
        await loadBestellingen();
        // feedback zonder alert()
        setMsg(formMsg, "ok", "Bestelling opgeslagen ✔");
        setTimeout(() => setMsg(formMsg, "", ""), 2000);

        // focus terug op klant input (helpt Electron)
        requestAnimationFrame(() => klantSearch.focus());

    } else if (r.status === 401) {
        location.href = "/index.html";
    } else if (r.status === 400) {
        setMsg(formMsg, "error", "Validatiefout. Controleer de velden.");
    } else {
        setMsg(formMsg, "error", "Opslaan mislukt.");
    }
});

/* ============================================================================
 * Start
 * ==========================================================================*/
await loadKlanten();
await loadBestellingen();

document.querySelectorAll("th.sortable").forEach(th => {
    th.addEventListener("click", () => {
        const col = th.dataset.col;
        if (!col) return;
        if (sortState.col !== col) {
            sortState = { col, dir: 1 };               // start met asc
        } else {
            sortState.dir = sortState.dir === 1 ? -1   // asc → desc
                : sortState.dir === -1 ? 0             // desc → normaal
                    : 1;                               // normaal → asc
        }
        renderBestellingen();
    });
});

// Suggest actuele maand bij openen (extra zekerheid)
if (!inpMaand.value) inpMaand.value = monthNameNlBE();
validate();
