package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.*;
import com.shubchynskyi.tictactoeapp.domain.OnlineGame;
import com.shubchynskyi.tictactoeapp.dto.LeaveGameMessage;
import com.shubchynskyi.tictactoeapp.dto.OnlineGameMessage;
import com.shubchynskyi.tictactoeapp.dto.RematchMessage;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import com.shubchynskyi.tictactoeapp.service.OnlineGameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static com.shubchynskyi.tictactoeapp.TestsConstant.*;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OnlineGameControllerTest {

    @Autowired
    private OnlineGameService onlineGameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        onlineGameService.getGames().clear();
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        reset(messagingTemplate);
    }

    @Test
    @DisplayName("Should show online games page and model attribute when GET /online is called")
    void shouldShowOnlineGamesPage() throws Exception {
        mockMvc.perform(get(Route.ONLINE))
                .andExpect(status().isOk())
                .andExpect(view().name(View.ONLINE))
                .andExpect(model().attribute(SessionAttributes.GAMES, instanceOf(List.class)));
    }

    @Test
    @DisplayName("Should create a new online game and redirect when POST /createOnline is called")
    void shouldCreateNewOnlineGameAndRedirect() throws Exception {
        MockHttpSession session = createSession(FIRST_USER_ID, FIRST_USER_NAME);

        mockMvc.perform(post(Route.CREATE_ONLINE).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(PATTERN_ONLINE_GAME_ANY_GAME_ID));

        assertEquals(1, onlineGameService.listGames().size());
        OnlineGame created = onlineGameService.listGames().getFirst();
        assertEquals(FIRST_USER_ID, created.getCreatorId());
        assertEquals(FIRST_USER_NAME, created.getCreatorDisplay());

        verify(messagingTemplate, atLeastOnce())
                .convertAndSend(eq(Route.TOPIC_GAME_LIST), anyList());
    }

    @Test
    @DisplayName("Should let second user join game and redirect when GET /joinOnline is called")
    void shouldJoinOnlineGameAndRedirect() throws Exception {
        long gameId = createGame();

        MockHttpSession session = createSession(SECOND_USER_ID, SECOND_USER_NAME);

        mockMvc.perform(get(Route.JOIN_ONLINE)
                        .param(RequestParams.GAME_ID, String.valueOf(gameId))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(PATTERN_ONLINE_GAME_GAME_ID + gameId));

        OnlineGame onlineGame = onlineGameService.getOnlineGame(gameId);
        assertNotNull(onlineGame);
        assertFalse(onlineGame.isWaitingForSecondPlayer());

        verify(messagingTemplate, atLeastOnce())
                .convertAndSend(eq(Route.TOPIC_GAME_LIST), anyList());
    }

    @Test
    @DisplayName("Should redirect to /online if game not found on GET /onlineGame")
    void shouldRedirectIfGameNotFound() throws Exception {
        mockMvc.perform(get(Route.ONLINE_GAME).param(RequestParams.GAME_ID, String.valueOf(INCORRECT_GAME_ID)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Route.ONLINE));
    }

    @Test
    @DisplayName("Should open onlineGame page if user is a participant")
    void shouldOpenOnlineGamePageIfParticipant() throws Exception {
        long gameId = createGame();

        MockHttpSession session = createSession(FIRST_USER_ID, FIRST_USER_NAME);

        mockMvc.perform(get(Route.ONLINE_GAME)
                        .param(RequestParams.GAME_ID, String.valueOf(gameId))
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name(View.ONLINE_GAME))
                .andExpect(model().attribute(SessionAttributes.ONLINE_GAME, notNullValue()));
    }

    @Test
    @DisplayName("Should block user if not participant and game not waiting for second player on GET /onlineGame")
    void shouldBlockIfNotParticipant() throws Exception {
        long gameId = createGame();
        joinGame(gameId, SECOND_USER_ID, SECOND_USER_NAME);

        MockHttpSession session = createSession(THIRD_USER_ID, THIRD_USER_NAME);

        mockMvc.perform(get(Route.ONLINE_GAME)
                        .param(RequestParams.GAME_ID, String.valueOf(gameId))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Route.ONLINE));
    }

    @Test
    @DisplayName("Should return game state as JSON when GET /onlineState is called")
    void shouldReturnOnlineGameState() throws Exception {
        long gameId = createGame();

        mockMvc.perform(get(Route.ONLINE_STATE).param(RequestParams.GAME_ID, String.valueOf(gameId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creatorId").value(FIRST_USER_ID));
    }

    @Test
    @DisplayName("Should make a move and send updated game when handleOnlineMove is called")
    void shouldMakeMoveAndSendUpdatedGame() {
        long gameId = createGame();
        joinGame(gameId, FIRST_USER_ID, FIRST_USER_NAME);

        OnlineGameController controller = getController();

        OnlineGameMessage message = new OnlineGameMessage();
        message.setGameId(gameId);
        message.setUserId(FIRST_USER_ID);
        message.setRow(0);
        message.setCol(0);

        controller.handleOnlineMove(message);

        OnlineGame onlineGame = onlineGameService.getOnlineGame(gameId);
        assertNotNull(onlineGame);
        assertEquals(Sign.CROSS, onlineGame.getGame().getSignAt(0));

        ArgumentCaptor<OnlineGame> captor = ArgumentCaptor.forClass(OnlineGame.class);
        verify(messagingTemplate)
                .convertAndSend(eq(Route.TOPIC_ONLINE_GAME_PREFIX + gameId), captor.capture());
        assertEquals(gameId, captor.getValue().getGameId());
    }

    @Test
    @DisplayName("Should reset finished game and send it when handleRematch is called")
    void shouldRematchGameAndSendUpdate() {
        long gameId = createGame();
        OnlineGame onlineGame = onlineGameService.getOnlineGame(gameId);
        onlineGame.setFinished(true);

        OnlineGameController controller = getController();
        RematchMessage message = new RematchMessage();
        message.setGameId(gameId);

        controller.handleRematch(message);
        assertFalse(onlineGame.isFinished());

        verify(messagingTemplate, atLeastOnce())
                .convertAndSend(eq(Route.TOPIC_ONLINE_GAME_PREFIX + gameId), any(OnlineGame.class));
    }

    @Test
    @DisplayName("Should handle leaving game properly and send CLOSED if creator leaves")
    void shouldHandleLeaveGame() {
        long gameId = createGame();
        joinGame(gameId, SECOND_USER_ID, SECOND_USER_NAME);

        OnlineGameController controller = getController();
        LeaveGameMessage leaveMessage = new LeaveGameMessage();
        leaveMessage.setGameId(gameId);
        leaveMessage.setUserId(SECOND_USER_ID);

        controller.handleLeaveGame(leaveMessage);
        OnlineGame og = onlineGameService.getOnlineGame(gameId);
        assertNotNull(og);
        assertTrue(og.isWaitingForSecondPlayer());

        leaveMessage.setUserId(FIRST_USER_ID);
        controller.handleLeaveGame(leaveMessage);
        assertNull(onlineGameService.getOnlineGame(gameId));

        verify(messagingTemplate, atLeastOnce())
                .convertAndSend(eq(Route.TOPIC_GAME_LIST), anyList());

        verify(messagingTemplate, atLeastOnce())
                .convertAndSend(eq(Route.TOPIC_ONLINE_GAME_PREFIX + gameId), eq(WebSocketCommand.CLOSED));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public SimpMessagingTemplate mockSimpMessagingTemplate() {
            return mock(SimpMessagingTemplate.class);
        }
    }

    private MockHttpSession createSession(String userId, String nick) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAttributes.USER_ID, userId);
        session.setAttribute(SessionAttributes.NICK, nick);
        return session;
    }

    private long createGame() {
        return onlineGameService.createGame(FIRST_USER_ID, FIRST_USER_NAME);
    }

    private void joinGame(long gameId, String userId, String displayName) {
        onlineGameService.joinGame(gameId, userId, displayName);
    }

    private OnlineGameController getController() {
        return wac.getBean(OnlineGameController.class);
    }
}
