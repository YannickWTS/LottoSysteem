const $ = (q)=>document.querySelector(q);
const api = (u,o={})=>fetch(u,{ credentials:'include', headers:{'Content-Type':'application/json'}, ...o });

const rol = localStorage.getItem('rol');          // bij login gezet
const meId = Number(localStorage.getItem('gebruikerId')); // bij login zetten als je dat nog niet deed

// Navigatie
$("#btnTerug").addEventListener('click', ()=>location.href='/welkom.html');

// 1) Eigen wachtwoord wijzigen (iedereen)
$("#formWachtwoord").addEventListener('submit', async (e)=>{
    e.preventDefault();
    const w1 = e.target.w1.value.trim();
    const w2 = e.target.w2.value.trim();
    if(w1.length < 6) return alert("Wachtwoord te kort (min 6).");
    if(w1 !== w2)   return alert("Wachtwoorden komen niet overeen.");
    const r = await api(`/gebruiker/${meId}/wachtwoord`, { method:'PUT', body: JSON.stringify({ wachtwoord: w1 }) });
    if(r.ok){ alert("Wachtwoord gewijzigd."); e.target.reset(); }
    else if(r.status===401){ location.href='/index.html'; }
    else if(r.status===403){ alert('Geen rechten om dit te wijzigen.'); }
    else { alert('Wijzigen mislukt.'); }
});

// 2) Admin-blok (lijst + acties)
async function loadUsers(){
    const res = await api('/gebruiker');
    if(res.status===401){ location.href='/index.html'; return; }
    if(res.status===403){ return; } // geen admin → blok blijft verborgen
    $("#adminBlok").classList.remove('hidden');

    const lijst = await res.json();
    const tbody = $("#usersTbody"); tbody.innerHTML = "";
    for(const u of lijst){
        const tr = document.createElement('tr');
        tr.innerHTML = `
      <td>${u.id}</td>
      <td>${u.gebruikersnaam}</td>
      <td>
        <select class="rol" data-id="${u.id}">
          <option ${u.rol==='USER' ? 'selected':''}>USER</option>
          <option ${u.rol==='ADMIN'? 'selected':''}>ADMIN</option>
        </select>
      </td>
      <td>
        <button class="pw warn" data-id="${u.id}">Reset wachtwoord</button>
        <button class="del danger" data-id="${u.id}">Verwijder</button>
      </td>`;
        tbody.appendChild(tr);
    }
}

document.addEventListener('change', async (e)=>{
    if(e.target.matches('select.rol')){
        const id = e.target.dataset.id;
        const rol = e.target.value;
        const r = await api(`/gebruiker/${id}/rol`, { method:'PUT', body: JSON.stringify({ rol }) });
        if(!r.ok) { alert('Rol wijzigen mislukt.'); }
    }
});

document.addEventListener('click', async (e)=>{
    if(e.target.matches('button.del')){
        const id = e.target.dataset.id;
        if(!confirm('Gebruiker verwijderen?')) return;
        const r = await api(`/gebruiker/${id}`, { method:'DELETE' });
        if(r.ok) loadUsers(); else alert('Verwijderen mislukt.');
    }
    if(e.target.matches('button.pw')){
        const id = e.target.dataset.id;
        const nieuw = prompt('Nieuw tijdelijk wachtwoord:');
        if(!nieuw) return;
        const r = await api(`/gebruiker/${id}/wachtwoord`, { method:'PUT', body: JSON.stringify({ wachtwoord: nieuw }) });
        if(!r.ok) alert('Reset mislukt.');
    }
});

// Init
loadUsers();
