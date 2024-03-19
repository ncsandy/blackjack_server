package org.blackjackserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
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
        executor = Executors.newCachedThreadPool();
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
                    game = new Game(players);
                    game.start();
                    players.clear();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private void acceptPlayerConnections() throws IOException {
        try {
            while (players.size() < 2) {
                Socket socket = serverSocket.accept();
                System.out.println("Player connected: " + socket);
                BlackJackPlayer player = new BlackJackPlayer(socket);
                assignName(player);
                players.add(player);

                if (players.size() == 2) {
                    break;  // Exit the loop once two players have joined
                }
            }
        }catch (SocketException s) {
            System.out.println("Client disconnected/Lost Connection");
            shutdown();
            System.exit(0);
        }
    }

    private void assignName(BlackJackPlayer player) throws IOException {

        try {
            while (true) {
                player.sendMessage("What is your name?");
                String input = player.readMessage();
                if (input != null) {
                    player.setName(input);
                    player.sendMessage("Your name is now: " + player.getName());
                    break;
                }
            }
        }catch (SocketException s){
            System.out.println("Client Disconnected/Or lost Connection");
            shutdown();
            System.out.println(s);
        }
    }
    private boolean isReady() {
        return players.size() == 2;
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