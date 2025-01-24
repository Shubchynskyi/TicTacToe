package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.RequestParams;
import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.SessionAttributes;
import com.shubchynskyi.tictactoeapp.domain.Game;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Objects;

import static com.shubchynskyi.tictactoeapp.TestsConstant.CROSS_VALUE;
import static com.shubchynskyi.tictactoeapp.TestsConstant.EMPTY_VALUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MoveControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    private MockHttpSession session;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        session = new MockHttpSession();
    }

    @Test
    @DisplayName("Should make a move and return updated game in JSON")
    void shouldMakeMoveAndReturnGame() throws Exception {
        Game game = new Game();
        session.setAttribute(SessionAttributes.LOCAL_GAME, game);

        mockMvc.perform(get(Route.MAKE_MOVE)
                        .param(RequestParams.ROW, "0")
                        .param(RequestParams.COL, "0")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board[0]").value(CROSS_VALUE))
                .andExpect(jsonPath("$.gameOver").value(false));

        Game updated = (Game) session.getAttribute(SessionAttributes.LOCAL_GAME);
        assertEquals(Sign.CROSS, Objects.requireNonNull(updated).getBoard()[0]);
    }

    @Test
    @DisplayName("Should restart local game and return a cleared board in JSON")
    void shouldRestartLocalGame() throws Exception {
        Game game = new Game();
        game.setSignAt(0, Sign.CROSS);
        game.setSignAt(1, Sign.NOUGHT);
        session.setAttribute(SessionAttributes.LOCAL_GAME, game);

        mockMvc.perform(get(Route.RESTART_LOCAL).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board[0]").value(EMPTY_VALUE))
                .andExpect(jsonPath("$.board[1]").value(EMPTY_VALUE))
                .andExpect(jsonPath("$.gameOver").value(false));

        Game updatedGame = (Game) session.getAttribute(SessionAttributes.LOCAL_GAME);
        assertNotNull(updatedGame);
        assertTrue(Arrays.stream(updatedGame.getBoard()).allMatch(s -> s == Sign.EMPTY));
    }

    @Test
    @DisplayName("Should create a new game if none in session and then make a move")
    void shouldCreateNewGameIfNoneInSession() throws Exception {
        mockMvc.perform(get(Route.MAKE_MOVE)
                        .param(RequestParams.ROW, "1")
                        .param(RequestParams.COL, "1")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board[4]").value(CROSS_VALUE));

        Game created = (Game) session.getAttribute(SessionAttributes.LOCAL_GAME);
        assertNotNull(created);
        assertEquals(Sign.CROSS, created.getBoard()[4]);
    }
}
