package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.SessionAttributes;
import com.shubchynskyi.tictactoeapp.domain.Game;
import com.shubchynskyi.tictactoeapp.enums.Difficulty;
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

import static com.shubchynskyi.tictactoeapp.TestsConstant.*;
import static com.shubchynskyi.tictactoeapp.constants.Key.SINGLE_GAME_MOD;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GameStateControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    @DisplayName("Should return a default game if session has no local game")
    void shouldReturnDefaultGameIfNoLocalGame() throws Exception {
        mockMvc.perform(get(Route.GAME_STATE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board", hasSize(GAME_FIELD_SIZE)))
                .andExpect(jsonPath("$.gameMode", is(SINGLE_GAME_MOD)))
                .andExpect(jsonPath("$.playerSign", is(CROSS_VALUE)))
                .andExpect(jsonPath("$.difficulty", is(DIFFICULT_EASY)))
                .andExpect(jsonPath("$.winner", nullValue()))
                .andExpect(jsonPath("$.gameOver", is(false)));
    }

    @Test
    @DisplayName("Should return existing game from session if present")
    void shouldReturnExistingGameFromSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Game customGame = new Game(MULTIPLAYER_GAME_MODE, Sign.NOUGHT.getSign(), Difficulty.HARD.getValue());
        session.setAttribute(SessionAttributes.LOCAL_GAME, customGame);

        mockMvc.perform(get(Route.GAME_STATE).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameMode", is(MULTIPLAYER_GAME_MODE)))
                .andExpect(jsonPath("$.playerSign", is(NOUGHT_VALUE)))
                .andExpect(jsonPath("$.difficulty", is(DIFFICULT_HARD)));
    }
}