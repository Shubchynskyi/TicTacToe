function changeNickname() {
    const newNick=document.getElementById('nickInput').value.trim();
    if(newNick){
        fetch('/update-nick?nick='+encodeURIComponent(newNick))
            .then(()=>location.reload());
    }
}

function onGameModeChange(value){
    const singleSettings=document.getElementById('singleSettings');
    if(value==='single'){
        singleSettings.style.display='block';
    } else {
        singleSettings.style.display='none';
    }
}

window.addEventListener('load', ()=>{
    // Если playerSymbolHidden == "O" => btnO selected
    // иначе "X"
    const symHidden=document.getElementById('playerSymbolHidden');
    const btnX=document.getElementById('btnX');
    const btnO=document.getElementById('btnO');
    if(symHidden.value==='O'){
        btnO.classList.add('selected');
    } else {
        btnX.classList.add('selected');
    }
});

function selectSymbol(symbol){
    const btnX=document.getElementById('btnX');
    const btnO=document.getElementById('btnO');
    btnX.classList.remove('selected');
    btnO.classList.remove('selected');

    if(symbol==='X'){
        btnX.classList.add('selected');
    } else {
        btnO.classList.add('selected');
    }
    document.getElementById('playerSymbolHidden').value=symbol;
}
