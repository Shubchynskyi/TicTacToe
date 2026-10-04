let stompList = null;
let currentUserId = '';

window.addEventListener('load', () => {
    currentUserId = document.getElementById('userIdHidden').value;
    connectList();
});
function connectList() {
    const sock = new SockJS('/ws');
    stompList = Stomp.over(sock);
    stompList.connect({}, () => {
        stompList.subscribe('/topic/game-list', message => renderGameList(JSON.parse(message.body)));
    }, error => console.error('WebSocket connection error:', error));
}
function renderGameList(games) {
    const container = document.getElementById('gameList');
    const i18n = document.getElementById('i18nOnline').dataset;
    container.replaceChildren();
    document.getElementById('lobbyEmpty').hidden = games.length !== 0;
    function column(className, label, value) {
        const element = document.createElement('div');
        element.className = 'game-col ' + className;
        const heading = document.createElement('strong');
        heading.textContent = label;
        const text = document.createElement('span');
        text.textContent = value ?? '';
        element.append(heading, text);
        return element;
    }
    games.forEach(game => {
        const row = document.createElement('article');
        row.className = 'box game-row';
        const xColumn = column('col-x', i18n.playerX, game.playerXDisplay);
        const oColumn = column('col-o', i18n.playerO, game.playerODisplay);
        if (currentUserId === game.playerXId) xColumn.lastChild.classList.add('has-text-weight-bold');
        if (currentUserId === game.playerOId) oColumn.lastChild.classList.add('has-text-weight-bold');
        const statusColumn = column('col-status', i18n.status, game.waitingForSecondPlayer ? i18n.waiting : i18n.progress);
        statusColumn.lastChild.className = 'status-pill' + (game.waitingForSecondPlayer ? ' is-waiting' : '');
        const action = document.createElement('div');
        action.className = 'game-col col-action';
        const isPlayer = currentUserId === game.playerXId || currentUserId === game.playerOId;
        if (isPlayer || game.waitingForSecondPlayer) {
            const link = document.createElement('a');
            link.className = 'button is-link btn-w100';
            link.href = (isPlayer ? '/onlineGame' : '/join-online') + '?gameId=' + encodeURIComponent(game.gameId);
            link.textContent = isPlayer ? i18n.go : i18n.join;
            action.appendChild(link);
        } else {
            const label = document.createElement('span');
            label.className = 'status-pill';
            label.textContent = i18n.inprogress;
            action.appendChild(label);
        }
        row.append(column('col-id', i18n.room, game.gameId), xColumn, oColumn, statusColumn, action);
        container.appendChild(row);
    });
}
function forceRefresh() { window.location.reload(); }
