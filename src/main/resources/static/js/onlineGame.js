let stompClient = null;
let gameId = 0;
let currentNick = "";
let currentUserId = "";
let crossOutAudio = null;
let lastComboString = null;

window.addEventListener('load', () => {
    crossOutAudio = new Audio('/sounds/crossout.mp3');

    const params = new URLSearchParams(window.location.search);
    gameId = params.get('gameId');
    document.getElementById('gid').innerText = gameId;

    currentNick = document.getElementById('nick').innerText;
    const uidElem = document.getElementById('userIdHidden');
    if (uidElem) {
        currentUserId = uidElem.value;
    }

    connectOnlineGame();
});

function connectOnlineGame() {
    const sock = new SockJS('/ws');
    stompClient = Stomp.over(sock);
    stompClient.connect({}, frame => {
        console.log("Connected => /topic/online-game-" + gameId, frame);

        stompClient.subscribe("/topic/online-game-" + gameId, msg => {
            if (!msg.body) return;

            if (msg.body === '"CLOSED"') {
                showSessionClosedModal();
                return;
            }
            if (msg.body === '"TIMELEFT_30"') {
                document.getElementById('timeLeft30Modal').style.display = 'block';
                return;
            }

            const onlineGameObj = JSON.parse(msg.body);
            updateBoard(onlineGameObj);
            updateStatus(onlineGameObj);
        });

        fetch('/online-state?gameId=' + gameId)
            .then(r => r.json())
            .then(onlineGameObj => {
                updateBoard(onlineGameObj);
                updateStatus(onlineGameObj);
            })
            .catch(e => console.error(e));
    });
}

function closeTimeLeft30Modal() {
    document.getElementById('timeLeft30Modal').style.display = 'none';
}

function showSessionClosedModal() {
    const m = document.getElementById('sessionClosedModal');
    m.style.display = 'block';

    let sec = 5;
    const tSpan = document.getElementById('closeTimer');
    const interval = setInterval(() => {
        sec--;
        tSpan.innerText = sec;
        if (sec <= 0) {
            clearInterval(interval);
            goToListNow();
        }
    }, 1000);
}

function goToListNow() {
    window.location.href = "/online";
}

function makeMove(r, c) {
    const msg = {
        gameId: gameId,
        userId: currentUserId,
        row: r,
        col: c
    };
    stompClient.send("/app/online-move", {}, JSON.stringify(msg));
}

function rematch() {
    const msg = {gameId: gameId};
    stompClient.send("/app/rematch", {}, JSON.stringify(msg));

    lastComboString = null;
    for (let i = 0; i < 9; i++) {
        document.getElementById('cell' + i).classList.remove('winner-line');
    }
}

function showCreatorLeaveModal() {
    document.getElementById('creatorLeaveModal').style.display = 'block';
}

function confirmCreatorLeave() {
    document.getElementById('creatorLeaveModal').style.display = 'none';
    leaveCurrentGame();
}

function cancelCreatorLeave() {
    document.getElementById('creatorLeaveModal').style.display = 'none';
}

function leaveCurrentGame() {
    const msg = {
        gameId: gameId,
        userId: currentUserId
    };
    stompClient.send("/app/leave-game", {}, JSON.stringify(msg));

    lastComboString = null;
    for (let i = 0; i < 9; i++) {
        document.getElementById('cell' + i).classList.remove('winner-line');
    }

    window.location.href = "/online";
}

