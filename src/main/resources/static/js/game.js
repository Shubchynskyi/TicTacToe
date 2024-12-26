// // src/main/resources/static/js/game.js
//
// document.addEventListener('DOMContentLoaded', function () {
//     const cells = document.querySelectorAll('.cell');
//     const gameMessage = document.getElementById('gameMessage');
//     const gameResult = document.getElementById('gameResult');
//     const restartButton = document.getElementById('restartButton');
//     const currentPlayerSpan = document.getElementById('currentPlayer');
//
//     cells.forEach(cell => {
//         cell.addEventListener('click', function () {
//             const row = this.getAttribute('data-row');
//             const col = this.getAttribute('data-col');
//
//             // Проверка, пуста ли ячейка до отправки запроса
//             const cellContent = this.querySelector('span').textContent.trim();
//             if (cellContent !== '') {
//                 // Ячейка уже занята, не отправляем запрос
//                 alert('Эта ячейка уже занята!');
//                 return;
//             }
//
//             // Отправка AJAX-запроса на сервер
//             fetch('/game/makeMove', {
//                 method: 'POST',
//                 headers: {
//                     'Content-Type': 'application/x-www-form-urlencoded',
//                 },
//                 body: `row=${row}&col=${col}`
//             })
//                 .then(response => {
//                     if (!response.ok) {
//                         return response.json().then(err => { throw err; });
//                     }
//                     return response.json();
//                 })
//                 .then(data => {
//                     if (data.message) {
//                         // Отображение сообщения об ошибке или победе
//                         gameResult.textContent = data.message;
//                         gameMessage.style.display = 'block';
//                     } else {
//                         // Обновление игрового поля
//                         updateBoard(data.board);
//                         // Обновление текущего игрока
//                         currentPlayerSpan.textContent = data.currentPlayer === 'cross' ? 'Крестик' : 'Нолик';
//                         if (data.gameOver) {
//                             gameResult.textContent = data.winner ? `Победитель: ${data.winner === 'cross' ? 'Крестик' : 'Нолик'}` : 'Ничья!';
//                             gameMessage.style.display = 'block';
//                         }
//                     }
//                 })
//                 .catch(error => {
//                     console.error('Ошибка:', error);
//                     alert(error.message || 'Произошла ошибка при совершении хода.');
//                 });
//         });
//     });
//
//     // Обработчик кнопки рестарта
//     restartButton.addEventListener('click', function () {
//         fetch('/game/restart', {
//             method: 'POST',
//             headers: {
//                 'Content-Type': 'application/x-www-form-urlencoded',
//             },
//             body: ''
//         })
//             .then(response => response.json())
//             .then(data => {
//                 // Сброс игрового поля
//                 updateBoard(data.board);
//                 // Скрытие сообщения о конце игры
//                 gameMessage.style.display = 'none';
//                 gameResult.textContent = '';
//                 // Обновление текущего игрока
//                 currentPlayerSpan.textContent = data.currentPlayer === 'cross' ? 'Крестик' : 'Нолик';
//             })
//             .catch(error => {
//                 console.error('Ошибка:', error);
//                 alert('Произошла ошибка при рестарте игры.');
//             });
//     });
//
//     function updateBoard(board) {
//         for (let i = 1; i <= 3; i++) {
//             for (let j = 1; j <= 3; j++) {
//                 const cell = document.querySelector(`.cell[data-row='${i}'][data-col='${j}'] span`);
//                 if (board[i - 1][j - 1] === 'cross') {
//                     cell.textContent = '✖️';
//                 } else if (board[i - 1][j - 1] === 'nought') {
//                     cell.textContent = '⭕';
//                 } else {
//                     cell.textContent = '';
//                 }
//             }
//         }
//     }
// });
