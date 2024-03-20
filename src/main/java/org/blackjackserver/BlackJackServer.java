package org.blackjackserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlackJackServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private List<BlackJackPlayer> players;
    private Game game;
    private ExecutorService executor;

    public BlackJackServer() {
        players = new ArrayList<>();
        executor = Executors.newFixedThreadPool(2); // Fixed-size thread pool
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started. Waiting for players...");

            while (true) {
                executor.execute(() -> {
                    try {
                        acceptPlayerConnections();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                if (isReady()) {
                    startGame();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private void acceptPlayerConnections() throws IOException {
        while (players.size() < 2 && !serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();
            System.out.println("Player connected: " + socket);
            BlackJackPlayer player = new BlackJackPlayer(socket);
            assignName(player);
            players.add(player);

            if (isReady()) {
                break;
            }
        }
    }
    public void removeDisconnectedPlayers() {
        Iterator<BlackJackPlayer> iterator = players.iterator();
        while (iterator.hasNext()) {
            BlackJackPlayer player = iterator.next();
            if (player.isDisconnected()) {
                System.out.println("Player disconnected: " + player.getName());
                iterator.remove();
            }
        }
    }

    private void assignName(BlackJackPlayer player) throws IOException {
        player.sendMessage("What is your name?");
        String input = player.readMessage();
        if (input != null) {
            player.setName(input);
            player.sendMessage("Your name is now: " + player.getName());
        }
    }

    private boolean isReady() {
        return players.size() == 2;
    }

    private void startGame() {
        removeDisconnectedPlayers();
        if (isReady()) {
            game = new Game(players);
            game.start();
            players.clear();
        }
    }

    private void shutdown() {
        try {
            serverSocket.close();
            executor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        BlackJackServer server = new BlackJackServer();
        server.start();
    }
}
