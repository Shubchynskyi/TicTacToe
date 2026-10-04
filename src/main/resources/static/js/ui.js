(() => {
    const root = document.documentElement;
    const toggle = document.getElementById('themeToggle');
    const systemTheme = matchMedia('(prefers-color-scheme: dark)');

    function applyTheme(theme) {
        root.dataset.theme = theme;
        document.querySelector('meta[name="theme-color"]').content = theme === 'dark' ? '#161b19' : '#f6f5ef';
        if (toggle) toggle.setAttribute('aria-pressed', String(theme === 'dark'));
    }

    applyTheme(root.dataset.theme || (systemTheme.matches ? 'dark' : 'light'));
    const languageSelect = document.getElementById('languageSelect');
    if (languageSelect) {
        languageSelect.addEventListener('change', () => {
            const language = languageSelect.value;
            if (!['en', 'de', 'ua', 'ru'].includes(language)) return;
            try { localStorage.setItem('tictactoe-language', language); }
            catch (error) { /* The selected page language still works when storage is blocked. */ }
            const url = new URL(window.location.href);
            url.searchParams.set('lang', language);
            window.location.assign(url.toString());
        });
    }
    if (toggle) {
        toggle.addEventListener('click', () => {
            const theme = root.dataset.theme === 'dark' ? 'light' : 'dark';
            applyTheme(theme);
            try { localStorage.setItem('tictactoe-theme', theme); } catch (error) { /* Theme still works when storage is blocked. */ }
        });
    }
    systemTheme.addEventListener('change', event => {
        try { if (!localStorage.getItem('tictactoe-theme')) applyTheme(event.matches ? 'dark' : 'light'); }
        catch (error) { applyTheme(event.matches ? 'dark' : 'light'); }
    });

    const installButton = document.getElementById('installApp');
    if (installButton) {
        const appMode = matchMedia('(display-mode: standalone), (display-mode: fullscreen), (display-mode: minimal-ui)');
        const installHelp = document.getElementById('installHelp');
        let installPrompt = null;
        let installed = false;

        function updateInstallButton() {
            installButton.hidden = installed || appMode.matches || navigator.standalone === true;
        }

        function showInstallHelp() {
            const isIos = /iPad|iPhone|iPod/.test(navigator.userAgent || '')
                || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);
            document.getElementById('installBrowserHelp').hidden = isIos;
            document.getElementById('installIosHelp').hidden = !isIos;
            if (!installHelp.open) installHelp.showModal();
        }

        window.addEventListener('beforeinstallprompt', event => {
            event.preventDefault();
            installPrompt = event;
            updateInstallButton();
        });
        window.addEventListener('appinstalled', () => {
            installed = true;
            installPrompt = null;
            updateInstallButton();
        });
        appMode.addEventListener('change', updateInstallButton);
        installButton.addEventListener('click', async () => {
            if (!installPrompt) {
                showInstallHelp();
                return;
            }
            const prompt = installPrompt;
            installPrompt = null;
            installButton.disabled = true;
            try {
                await prompt.prompt();
                const choice = await prompt.userChoice;
                installed = installed || choice.outcome === 'accepted';
            } catch (error) {
                console.error('App installation prompt failed', error);
                showInstallHelp();
            } finally {
                installButton.disabled = false;
                updateInstallButton();
            }
        });
        updateInstallButton();
    }

    if ('serviceWorker' in navigator) {
        window.addEventListener('load', () => {
            navigator.serviceWorker.register('/sw.js?v=20261004-3', { scope: '/', updateViaCache: 'none' })
                .catch(error => console.error('Service worker registration failed', error));
        });
    }

    // Move focus into dialogs opened by the existing online-game handlers.
    let returnFocus = null;
    document.querySelectorAll('.modal').forEach(modal => {
        new MutationObserver(() => {
            if (modal.style.display === 'block') {
                returnFocus = document.activeElement;
                modal.querySelector('button')?.focus();
            } else if (returnFocus) { returnFocus.focus(); returnFocus = null; }
        }).observe(modal, { attributes: true, attributeFilter: ['style'] });
        modal.addEventListener('keydown', event => {
            const buttons = Array.from(modal.querySelectorAll('button, a[href]'));
            if (event.key !== 'Tab' || !buttons.length) return;
            const first = buttons[0];
            const last = buttons[buttons.length - 1];
            if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
            else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
        });
    });
})();

// Shared presentation only; the server remains responsible for all game rules.
function displaySymbol(symbol) {
    return symbol === '0' ? 'O' : symbol;
}

function paintBoardCell(cell, index, symbol, locked) {
    cell.textContent = symbol;
    cell.dataset.symbol = symbol;
    cell.disabled = Boolean(symbol) || locked;
    const label = cell.closest('.game-board').dataset.cellLabel;
    cell.setAttribute('aria-label', label + ' ' + (index + 1) + (symbol ? ': ' + symbol : ''));
}
