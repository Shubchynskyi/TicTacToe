let stompList=null;
let currentUserId="";

window.addEventListener('load', ()=>{
    const uidElem=document.getElementById('userIdHidden');
    if(uidElem){
        currentUserId=uidElem.value;
    }
    connectList();
});

function connectList(){
    const sock=new SockJS('/ws');
    stompList=Stomp.over(sock);
    stompList.connect({}, frame=>{
        console.log("Connected => /topic/game-list",frame);
        stompList.subscribe("/topic/game-list", msg=>{
            const games=JSON.parse(msg.body);
            renderGameList(games);
        });
    });
}

function renderGameList(games){
    const tbody=document.getElementById('gamesBody');
    let html='';

    games.forEach(g=>{
        const statusText=g.waitingForSecondPlayer?'Ожидает второго':'Игра идёт';

        let actionHtml='';
        if(g.waitingForSecondPlayer){
            if(currentUserId===g.playerXId || currentUserId===g.playerOId){
                // Уже занял слот => "Перейти"
                actionHtml=`
                  <a class="button is-small is-link"
                     href="/onlineGame?gameId=${g.gameId}">
                     Перейти
                  </a>
                `;
            } else {
                // Можно присоединиться
                actionHtml=`
                  <a class="button is-small is-info"
                     href="/join-online?gameId=${g.gameId}">
                     Присоединиться
                  </a>
                `;
            }
        } else {
            // !waiting
            if(currentUserId===g.playerXId || currentUserId===g.playerOId){
                actionHtml=`
                  <a class="button is-small is-link"
                     href="/onlineGame?gameId=${g.gameId}">
                     Перейти
                  </a>
                `;
            } else {
                actionHtml=`Идёт игра`;
            }
        }

        html+=`
          <tr>
            <td>${g.gameId}</td>
            <td>${g.playerXDisplay??''}</td>
            <td>${g.playerODisplay??''}</td>
            <td>${statusText}</td>
            <td>${actionHtml}</td>
          </tr>
        `;
    });
    tbody.innerHTML=html;
}

function forceRefresh(){
    window.location.reload();
}
