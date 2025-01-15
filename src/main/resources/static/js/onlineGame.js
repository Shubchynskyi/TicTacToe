let stompClient = null;
let gameId = 0;
let currentNick = "";
let currentUserId = "";
let crossOutAudio = null;
let lastComboString = null;

window.addEventListener('load', () => {
    // Загружаем звук
    crossOutAudio = new Audio('/sounds/crossout.mp3');

    // Считываем gameId, userId
    const params = new URLSearchParams(window.location.search);
    gameId = params.get('gameId');
    document.getElementById('gid').innerText = gameId;

    currentNick = document.getElementById('nick').innerText;
    const uidElem = document.getElementById('userIdHidden');
    if (uidElem) {
        currentUserId = uidElem.value;
    }

    // Запускаем подключение
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
    const msg={gameId:gameId};
    stompClient.send("/app/rematch", {}, JSON.stringify(msg));

    // Локально тоже стираем "lastComboString" и убираем класс
    lastComboString = null;
    for(let i=0;i<9;i++){
        document.getElementById('cell'+i).classList.remove('winner-line');
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

    // Снимаем локальные эффекты
    lastComboString = null;
    for(let i=0;i<9;i++){
        document.getElementById('cell'+i).classList.remove('winner-line');
    }

    window.location.href = "/online";
}

function updateStatus(onlineGame) {
    const game = onlineGame.game;
    const stEl = document.getElementById('status');

    // Ваш символ
    let symbol = "";
    if (onlineGame.playerXId === currentUserId) {
        symbol = "X";
    } else if (onlineGame.playerOId === currentUserId) {
        symbol = "O";
    }
    document.getElementById('yourSymbol').innerText = symbol;

    // Счёт
    const sX = document.getElementById('scoreX');
    const sO = document.getElementById('scoreO');
    sX.innerText = onlineGame.scoreX;
    sO.innerText = onlineGame.scoreO;

    // Подсветка счёта
    sX.classList.remove('has-text-info', 'has-text-danger');
    sO.classList.remove('has-text-info', 'has-text-danger');
    if (onlineGame.scoreX > onlineGame.scoreO) {
        sX.classList.add('has-text-info');
        sO.classList.add('has-text-danger');
    } else if (onlineGame.scoreX < onlineGame.scoreO) {
        sX.classList.add('has-text-danger');
        sO.classList.add('has-text-info');
    }

    // Ники
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

    const turnInfo = document.getElementById('turnInfoEl');
    turnInfo.innerText = "";

    if (!onlineGame.finished) {
        document.getElementById('rematchBtn').style.display = 'none';
        if (game.currentPlayer === "X") {
            if (onlineGame.playerXId === currentUserId) {
                turnInfo.innerHTML = "<b>Ваш ход</b>";
            } else {
                turnInfo.innerText = "Ожидаем ход игрока X: "
                    + (onlineGame.playerXDisplay ?? "X");
            }
        } else {
            if (onlineGame.playerOId === currentUserId) {
                turnInfo.innerHTML = "<b>Ваш ход</b>";
            } else {
                turnInfo.innerText = "Ожидаем ход игрока O: "
                    + (onlineGame.playerODisplay ?? "O");
            }
        }
    } else {
        // finished
        document.getElementById('rematchBtn').style.display = 'inline-block';
        if (game.winner === "DRAW") {
            stEl.innerText = "Ничья!";
        } else if (game.winner === "X") {
            stEl.innerText = "Победил: " + (onlineGame.playerXDisplay ?? "X");
        } else if (game.winner === "O") {
            stEl.innerText = "Победил: " + (onlineGame.playerODisplay ?? "O");
        }
    }
}

// function updateBoard(g){
//     for(let i=0; i<9; i++){
//         const cell=document.getElementById('cell'+i);
//         if(g.board[i]==='CROSS'){
//             cell.innerText='X';
//         } else if(g.board[i]==='NOUGHT'){
//             cell.innerText='O';
//         } else {
//             cell.innerText='';
//         }
//     }
// }

// Полный метод updateBoard
function updateBoard(onlineGameObj) {
    const gameObj = onlineGameObj.game;

    // Снимем стили winner-line (если были)
    for (let i=0; i<9; i++){
        const cell = document.getElementById('cell'+i);
        cell.classList.remove('winner-line');
    }

    // Заполняем X/O
    for (let i=0; i<9; i++){
        const sign = gameObj.board[i]; // 'CROSS' | 'NOUGHT' | 'EMPTY'
        const cell = document.getElementById('cell'+i);
        cell.innerText = (sign === 'CROSS') ? 'X'
            : (sign === 'NOUGHT') ? 'O'
                : '';
    }

    // Смотрим, есть ли combo, и при этом не ничья
    const isDraw = (gameObj.winner==='DRAW');
    const combo = onlineGameObj.game.winningCombo;
    if(combo && !isDraw){
        const comboStr=JSON.stringify(combo);
        
        if(comboStr!==lastComboString){
            // новая линия
            combo.forEach(idx=>{
                document.getElementById('cell'+idx).classList.add('winner-line');
            });
            // звук один раз
            if(crossOutAudio){
                crossOutAudio.currentTime=0;
                crossOutAudio.play().catch(e=>console.log(e));
            }
            lastComboString=comboStr;
        } else {
            // combo не изменился => просто добавим .winner-line (без звука)
            combo.forEach(idx=>{
                document.getElementById('cell'+idx).classList.add('winner-line');
            });
        }
    } else {
        // Либо combo=null, либо DRAW => сбрасываем
        lastComboString=null;
    }
}