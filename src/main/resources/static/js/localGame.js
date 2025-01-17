let crossOutAudioLocal = null;
let lastComboStringLocal = null;

window.addEventListener('load', () => {
    crossOutAudioLocal = new Audio('/sounds/crossout.mp3');
    refreshGameState();
});

async function refreshGameState() {
    const resp = await fetch('/game-state');
    if (resp.ok) {
        const game = await resp.json();
        updateBoard(game);
        updateStatus(game);
    }
}

async function makeMove(r, c) {
    const resp = await fetch(`/make-move?row=${r}&col=${c}`);
    if (resp.ok) {
        const game = await resp.json();
        updateBoard(game);
        updateStatus(game);
    }
}

function updateBoard(game) {
    for (let i = 0; i < 9; i++) {
        const cellEl = document.getElementById('cell' + i);
        cellEl.classList.remove('winner-line');
    }

    for (let i = 0; i < 9; i++) {
        if (game.board[i] === 'CROSS') {
            document.getElementById('cell' + i).innerText = 'X';
        } else if (game.board[i] === 'NOUGHT') {
            document.getElementById('cell' + i).innerText = 'O';
        } else {
            document.getElementById('cell' + i).innerText = '';
        }
    }

    if (game.winner && game.winner !== 'DRAW' && game.winningCombo) {
        const comboStr = JSON.stringify(game.winningCombo);
        if (comboStr !== lastComboStringLocal) {
            game.winningCombo.forEach(idx => {
                document.getElementById('cell' + idx).classList.add('winner-line');
            });
            if (crossOutAudioLocal) {
                crossOutAudioLocal.currentTime = 0;
                crossOutAudioLocal.play().catch(e => console.log(e));
            }
            lastComboStringLocal = comboStr;
        } else {
            game.winningCombo.forEach(idx => {
                document.getElementById('cell' + idx).classList.add('winner-line');
            });
        }
    } else {
        lastComboStringLocal = null;
    }
}

function updateStatus(game) {
    const i18nEl = document.getElementById('i18nLocalGame');
    const textTurn = i18nEl.getAttribute('data-turn');
    const textWinner = i18nEl.getAttribute('data-winner');
    const textDraw = i18nEl.getAttribute('data-draw');
    const textRestart = i18nEl.getAttribute('data-restart');
    const statusEl = document.getElementById('status');
    const btnRestart = document.getElementById('restartBtn');

    if (game.gameMode === 'single') {
        document.getElementById('scorePanel').style.display = 'block';
        document.getElementById('scoreHuman').innerText = game.scoreHuman;
        document.getElementById('scoreAI').innerText = game.scoreAI;
    } else {
        document.getElementById('scorePanel').style.display = 'none';
    }

    if (game.gameOver) {
        btnRestart.innerText = textRestart;
        if (game.winner === 'DRAW') {
            statusEl.innerText = textDraw;
        } else {
            statusEl.innerText = textWinner + ' ' + game.winner;
        }
    } else {
        btnRestart.innerText = textRestart;
        statusEl.innerText = textTurn + ' ' + game.currentPlayer;
    }
}

async function restartLocal() {
    const resp = await fetch('/restart-local');
    if (resp.ok) {
        const game = await resp.json();
        lastComboStringLocal = null;
        updateBoard(game);
        updateStatus(game);
    }
}