package de.ostfalia.snakecore.ws.client;

import de.ostfalia.snakecore.model.RunningGame;
import de.ostfalia.snakecore.model.Spieler;
import de.ostfalia.snakecore.ws.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.LinkedList;

/**
 * @author Benjamin Wulfert
 *
 * Der StompClient ist eine Software-Komponente welche die Kommunikation mittels Backend via WebSockets / STOMP-Protokoll ermöglicht.
 */
public class StompClient extends StompSessionHandlerAdapter implements StompMessageListener {

    // ws://localhost:13373/snakeserver/
    private String connectionUrl;

    private StompSessionHandler sessionHandler;

    private Logger logger = LogManager.getLogger(StompSessionHandler.class);

    private StompSession session;
    private StompMessageListener stompMessageListener;

    /**
     * Check wether the connection has already been established or not
     * @return
     */
    public boolean isConnected(){
        return session != null || ((session != null && session.isConnected()));
    }

    public void connect(String url, Runnable onSuccessRunnable) {
        this.connectionUrl = url;

        // initialize the clients
        WebSocketClient wsClient = new StandardWebSocketClient();
        WebSocketStompClient wsStompClient = new WebSocketStompClient(wsClient);

        // register a jackson 2 message converter
        wsStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // instantiate the session handler in order to communicate over STOMP
        sessionHandler = this;

        // CONNECT to the Stomp server using the url and the sessionHandler
        ListenableFuture<StompSession> connect = wsStompClient.connect(connectionUrl, sessionHandler);

        connect.addCallback(new ListenableFutureCallback<StompSession>() {
            @Override
            public void onFailure(Throwable throwable) {
                // TODO: indicate error
            }

            @Override
            public void onSuccess(StompSession stompSession) {
                session = stompSession;
                onSuccessRunnable.run();
            }
        });

        // register a listener for incoming messages
        setStompMessageListener(this);

    }


    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.session = session;

        logger.info("New session established : " + session.getSessionId());

        // subscribe to incoming chat messages
        session.subscribe("/topic/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {

                // System.out.println("Received message: " + headers.getMessageId() + " | " + headers.getDestination());

                ChatMessage msg = (ChatMessage) payload;
                if (msg != null) {
                    // System.out.println("Received ChatMessage: " + msg.getText() + " from : " + msg.getFrom());
                    stompMessageListener.onChatMessageReceived(msg);
                }

            }
        });
        logger.info("Subscribed to " + "/topic/messages");

        // subscribe to game changes
        session.subscribe("/topic/games", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LobbyMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                // System.out.println("Received message: " + headers.getMessageId() + " | " + headers.getDestination());

                LobbyMessage msg = (LobbyMessage) payload;
                if (msg != null) {
                    // System.out.println("Received LobbyMessage: " + msg.toString());
                    stompMessageListener.onLobbyMessageReceived(msg);
                }
            }
        });
        logger.info("Subscribed to " + "/topic/games");


        // subscribe to newly joined players
        session.subscribe("/topic/players", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return PlayerMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                // System.out.println("Received message: " + headers.getMessageId() + " | " + headers.getDestination());

                PlayerMessage msg = (PlayerMessage) payload;
                if (msg != null) {
                    // System.out.println("Received PlayerMessage: " + msg.toString());
                    stompMessageListener.onPlayerMessageReceived(msg);
                }
            }
        });
        logger.info("Subscribed to " + "/topic/players");


        // subscribe to newly joined players
        session.subscribe("/topic/games/{gameId}/{playerId}", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return PlayerJoinsGameMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                // System.out.println("Received message: " + headers.getMessageId() + " | " + headers.getDestination());

                PlayerJoinsGameMessage msg = (PlayerJoinsGameMessage) payload;
                if (msg != null) {
                    // System.out.println("Received onPlayerJoinedGameMessageReceived: " + msg.toString());
                    stompMessageListener.onPlayerJoinedGameMessageReceived(msg);
                }
            }
        });
        logger.info("Subscribed to " + "/topic/players");



        /*
        session.send(ProjectEndpoints.STOMP_APP_PREFIX + ProjectEndpoints.STOMP_MESSAGE_MAPPING_CHAT, getSampleMessage());
        logger.info("Message sent to websocket server");
        */
    }

    public void sendChatMessage(String user, String message) {
        // System.out.println("Sending message to /app/chat ... Message: " + message);
        session.send("/app/chat", new ChatMessage(user, message));
    }

    public void sendLobbyMessage(LobbyMessage lobbyMessage) {
        // System.out.println("Sending lobby message");
        session.send("/app/games", lobbyMessage);
    }

    public void sendNewPlayerMessage(PlayerMessage playerMessage){
        // System.out.println("Sending player message");
        session.send("/app/players", playerMessage);
    }

    public void sendGameInputMessage(String stompPath, GameSessionMessage gameInputMessage) {
        // System.out.println("Sending player message to " + stompPath);
        session.send("/app/games/1", gameInputMessage);
    }

    public void sendJoinGameMessage(String stompPath, Spieler spieler, RunningGame runningGame) {
        // System.out.println("Sending joinGame message ");
        String playerStompPath = "/app/games/1" + "/" + spieler.getName();
        //  System.out.println("\t to path: " + playerStompPath);

        session.send(playerStompPath, new PlayerJoinsGameMessage(spieler, runningGame, new LinkedList<>()));
    }


    /**
     * Uses a lobby message to disconnect a player from the server and do cleanup tasks
     * (like removing the player from the lobby, etc.)
     *
     * @param spieler
     */
    public void sendLogoutMessage(Spieler spieler) {
        LobbyMessage lobbyMessage = new LobbyMessage();

        lobbyMessage.logout = true;
        lobbyMessage.logoutSpieler = spieler;

        session.send("/app/games", lobbyMessage);
    }

    /**
     * Sends a message to a game topic with gameDestinationTopic
     *
     * @param gameDestinationTopic
     */
    public void subscribeToGameTopic(String gameDestinationTopic) {

        session.subscribe(gameDestinationTopic, new StompFrameHandler() {

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return GameSessionMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                GameSessionMessage msg = (GameSessionMessage) payload;
                if (stompMessageListener != null) {
                    stompMessageListener.onGameSessionMessageReceived(msg);
                }
            }
        });
    }

    public void setStompMessageListener(StompMessageListener recievedCallback) {
        this.stompMessageListener = recievedCallback;
    }

    @Override
    public void onChatMessageReceived(ChatMessage msg) {
        if (stompMessageListener != null) {
            stompMessageListener.onChatMessageReceived(msg);
        }
    }

    @Override
    public void onLobbyMessageReceived(LobbyMessage msg) {
        if (stompMessageListener != null) {
            stompMessageListener.onLobbyMessageReceived(msg);
        }
    }

    @Override
    public void onPlayerMessageReceived(PlayerMessage msg) {
        if (stompMessageListener != null) {
            stompMessageListener.onPlayerMessageReceived(msg);
        }
    }

    @Override
    public void onGameSessionMessageReceived(GameSessionMessage msg) {
        if (stompMessageListener != null) {
            stompMessageListener.onGameSessionMessageReceived(msg);
        }
    }

    @Override
    public void onPlayerJoinedGameMessageReceived(PlayerJoinsGameMessage msg) {
        if (stompMessageListener != null) {
            stompMessageListener.onPlayerJoinedGameMessageReceived(msg);
        }
    }

}