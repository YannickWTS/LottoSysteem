// ===== Helpers ===============================================================

function qs(selector) {
    return document.querySelector(selector);
}

async function apiFetch(url, options = {}) {
    // Zorg dat cookies/sessie meegaan
    const response = await fetch(url, {
        credentials: "include",
        headers: {"Content-Type": "application/json"},
        ...options
    });
    return response;
}

function getCurrentUserRole() {
    return localStorage.getItem("rol"); // door login.js gezet (USER/ADMIN)
}

function getCurrentUserId() {
    const raw = localStorage.getItem("gebruikerId");
    return raw ? Number(raw) : null;
}

// ===== Navigatie =============================================================

const backButton = qs("#btnTerug");
if (backButton) {
    backButton.addEventListener("click", () => (window.location.href = "/welkom.html"));
}

// ===== Eigen wachtwoord wijzigen ============================================

const passwordForm = qs("#formWachtwoord");

if (passwordForm) {
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
        } else if (response.status === 401) {
            window.location.href = "/index.html";
        } else if (response.status === 403) {
            alert("Geen rechten om dit te wijzigen.");
        } else {
            alert("Wijzigen mislukt.");
        }
    });
}

// ===== Admin-lijst: laden en acties =========================================

async function loadUsersForAdmin() {
    const adminBlock = qs("#adminBlok");
    const tableBody = qs("#usersTbody");

    // Als je geen admin bent, gewoon niets tonen.
    if (getCurrentUserRole() !== "ADMIN") {
        return;
    }

    const response = await apiFetch("/gebruiker");

    if (response.status === 401) {
        window.location.href = "/index.html";
        return;
    }
    if (response.status === 403) {
        // Dit betekent dat de back-end je NIET als ADMIN ziet.
        // Zie sectie 1 hierboven (ROLE_ prefix fix).
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
      <select class="role-select" data-id="${user.id}" data-initial="${user.rol}">
        <option ${user.rol === "USER" ? "selected" : ""}>USER</option>
        <option ${user.rol === "ADMIN" ? "selected" : ""}>ADMIN</option>
      </select>
    </td>
    <td>
      <details>
        <summary>Bewerk</summary>

        <div class="edit-grid">
          <div class="field">
            <label>Nieuw wachtwoord</label>
            <input type="password" class="pw1" data-id="${user.id}" placeholder="min. 6 tekens">
          </div>
          <div class="field">
            <label>Herhaal wachtwoord</label>
            <input type="password" class="pw2" data-id="${user.id}" placeholder="herhaal">
          </div>
        </div>

        <div class="row-actions">
          <button class="save-user-button" data-id="${user.id}" disabled>Opslaan</button>
          <button class="delete-user-button danger" data-id="${user.id}">Verwijder</button>
        </div>
      </details>
    </td>
  `;
        tableBody.appendChild(row);
    }
}

// Event-delegatie voor admin-acties (één luisteraar)
document.addEventListener("click", async (event) => {
    // Nieuwe gebruiker
    if (event.target.matches("#btnNieuweUser")) {
        const username = prompt("Gebruikersnaam:");
        if (!username) return;

        const password = prompt("Wachtwoord:");
        if (!password) return;

        const roleInput = (prompt("Rol (USER/ADMIN):", "USER") || "USER").toUpperCase();

        const response = await apiFetch("/gebruiker", {
            method: "POST",
            body: JSON.stringify({gebruikersnaam: username, wachtwoord: password, rol: roleInput})
        });

        if (response.ok) {
            await loadUsersForAdmin();
        } else {
            alert("Aanmaken mislukt.");
        }
    }

    // Rol opslaan
    if (event.target.matches(".save-role-button")) {
        const userId = event.target.dataset.id;
        const select = document.querySelector(`select.role-select[data-id="${userId}"]`);
        const newRole = select.value;

        const response = await apiFetch(`/gebruiker/${userId}/rol`, {
            method: "PUT",
            body: JSON.stringify({rol: newRole})
        });

        if (response.ok) {
            alert("Rol opgeslagen.");
        } else {
            alert("Rol wijzigen mislukt.");
        }
    }

    // Wachtwoord opslaan voor die gebruiker
    if (event.target.matches(".save-password-button")) {
        const userId = event.target.dataset.id;
        const input = document.querySelector(`.password-input[data-id="${userId}"]`);
        const newPassword = (input.value || "").trim();

        if (newPassword.length < 6) {
            alert("Wachtwoord te kort (minimaal 6 tekens).");
            return;
        }

        const response = await apiFetch(`/gebruiker/${userId}/wachtwoord`, {
            method: "PUT",
            body: JSON.stringify({wachtwoord: newPassword})
        });

        if (response.ok) {
            alert("Wachtwoord aangepast.");
            input.value = "";
        } else {
            alert("Reset mislukt.");
        }
    }

    // Verwijderen
    if (event.target.matches(".delete-user-button")) {
        const userId = event.target.dataset.id;
        if (!confirm("Gebruiker verwijderen?")) return;

        const response = await apiFetch(`/gebruiker/${userId}`, {method: "DELETE"});
        if (response.ok) {
            await loadUsersForAdmin();
        } else {
            alert("Verwijderen mislukt.");
        }
    }
});

// Init
loadUsersForAdmin();
