package org.blackjackserver;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class BlackJackPlayer extends CardHolder {
    Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    @Getter @Setter
    private String name;

    @Getter
    private boolean disconnected;

    public BlackJackPlayer(Socket socket)  throws IOException {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Network methods

    public void sendMessage(String message) {
        out.println(message);
    }

    public String readMessage() throws IOException {
        try {
            return in.readLine();
        } catch (SocketException e) {
            System.out.println("Player disconnected" + socket);
            disconnected = true;
            close();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            disconnected = true;
            close();
            return null;
        }
    }
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

}
