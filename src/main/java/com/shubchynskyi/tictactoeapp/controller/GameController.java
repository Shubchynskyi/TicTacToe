package com.shubchynskyi.tictactoeapp.controller;


import com.shubchynskyi.tictactoeapp.model.Game;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Создание новой локальной игры по "/start",
 * затем кладём объект Game в HttpSession (назовём "localGame"),
 * и рендерим game.html
 */
@Controller
public class GameController {

    @GetMapping("/start")
    public String startGame(
            @RequestParam("gameMode") String gameMode,
            @RequestParam(value="playerSymbol", required=false) String playerSymbol,
            @RequestParam(value="difficulty", required=false) String difficulty,
            HttpSession session,
            Model model
    ) {
        // Создаём Game
        Game game = new Game(gameMode, playerSymbol, difficulty);
        // Храним в сессии
        session.setAttribute("localGame", game);

        // Передаём в модель (можно, чтобы game.html отобразил)
        model.addAttribute("game", game);
        return "game"; // game.html
    }
}

//@Controller
//public class GameController {
//
//    @GetMapping("/start")
//    public String startGame(
//            @RequestParam("gameMode") String gameMode,
//            @RequestParam(value = "playerSymbol", required = false) String playerSymbol,
//            @RequestParam(value = "difficulty", required = false) String difficulty,
//            HttpSession session,
//            Model model
//    ) {
//        Game game = new Game(gameMode, playerSymbol, difficulty);
//        session.setAttribute("localGame", game);
//        model.addAttribute("game", game);
//        return "game";
//    }
//}


//@Controller
//@RequestMapping("/game")
//public class GameController {
//
//    @Autowired
//    private MessageSource messageSource;
//
//    // Хранение состояния игры (для простоты в сессии)
//    @Autowired
//    private HttpSession session;
//
//    @PostMapping("/newGame")
//    public String newGame(
//            @RequestParam("gameMode") String gameModeStr,
//            @RequestParam(value = "difficulty", required = false) String difficulty,
//            @RequestParam(value = "sign", required = false) String sign,
//            Model model) {
//
//        // Логика инициализации новой игры
//        String[][] board = new String[3][3];
//        for (String[] row : board) {
//            Arrays.fill(row, "");
//        }
//
//        GameMode gameMode = GameMode.valueOf(gameModeStr);
//        GameState gameState = new GameState(gameMode, difficulty, sign);
//
//        model.addAttribute("gameState", gameState);
//        model.addAttribute("gameMode", gameModeStr);
//        model.addAttribute("difficulty", difficulty);
//        model.addAttribute("sign", sign);
//        model.addAttribute("board", board);
//        model.addAttribute("gameOver", false);
//        model.addAttribute("winner", null);
//
//        return "game"; // Перенаправление на game.html
//    }
//
//    @PostMapping("/makeMove")
//    @ResponseBody
//    public ResponseEntity<GameResponse> makeMove(
//            @RequestParam("row") int row,
//            @RequestParam("col") int col,
//            Locale locale) {
//
//        GameState gameState = (GameState) session.getAttribute("gameState");
//        if (gameState == null || gameState.isGameOver()) {
//            return ResponseEntity.badRequest().body(new GameResponse("Игра не начата или уже завершена."));
//        }
//
//        // Обработка хода пользователя
//        boolean moveAccepted = gameState.makeMove(row, col, gameState.getPlayerSign());
//        if (!moveAccepted) {
//            return ResponseEntity.badRequest().body(new GameResponse("Ячейка уже занята."));
//        }
//
//        // Проверка на победу или ничью
//        if (gameState.checkWin(gameState.getPlayerSign())) {
//            gameState.setGameOver(true);
//            return ResponseEntity.ok(new GameResponse("Победитель: " + (gameState.getPlayerSign().equals("cross") ? "Крестик" : "Нолик")));
//        } else if (gameState.isDraw()) {
//            gameState.setGameOver(true);
//            return ResponseEntity.ok(new GameResponse("Ничья!"));
//        }
//
//        // Генерация хода AI, если игра одиночная
//        if (gameState.getGameMode() == GameMode.SINGLE_PLAYER) {
//            AIService aiService = new AIService();
//            int[] aiMove = aiService.generateMove(gameState, gameState.getDifficulty());
//            if (aiMove != null) {
//                gameState.makeMove(aiMove[0], aiMove[1], gameState.getAiSign());
//
//                // Проверка победы AI
//                if (gameState.checkWin(gameState.getAiSign())) {
//                    gameState.setGameOver(true);
//                    return ResponseEntity.ok(new GameResponse("Победитель: " + (gameState.getAiSign().equals("cross") ? "Крестик" : "Нолик")));
//                } else if (gameState.isDraw()) {
//                    gameState.setGameOver(true);
//                    return ResponseEntity.ok(new GameResponse("Ничья!"));
//                }
//            }
//        }
//
//        // Возвращаем обновленное состояние игры
//        return ResponseEntity.ok(new GameResponse(null, gameState.getBoard(), gameState.isGameOver(), gameState.getWinner()));
//    }
//
//    @PostMapping("/restart")
//    public String restartGame(
//            @RequestParam("gameMode") String gameModeStr,
//            @RequestParam(value = "difficulty", required = false) String difficulty,
//            @RequestParam(value = "sign", required = false) String sign,
//            Model model, Locale locale) {
//
//        GameMode gameMode = GameMode.fromString(gameModeStr);
//
//        // Инициализация новой игры
//        GameState gameState = new GameState(gameMode, difficulty, sign);
//        session.setAttribute("gameState", gameState);
//
//        model.addAttribute("gameMode", gameMode);
//        model.addAttribute("difficulty", difficulty);
//        model.addAttribute("sign", sign);
//        model.addAttribute("gameOver", false);
//        model.addAttribute("winner", null);
//
//        return "game"; // Шаблон game.html
//    }
//
//    // Вспомогательный класс для ответа игры
//    public static class GameResponse {
//        private String message;
//        private String[][] board;
//        private boolean gameOver;
//        private String winner;
//
//        public GameResponse(String message) {
//            this.message = message;
//        }
//
//        public GameResponse(String message, String[][] board, boolean gameOver, String winner) {
//            this.message = message;
//            this.board = board;
//            this.gameOver = gameOver;
//            this.winner = winner;
//        }
//
//        // Геттеры и сеттеры
//        public String getMessage() {
//            return message;
//        }
//
//        public String[][] getBoard() {
//            return board;
//        }
//
//        public boolean isGameOver() {
//            return gameOver;
//        }
//
//        public String getWinner() {
//            return winner;
//        }
//    }
//}