function updateStatus(onlineGame) {
    const game = onlineGame.game;
    const turnInfo = document.getElementById('turnInfoEl');
    const rematchBtn = document.getElementById('rematchBtn');

    const i18nEl = document.getElementById('i18nOnlineGame');
    const txtYourTurn = i18nEl.getAttribute('data-your-turn');
    const txtWaitX = i18nEl.getAttribute('data-wait-x');
    const txtWaitO = i18nEl.getAttribute('data-wait-o');
    const txtDraw = i18nEl.getAttribute('data-draw');
    const txtWinner = i18nEl.getAttribute('data-winner');

    let symbol = "";
    if (onlineGame.playerXId === currentUserId) {
        symbol = "X";
    } else if (onlineGame.playerOId === currentUserId) {
        symbol = "O";
    }
    document.getElementById('yourSymbol').innerText = symbol;

    const sX = document.getElementById('scoreX');
    const sO = document.getElementById('scoreO');
    sX.innerText = onlineGame.scoreX;
    sO.innerText = onlineGame.scoreO;

    sX.classList.remove('has-text-info', 'has-text-danger');
    sO.classList.remove('has-text-info', 'has-text-danger');
    if (onlineGame.scoreX > onlineGame.scoreO) {
        sX.classList.add('has-text-info');
        sO.classList.add('has-text-danger');
    } else if (onlineGame.scoreX < onlineGame.scoreO) {
        sX.classList.add('has-text-danger');
        sO.classList.add('has-text-info');
    }

    const pxEl = document.getElementById('playerX');
    const poEl = document.getElementById('playerO');
    pxEl.innerText = onlineGame.playerXDisplay ?? "X";
    poEl.innerText = onlineGame.playerODisplay ?? "O";
    pxEl.classList.remove('has-text-success', 'has-text-weight-bold');
    poEl.classList.remove('has-text-success', 'has-text-weight-bold');
    if (onlineGame.playerXId === currentUserId) {
        pxEl.classList.add('has-text-success', 'has-text-weight-bold');
    }
    if (onlineGame.playerOId === currentUserId) {
        poEl.classList.add('has-text-success', 'has-text-weight-bold');
    }

    if (!onlineGame.finished) {
        rematchBtn.style.display = 'none';

        if (game.currentPlayer === "X") {
            if (onlineGame.playerXId === currentUserId) {
                turnInfo.innerHTML = "<b>" + txtYourTurn + "</b>";
            } else {
                turnInfo.innerText = txtWaitX + " " + (onlineGame.playerXDisplay ?? "X");
            }
        } else {
            if (onlineGame.playerOId === currentUserId) {
                turnInfo.innerHTML = "<b>" + txtYourTurn + "</b>";
            } else {
                turnInfo.innerText = txtWaitO + " " + (onlineGame.playerODisplay ?? "O");
            }
        }

    } else {
        rematchBtn.style.display = 'inline-block';

        if (game.winner === "DRAW") {
            turnInfo.innerHTML = "<b>" + txtDraw + "</b>";
        } else if (game.winner === "X") {
            turnInfo.innerHTML = "<b>" + txtWinner + " " + (onlineGame.playerXDisplay ?? "X") + "</b>";
        } else if (game.winner === "O") {
            turnInfo.innerHTML = "<b>" + txtWinner + " " + (onlineGame.playerODisplay ?? "O") + "</b>";
        }
    }
}

function updateBoard(onlineGameObj) {
    const gameObj = onlineGameObj.game;

    for (let i = 0; i < 9; i++) {
        const cell = document.getElementById('cell' + i);
        cell.classList.remove('winner-line');
    }

    for (let i = 0; i < 9; i++) {
        const sign = gameObj.board[i];
        const cell = document.getElementById('cell' + i);
        cell.innerText = (sign === 'CROSS') ? 'X'
            : (sign === 'NOUGHT') ? 'O'
                : '';
    }

    const isDraw = (gameObj.winner === 'DRAW');
    const combo = onlineGameObj.game.winningCombo;
    if (combo && !isDraw) {
        const comboStr = JSON.stringify(combo);

        if (comboStr !== lastComboString) {
            combo.forEach(idx => {
                document.getElementById('cell' + idx).classList.add('winner-line');
            });
            if (crossOutAudio) {
                crossOutAudio.currentTime = 0;
                crossOutAudio.play().catch(e => console.log(e));
            }
            lastComboString = comboStr;
        } else {
            combo.forEach(idx => {
                document.getElementById('cell' + idx).classList.add('winner-line');
            });
        }
    } else {
        lastComboString = null;
    }
}