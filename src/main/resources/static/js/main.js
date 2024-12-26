// document.addEventListener('DOMContentLoaded', function () {
//     const gameModeSelect = document.getElementById('gameMode');
//     const difficultyControl = document.getElementById('difficultyControl');
//     const signControl = document.getElementById('signControl');
//
//     function toggleFields() {
//         if (gameModeSelect.value === 'multiplayer') {
//             difficultyControl.style.display = 'none';
//             signControl.style.display = 'none';
//         } else {
//             difficultyControl.style.display = 'block';
//             signControl.style.display = 'block';
//         }
//     }
//
//     // Инициализация при загрузке страницы
//     toggleFields();
//
//     // Добавление слушателя событий для изменения значения
//     gameModeSelect.addEventListener('change', toggleFields);
// });