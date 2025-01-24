package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.RequestParams;
import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.SessionAttributes;
import com.shubchynskyi.tictactoeapp.constants.View;
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

import static com.shubchynskyi.tictactoeapp.constants.Key.SINGLE_GAME_MOD;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GameControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    @DisplayName("Should start a new game if none in session and return 'game' view")
    void shouldStartNewGameIfNoneInSession() throws Exception {
        mockMvc.perform(get(Route.START_GAME)
                        .param(RequestParams.GAME_MODE, SINGLE_GAME_MOD)
                        .param(RequestParams.PLAYER_SYMBOL, Sign.CROSS.getSign())
                        .param(RequestParams.DIFFICULTY, Difficulty.EASY.getValue())
                )
                .andExpect(status().isOk())
                .andExpect(view().name(View.GAME))
                .andExpect(model().attributeExists(SessionAttributes.LOCAL_GAME));
    }

    @Test
    @DisplayName("Should reuse existing game from session instead of creating a new one")
    void shouldReuseExistingGame() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Game existingGame = new Game();
        session.setAttribute(SessionAttributes.LOCAL_GAME, existingGame);

        mockMvc.perform(get(Route.START_GAME)
                        .param(RequestParams.GAME_MODE, SINGLE_GAME_MOD)
                        .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(view().name(View.GAME))
                .andExpect(model().attribute(SessionAttributes.LOCAL_GAME, sameInstance(existingGame)));

        Game fromSession = (Game) session.getAttribute(SessionAttributes.LOCAL_GAME);
        assertSame(existingGame, fromSession);
    }
}