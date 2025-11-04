// Haal id=… op, laad bon data, toon, print
(async function () {
    const qs = s => document.querySelector(s);
    const params = new URLSearchParams(location.search);
    const id = params.get('id');

    // 1) Haal bestelling op (pas endpoint aan als nodig)
    let b;
    try {
        const res = await fetch(`/bestelling/${id}`, {credentials: 'include'});
        if (!res.ok) throw new Error('Bestelling niet gevonden');
        b = await res.json();
    } catch (e) {
        alert(e.message);
        return;
    }

    // 2) Vul velden
    // Verwacht velden: klantNaam, maand (tekst “oktober 2025”), medewerkerNaam, lottoBedrag, euroBedrag, totaal
    // Als backend andere keys gebruikt - hier mappen.
    qs('#klantNaam').textContent = b.klantNaam ?? '—';
    qs('#maand').textContent = b.maand ?? '—';
    qs('#medewerkerNaam').textContent = b.medewerkerNaam ?? '—';

    // Bedragen als €XX,XX
    const fmt = n => typeof n === 'number' ? n.toLocaleString('nl-BE', {style: 'currency', currency: 'EUR'}) : '—';
    qs('#lottoBedrag').textContent = fmt(b.lottoBedrag ?? b.lotto ?? 0);
    qs('#euroBedrag').textContent = fmt(b.euromillionsBedrag ?? b.euromillions ?? 0);
    const totaal = (b.totaalBedrag ?? b.totaal ?? ((b.lottoBedrag || 0) + (b.euromillionsBedrag || 0)));
    qs('#totaalBedrag').textContent = fmt(totaal);

    // Datum (nu) in nl-BE
    const nu = new Date();
    qs('#datum').textContent = nu.toLocaleString('nl-BE', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
    });

    // 3) Automatisch printen (Electron kan silent, browser toont dialoog)
    setTimeout(() => {
        if (window.aliApp?.printBon) {
            // Electron-kanaal (als aanwezig)
            window.aliApp.printBon();
        } else {
            window.print();
        }
    }, 200);
})();
