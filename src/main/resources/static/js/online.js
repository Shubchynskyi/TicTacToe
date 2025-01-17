let stompList = null;
let currentUserId = "";

window.addEventListener('load', () => {
    const uidElem = document.getElementById('userIdHidden');
    if (uidElem) {
        currentUserId = uidElem.value;
    }
    connectList();
});

function connectList() {
    const sock = new SockJS('/ws');
    stompList = Stomp.over(sock);
    stompList.connect({}, frame => {
        console.log("Connected => /topic/game-list", frame);
        stompList.subscribe("/topic/game-list", msg => {
            const games = JSON.parse(msg.body);
            renderGameList(games);
        });
    });
}

function renderGameList(games) {
    const tbody = document.getElementById('gamesBody');
    let html = '';

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
          <a class="button is-small is-link" href="/onlineGame?gameId=${g.gameId}">
            ${txtGo}
          </a>`;
            } else {
                actionHtml = `
          <a class="button is-small is-info" href="/join-online?gameId=${g.gameId}">
            ${txtJoin}
          </a>`;
            }
        } else {
            if (currentUserId === g.playerXId || currentUserId === g.playerOId) {
                actionHtml = `
          <a class="button is-small is-link" href="/onlineGame?gameId=${g.gameId}">
            ${txtGo}
          </a>`;
            } else {
                actionHtml = txtInpr;
            }
        }

        html += `
      <tr>
        <td>${g.gameId}</td>
        <td>${g.playerXDisplay ?? ''}</td>
        <td>${g.playerODisplay ?? ''}</td>
        <td>${statusText}</td>
        <td>${actionHtml}</td>
      </tr>`;
    });

    tbody.innerHTML = html;
}

function forceRefresh() {
    window.location.reload();
}