//    private final GameService gameService;
//
//    public GameController(GameService gameService) {
//        this.gameService = gameService;
//    }
//
//    /**
//     * Начинаем новую игру (или показываем текущую, если есть).
//     */
//    @GetMapping("/start")
//    public String startGame(
//            @RequestParam(defaultValue = "cross") String sign,
//            @RequestParam(defaultValue = "easy") String difficulty,
//            HttpSession session,
//            Model model
//    ) {
//        // Инициализация новой игры
//        Game game = gameService.initializeGame(sign, difficulty);
//        session.setAttribute("game", game);
//
//        // Счёт пользователя (из сессии)
//        Integer userScore = (Integer) session.getAttribute("userScore");
//        if (userScore == null) {
//            userScore = 0;
//        }
//        model.addAttribute("userScore", userScore);
//
//        // В модель передаём данные для отображения
//        model.addAttribute("data", game.getFieldData());
//        model.addAttribute("winner", Sign.EMPTY);
//        model.addAttribute("draw", false);
//        model.addAttribute("selectedSign", sign);
//        model.addAttribute("selectedDifficulty", difficulty);
//
//        return "index"; // Thymeleaf шаблон index.html
//    }
//
//    /**
//     * Обрабатываем ход пользователя (клик по ячейке).
//     */
//    @GetMapping("/move")
//    public String playerMove(
//            @RequestParam int cell,
//            HttpSession session,
//            Model model
//    ) {
//        Game game = (Game) session.getAttribute("game");
//        if (game == null) {
//            return "redirect:/game/start"; // Если нет игры в сессии, стартуем
//        }
//
//        // Делаем ход (если клетка пустая)
//        gameService.makeUserMove(game, cell);
//
//        // Проверяем результат
//        Sign winner = game.checkWin();
//        boolean draw = false;
//
//        if (winner == Sign.EMPTY && game.getEmptyFieldIndex() < 0) {
//            draw = true; // ничья
//        }
//
//        // Если нет победителя и не ничья, ход компьютера
//        if (winner == Sign.EMPTY && !draw) {
//            gameService.makeAiMove(game);
//            // Снова проверяем
//            winner = game.checkWin();
//            if (winner == Sign.EMPTY && game.getEmptyFieldIndex() < 0) {
//                draw = true;
//            }
//        }
//
//        // Обновляем счёт (если пользователь выиграл)
//        if (winner == game.getPlayerSign()) {
//            Integer userScore = (Integer) session.getAttribute("userScore");
//            if (userScore == null) userScore = 0;
//            userScore++;
//            session.setAttribute("userScore", userScore);
//        }
//
//        // Возвращаем данные в модель
//        model.addAttribute("data", game.getFieldData());
//        model.addAttribute("winner", winner);
//        model.addAttribute("draw", draw);
//        model.addAttribute("selectedSign", game.getPlayerSign().toString().toLowerCase());
//        model.addAttribute("selectedDifficulty", game.getDifficulty().getSign());
//
//        // Счёт пользователя
//        Integer finalScore = (Integer) session.getAttribute("userScore");
//        model.addAttribute("userScore", finalScore != null ? finalScore : 0);
//
//        return "index";
//    }
//
//    /**
//     * Перезапуск игры (с сохранением выбранной сложности/знака).
//     */
//    @PostMapping("/restart")
//    public String restartGame(
//            @RequestParam(defaultValue = "cross") String sign,
//            @RequestParam(defaultValue = "easy") String difficulty
//    ) {
//        // Простой переход
//        return "redirect:/game/start?sign=" + sign + "&difficulty=" + difficulty;
//    }
//}