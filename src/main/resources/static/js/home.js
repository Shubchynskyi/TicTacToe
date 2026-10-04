function changeNickname() {
    const newNick = document.getElementById('nickInput').value.trim();
    if (newNick) {
        fetch('/update-nick?nick=' + encodeURIComponent(newNick))
            .then(() => location.reload());
    }
}

function onGameModeChange(value) {
    const singleSettings = document.getElementById('singleSettings');
    const btnSingle = document.getElementById('btnSingle');
    const btnLocal = document.getElementById('btnLocal');
    const gameModeHidden = document.getElementById('gameModeHidden');

    if (value === 'single') {
        singleSettings.style.display = 'block';
        singleSettings.style.opacity = '1';
        singleSettings.style.pointerEvents = 'auto';

        btnSingle.classList.add('is-info', 'chosen-mode');
        btnLocal.classList.remove('is-info', 'chosen-mode');
        gameModeHidden.value = 'single';
    } else {
        singleSettings.style.display = 'none';

        btnLocal.classList.add('is-info', 'chosen-mode');
        btnSingle.classList.remove('is-info', 'chosen-mode');
        gameModeHidden.value = 'local';
    }
    btnSingle.setAttribute('aria-pressed', String(value === 'single'));
    btnLocal.setAttribute('aria-pressed', String(value === 'local'));
}

window.addEventListener('load', () => {
    const symHidden = document.getElementById('playerSymbolHidden');
    const btnX = document.getElementById('btnX');
    const btnO = document.getElementById('btnO');
    if (symHidden && symHidden.value === 'O') {
        btnO.classList.add('selected');
    } else if (btnX) {
        btnX.classList.add('selected');
    }

    updateDiffDisplay();
    selectSymbol(displaySymbol(symHidden.value) === 'O' ? 'O' : 'X');
    document.getElementById('nickInput').addEventListener('keydown', event => {
        if (event.key === 'Enter') { event.preventDefault(); changeNickname(); }
    });
});

function selectSymbol(symbol) {
    const btnX = document.getElementById('btnX');
    const btnO = document.getElementById('btnO');
    btnX.classList.remove('selected');
    btnO.classList.remove('selected');

    if (symbol === 'X') {
        btnX.classList.add('selected');
    } else {
        btnO.classList.add('selected');
    }
    document.getElementById('playerSymbolHidden').value = symbol;
    btnX.setAttribute('aria-pressed', String(symbol === 'X'));
    btnO.setAttribute('aria-pressed', String(symbol === 'O'));
}

function prevDifficulty() {
    const sel = document.getElementById('difficultySelect');
    if (!sel) return;
    let i = sel.selectedIndex;
    i = (i - 1 + sel.options.length) % sel.options.length;
    sel.selectedIndex = i;
    updateDiffDisplay();
}

function nextDifficulty() {
    const sel = document.getElementById('difficultySelect');
    if (!sel) return;
    let i = sel.selectedIndex;
    i = (i + 1) % sel.options.length;
    sel.selectedIndex = i;
    updateDiffDisplay();
}

function updateDiffDisplay() {
    const sel = document.getElementById('difficultySelect');
    const diffSpan = document.getElementById('diffDisplay');
    if (!sel || !diffSpan) return;
    diffSpan.innerText = sel.options[sel.selectedIndex].text;
}
