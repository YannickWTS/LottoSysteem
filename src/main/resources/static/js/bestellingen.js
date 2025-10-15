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

// Format "YYYY-MM-DD" -> "DD/MM/YYYY" (veilig i.v.m. timezone)
function fmtLocalDateToNl(d) {
    if (!d) return "";
    const [y, m, day] = String(d).split("-");
    return `${day}/${m}/${y}`;
}

/* ============================================================================
 * UI refs
 * ==========================================================================*/
const form = qs("#formBestelling");
const selSpel = qs("#selSpel");
const inpMaand = qs("#inpMaand");
const chkBetaald = qs("#chkBetaald");
const btnOpslaan = qs("#btnOpslaan");
const formMsg = qs("#formMsg");
const tbody = qs("#bestelTbody");

// Combobox-onderdelen (klant zoeken)
const klantSearch = qs("#klantSearch");
const klantList = qs("#klantList");

/* ============================================================================
 * State
 * ==========================================================================*/
let klantenCache = [];        // [{id, label, naam}, ...]
let klantenMap = {};          // id -> naam
let selectedKlantId = 0;

const medewerkerCache = new Map(); // id -> gebruikersnaam (lazy geladen)

/* ============================================================================
 * Init
 * ==========================================================================*/
qs("#btnRefresh")?.addEventListener("click", loadBestellingen);
qs("#btnTerug")?.addEventListener("click", () => (location.href = "welkom.html"));

// Maand automatisch invullen + readOnly
inpMaand.value = monthNameNlBE();
inpMaand.readOnly = true;

// ▼ Klanten laden
async function loadKlanten(){
    const r = await api("/klanten");
    if(r.status===401){ location.href="/index.html"; return; }
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
                label,               // ← tonen
                naam: displayNaam,   // ← naam
                naamLower: displayNaam.toLowerCase() // ← filter-key
            };
        })
        .sort((a,b)=> a.naam.localeCompare(b.naam,'nl',{sensitivity:"base"}));

    klantenMap = Object.fromEntries(klantenCache.map(k => [k.id, k.naam]));
    renderKlantList("");   // start: leeg tot je typt
    validate();
}

// ▼ Popover-lijst alleen op NAAM filteren
function renderKlantList(filter){
    const f = filter.trim().toLowerCase();
    const lijst = f
        ? klantenCache.filter(k => k.naamLower.includes(f)) // ← alleen naam
        : []; // geen filter = lijst dicht/geen items

    klantList.innerHTML = lijst
        .map((k,i)=>`<div class="combo-item${i===0?" active":""}" data-id="${k.id}">${k.label}</div>`)
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
        // 403/404 → geen toegang of niet gevonden
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
    if (r.status === 401) {
        location.href = "/index.html";
        return;
    }
    const lijst = await r.json();

    tbody.innerHTML = "";

    for (const b of lijst) {
        const tr = document.createElement("tr");

        const klantNaam = b.klantNaam || klantenMap[b.klantId] || `#${b.klantId}`;
        const medewerkerNaam = b.medewerkerNaam || (b.medewerkerId ? `#${b.medewerkerId}` : "-");

        const acties = [];
        if (!b.betaald) {
            acties.push(`<span class="action-link act-betaal" data-id="${b.id}">Markeer betaald</span>`);
        }
        if (isAdmin()) {
            acties.push(`<span class="action-link danger act-del" data-id="${b.id}">Verwijder</span>`);
        }

        tr.innerHTML = `
    <td>${b.id}</td>
    <td>${klantNaam}</td>
    <td>${b.spelType}</td>
    <td>${b.maand}</td>
    <td>${fmtLocalDateToNl(String(b.datumRegistratie))}</td>
    <td>${b.betaald ? "Ja" : "Nee"}</td>
    <td>${medewerkerNaam}</td>
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
        inpMaand.value = monthNameNlBE();
        validate();
        await loadBestellingen();
        alert("Bestelling opgeslagen.");
    } else if (r.status === 401) {
        location.href = "/index.html";
    } else if (r.status === 400) {
        alert("Validatiefout. Controleer de velden.");
    } else {
        alert("Opslaan mislukt.");
    }
});

/* ============================================================================
 * Start
 * ==========================================================================*/
await loadKlanten();
await loadBestellingen();

// Suggest actuele maand bij openen (extra zekerheid)
if (!inpMaand.value) inpMaand.value = monthNameNlBE();
validate();
