package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.TestsConstant;
import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.SessionAttributes;
import com.shubchynskyi.tictactoeapp.constants.View;
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

import static com.shubchynskyi.tictactoeapp.TestsConstant.FIRST_USER_ID;
import static com.shubchynskyi.tictactoeapp.TestsConstant.FIRST_USER_NAME;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MainControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    @DisplayName("Should remove local game and set default session attributes, return index")
    void shouldRemoveLocalGameAndSetDefaults() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAttributes.LOCAL_GAME, TestsConstant.SOME_OLD_GAME);
        session.setAttribute(SessionAttributes.NICK, FIRST_USER_NAME);

        mockMvc.perform(get(Route.INDEX).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name(View.INDEX))
                .andExpect(request().sessionAttribute(SessionAttributes.LOCAL_GAME, nullValue()))
                .andExpect(request().sessionAttribute(SessionAttributes.USER_ID, notNullValue()))
                .andExpect(request().sessionAttribute(SessionAttributes.NICK, FIRST_USER_NAME))
                .andExpect(request().sessionAttribute(SessionAttributes.LAST_SYMBOL, is(Sign.CROSS.getSign())))
                .andExpect(request().sessionAttribute(SessionAttributes.LAST_DIFF, is(Difficulty.EASY.getValue())));
    }

    @Test
    @DisplayName("Should keep existing session attributes if they are already set")
    void shouldKeepExistingSessionAttributes() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAttributes.LOCAL_GAME, TestsConstant.SOME_OLD_GAME);
        session.setAttribute(SessionAttributes.USER_ID, FIRST_USER_ID);
        session.setAttribute(SessionAttributes.NICK, FIRST_USER_NAME);
        session.setAttribute(SessionAttributes.LAST_SYMBOL, Sign.NOUGHT.getSign());
        session.setAttribute(SessionAttributes.LAST_DIFF, Difficulty.HARD.getValue());

        mockMvc.perform(get(Route.INDEX).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name(View.INDEX))
                .andExpect(request().sessionAttribute(SessionAttributes.LOCAL_GAME, nullValue()))
                .andExpect(request().sessionAttribute(SessionAttributes.USER_ID, is(FIRST_USER_ID)))
                .andExpect(request().sessionAttribute(SessionAttributes.NICK, is(FIRST_USER_NAME)))
                .andExpect(request().sessionAttribute(SessionAttributes.LAST_SYMBOL, is(Sign.NOUGHT.getSign())))
                .andExpect(request().sessionAttribute(SessionAttributes.LAST_DIFF, is(Difficulty.HARD.getValue())));
    }

}