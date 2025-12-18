"use strict";

(function () {
    function ensureBanner() {
        let el = document.getElementById('update-banner');
        if (el) return el;

        el = document.createElement('div');
        el.id = 'update-banner';
        el.style.position = 'fixed';
        el.style.top = '12px';
        el.style.left = '50%';
        el.style.transform = 'translateX(-50%)';
        el.style.padding = '10px 14px';
        el.style.borderRadius = '10px';
        el.style.boxShadow = '0 10px 30px rgba(0,0,0,0.12)';
        el.style.background = '#ffffff';
        el.style.border = '1px solid rgba(0,0,0,0.08)';
        el.style.fontFamily = 'system-ui, -apple-system, Segoe UI, Roboto, Arial';
        el.style.fontSize = '14px';
        el.style.zIndex = '9999';
        el.style.display = 'none';
        document.body.appendChild(el);
        return el;
    }

    function show(msg) {
        const el = ensureBanner();
        el.textContent = msg;
        el.style.display = 'block';
    }

    function hide() {
        const el = document.getElementById('update-banner');
        if (el) el.style.display = 'none';
    }

    if (window.updater && typeof window.updater.onStatus === 'function') {
        window.updater.onStatus((s) => {
            if (!s || !s.type) return;

            if (s.type === 'available') {
                show(`Update gevonden (v${s.version}). Download start...`);
            }

            if (s.type === 'downloading') {
                show(`Update downloaden… ${s.percent}% (laat de app open)`);
            }

            if (s.type === 'downloaded') {
                show(`Update klaar (v${s.version}). Sluit de app om te installeren.`);
                // Optional: na 15s banner weg
                setTimeout(hide, 15000);
            }

            if (s.type === 'error') {
                show(`Update probleem: ${s.message}`);
            }

            if (s.type === 'none') {
                // meestal niet tonen
                hide();
            }
        });
    }
})();
