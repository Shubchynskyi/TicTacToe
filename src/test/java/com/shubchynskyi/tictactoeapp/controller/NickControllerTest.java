package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.TestsConstant;
import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.SessionAttributes;
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

import static com.shubchynskyi.tictactoeapp.TestsConstant.FIRST_USER_NAME;
import static com.shubchynskyi.tictactoeapp.TestsConstant.SECOND_USER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class NickControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    @DisplayName("Should update nick if newNick is valid")
    void shouldUpdateNickIfValid() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAttributes.NICK, FIRST_USER_NAME);

        mockMvc.perform(get(Route.UPDATE_NICK)
                        .param(SessionAttributes.NICK, SECOND_USER_NAME)
                        .session(session))
                .andExpect(status().isOk());

        assertEquals(SECOND_USER_NAME, session.getAttribute(SessionAttributes.NICK));
    }

    @Test
    @DisplayName("Should not update nick if blank")
    void shouldNotUpdateNickIfBlank() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAttributes.NICK, FIRST_USER_NAME);

        mockMvc.perform(get(Route.UPDATE_NICK)
                        .param(SessionAttributes.NICK, TestsConstant.EMPTY_USER_NAME)
                        .session(session))
                .andExpect(status().isOk());

        assertEquals(FIRST_USER_NAME, session.getAttribute(SessionAttributes.NICK));
    }
}
