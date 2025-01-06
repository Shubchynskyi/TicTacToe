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
    // 1) убираем .winner-line
    for(let i=0;i<9;i++){
        const cellEl = document.getElementById('cell'+i);
        cellEl.classList.remove('winner-line');
        // Заполняем X / O
        if(game.board[i]==='CROSS'){
            cellEl.innerText='X';
        } else if(game.board[i]==='NOUGHT'){
            cellEl.innerText='O';
        } else {
            cellEl.innerText='';
        }
    }

    // 2) Если game.winner!=='DRAW' и game.winningCombo!=null => подсветим
    if(game.winner && game.winner!=='DRAW' && game.winningCombo){
        // скажем, "звук" тоже можно
        // document.getElementById('audioWin').play();
        game.winningCombo.forEach(idx=>{
            document.getElementById('cell'+idx).classList.add('winner-line');
        });
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

    if (game.gameOver) {
        document.getElementById('restartBtn').innerText="Заново";
        if (game.winner === 'DRAW') {
            st.innerText='Ничья!';
        } else {
            st.innerText='Победитель: ' + game.winner;
        }
    } else {
        document.getElementById('restartBtn').innerText="Рестарт";
        st.innerText = 'Ход: ' + game.currentPlayer;
    }
}

async function restartLocal() {
    const resp = await fetch('/restart-local');
    if (resp.ok) {
        const game = await resp.json();
        updateBoard(game);
        updateStatus(game);
    }
}

window.addEventListener('load', ()=> {
    refreshGameState();
});