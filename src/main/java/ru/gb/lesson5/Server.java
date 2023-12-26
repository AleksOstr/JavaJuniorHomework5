package ru.gb.lesson5;

import lombok.Getter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Server {

    // message broker (kafka, redis, rabbitmq, ...)
    // client sent letter to broker

    // server sent to SMTP-server

    public static final int PORT = 8181;
    private static final String ADMIN_PASSWORD = "I_am_the_boss";

    private static long clientIdCounter = 1L;
    private static Map<Long, SocketWrapper> clients = new HashMap<>();

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server started at Port " + PORT);
            while (true) {
                final Socket client = server.accept();
                final long clientId = clientIdCounter++;

                SocketWrapper wrapper = new SocketWrapper(clientId, client, false);
                clients.put(clientId, wrapper);
                startClientThread(wrapper, clientId);
            }
        }
    }

    private static void startClientThread(SocketWrapper wrapper, long clientId) {
        new Thread(() -> {
            try (Scanner input = wrapper.getInput(); PrintWriter output = wrapper.getOutput()) {
                output.println("Enter administrator password");
                String inputPassword = input.nextLine();
                if (Objects.equals(inputPassword, Server.ADMIN_PASSWORD)) {
                    wrapper.setAdmin(true);
                    sendMessage("Administrator[" + wrapper + "] connected to server", clients);
                    output.println("Connection successful. Contacts list: " + clients);
                    while (true) {
                        String clientInput = input.nextLine();
                        if (checkForExit(clientInput, clientId)) break;
                        if (clientInput.matches("(?i)Kick \\d+")) {
                            String[] splitInput = clientInput.split(" ");
                            try {
                                long targetId = Long.parseLong(splitInput[1]);
                                SocketWrapper target = clients.get(targetId);
                                target.close();
                                sendMessage("Client [" + target + "] has been kicked by administrator", clients);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            sendMessage(clientInput, clients);
                        }
                    }
                } else {
                    output.println("Connection successful. Contacts list: " + clients);
                    while (true) {
                        String clientInput = input.nextLine();
                        if (checkForExit(clientInput, clientId)) break;
                        // формат сообщения: "@цифра сообщение"
                        sendMessage(clientInput, clients);
                    }
                }
            }
        }).start();
    }

    private static boolean checkForExit(String input, long id) {
        if (Objects.equals("q", input)) {
            clients.remove(input);
            clients.values().forEach(it -> it.getOutput().println("Client[" + id + "] disconnected"));
            return true;
        }
        return false;
    }

    private static boolean checkForPrivateMessage(String input) {
        String[] splitInput = input.split(" ");
        return splitInput[0].matches("@\\d+");
    }
    // формат сообщения: "@цифра сообщение"
    private static void sendMessage(String clientInput, Map<Long, SocketWrapper> clients) {
        if (checkForPrivateMessage(clientInput)) {
            String[] splitInput = clientInput.split(" ");
            long destinationId = Long.parseLong(splitInput[0].replace("@", ""));
            clients.get(destinationId).getOutput().println(clientInput);
        } else {
            clients.values().forEach(it -> it.getOutput().println(clientInput));
        }
    }
}
