let stompList = null;
let currentUserId = "";

window.addEventListener('load', () => {
    const uidElem = document.getElementById('userIdHidden');
    if (uidElem) {
        currentUserId = uidElem.value;
    }
    connectList();
});

// WebSocket
function connectList() {
    const sock = new SockJS('/ws');
    stompList = Stomp.over(sock);
    stompList.connect({}, frame => {
        console.log("Connected => /topic/game-list", frame);
        stompList.subscribe("/topic/game-list", msg => {
            const games = JSON.parse(msg.body);
            renderGameList(games);
        });
    }, error => {
        console.error("WebSocket connection error:", error);
    });
}

function renderGameList(games) {
    const container = document.querySelector('.container');
    container.innerHTML = '';

    const i18n = document.getElementById('i18nOnline');
    const txtWait = i18n.getAttribute('data-waiting');
    const txtProg = i18n.getAttribute('data-progress');
    const txtGo = i18n.getAttribute('data-go');
    const txtJoin = i18n.getAttribute('data-join');
    const txtInpr = i18n.getAttribute('data-inprogress');

    games.forEach(g => {
        const statusText = g.waitingForSecondPlayer ? txtWait : txtProg;

        let actionHtml = '';
        if (g.waitingForSecondPlayer) {
            if (currentUserId === g.playerXId || currentUserId === g.playerOId) {
                actionHtml = `
          <a class="button is-small is-link btn-w100 ml-5" href="/onlineGame?gameId=${g.gameId}">
            ${txtGo}
          </a>`;
            } else {
                actionHtml = `
          <a class="button is-small is-info btn-w100 ml-5" href="/join-online?gameId=${g.gameId}">
            ${txtJoin}
          </a>`;
            }
        } else {
            if (currentUserId === g.playerXId || currentUserId === g.playerOId) {
                actionHtml = `
          <a class="button is-small is-link btn-w100 ml-5" href="/onlineGame?gameId=${g.gameId}">
            ${txtGo}
          </a>`;
            } else {
                actionHtml = txtInpr;
            }
        }

        const gameRow = document.createElement('div');
        gameRow.className = 'box game-row';

        // ID
        const idCol = document.createElement('div');
        idCol.className = 'game-col col-id';
        idCol.innerHTML = `<strong>ID:</strong> ${g.gameId}`;
        gameRow.appendChild(idCol);

        // Player X
        const xCol = document.createElement('div');
        xCol.className = 'game-col col-x';
        xCol.innerHTML = `<strong>Player X:</strong> ${g.playerXDisplay ?? ''}`;
        gameRow.appendChild(xCol);

        // Player O
        const oCol = document.createElement('div');
        oCol.className = 'game-col col-o';
        oCol.innerHTML = `<strong>Player O:</strong> ${g.playerODisplay ?? ''}`;
        gameRow.appendChild(oCol);

        // Status
        const statusCol = document.createElement('div');
        statusCol.className = 'game-col col-status';
        statusCol.innerHTML = `<strong>Status:</strong> ${statusText}`;
        gameRow.appendChild(statusCol);

        // Action
        const actionCol = document.createElement('div');
        actionCol.className = 'game-col col-action';
        actionCol.innerHTML = `<strong>Action:</strong> ${actionHtml}`;
        gameRow.appendChild(actionCol);

        container.appendChild(gameRow);
    });
}

function forceRefresh() {
    window.location.reload();
}