let crossOutAudioLocal = null;
let lastComboStringLocal = null;

window.addEventListener('load', ()=> {
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
    // 1) убираем winner-line
    for(let i=0;i<9;i++){
        const cellEl = document.getElementById('cell'+i);
        cellEl.classList.remove('winner-line');
    }
    // 2) заполняем X/O
    for(let i=0;i<9;i++){
        if(game.board[i]==='CROSS'){
            document.getElementById('cell'+i).innerText='X';
        } else if(game.board[i]==='NOUGHT'){
            document.getElementById('cell'+i).innerText='O';
        } else {
            document.getElementById('cell'+i).innerText='';
        }
    }
    // 3) зачеркивание, если есть winningCombo и не ничья
    if(game.winner && game.winner!=='DRAW' && game.winningCombo){
        const comboStr = JSON.stringify(game.winningCombo);
        if(comboStr!==lastComboStringLocal){
            // новая
            game.winningCombo.forEach(idx=>{
                document.getElementById('cell'+idx).classList.add('winner-line');
            });
            // звук 1 раз
            if(crossOutAudioLocal){
                crossOutAudioLocal.currentTime=0;
                crossOutAudioLocal.play().catch(e=>console.log(e));
            }
            lastComboStringLocal = comboStr;
        } else {
            // та же combo => добавим .winner-line без звука
            game.winningCombo.forEach(idx=>{
                document.getElementById('cell'+idx).classList.add('winner-line');
            });
        }
    } else {
        // нет combo / ничья => сброс
        lastComboStringLocal=null;
    }
}

function updateStatus(game) {
    const st = document.getElementById('status');

    // Если single -> показываем счёт, иначе скрываем
    if (game.gameMode === 'single') {
        document.getElementById('scorePanel').style.display='block';
        document.getElementById('scoreHuman').innerText=game.scoreHuman;
        document.getElementById('scoreAI').innerText=game.scoreAI;
    } else {
        // local (2 players) => не показываем счёт
        document.getElementById('scorePanel').style.display='none';
    }

    const btn = document.getElementById('restartBtn');
    if(game.gameOver){
        btn.innerText="Заново";
        if(game.winner==='DRAW'){
            st.innerText='Ничья!';
        } else {
            st.innerText='Победитель: '+game.winner;
        }
    } else {
        btn.innerText="Рестарт";
        st.innerText='Ход: '+game.currentPlayer;
    }
}

async function restartLocal() {
    const resp = await fetch('/restart-local');
    if (resp.ok) {
        const game = await resp.json();
        lastComboStringLocal=null;
        updateBoard(game);
        updateStatus(game);
    }
}

