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

    private static long clientIdCounter = 1L;
    private static Map<Long, SocketWrapper> clients = new HashMap<>();


    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту " + PORT);
            while (true) {
                final Socket client = server.accept();
                final long clientId = clientIdCounter++;

                SocketWrapper wrapper = new SocketWrapper(clientId, client);
                if (!wrapper.isAdmin()) {
                    System.out.println("Подключился новый клиент [" + wrapper + "]");
                    clients.put(clientId, wrapper);
                    startClientThread(wrapper, clientId);
                } else {
                    System.out.println("Подключился администратор [" + wrapper + "]");
                    clients.put(clientId, wrapper);
                    startAdminThread(wrapper, clientId);
                }


            }
        }
    }

    private static void startClientThread(SocketWrapper wrapper, long clientId) {
        new Thread(() -> {
            try (Scanner input = wrapper.getInput(); PrintWriter output = wrapper.getOutput()) {
                output.println("Подключение успешно. Список всех клиентов: " + clients);

                while (true) {
                    String clientInput = input.nextLine();
                    if (Objects.equals("q", clientInput)) {
                        // todo разослать это сообщение всем остальным клиентам
                        clients.remove(clientId);
                        clients.values().forEach(it -> it.getOutput().println("Клиент[" + clientId + "] отключился"));
                        break;
                    }

                    // формат сообщения: "@цифра сообщение"
                    sendMessage(clientInput, clients);
                }
            }
        }).start();
    }

    private static void startAdminThread(SocketWrapper wrapper, long clientId) {
        new Thread(() -> {
            try (Scanner input = wrapper.getInput(); PrintWriter output = wrapper.getOutput()) {
                output.println("Подключение успешно. Список всех клиентов: " + clients);

                while (true) {
                    String clientInput = input.nextLine();
                    if (Objects.equals("q", clientInput)) {
                        // todo разослать это сообщение всем остальным клиентам
                        clients.remove(clientId);
                        clients.values().forEach(it -> it.getOutput().println("Администратор[" + clientId + "] отключился"));
                        break;
                    }
                    if (clientInput.matches("(?i)Kick \\d+")) {
                        String[] splitInput = clientInput.split(" ");
                        try {
                            clients.get(Long.parseLong(splitInput[1])).close();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        sendMessage(clientInput, clients);
                    }

                }
            }
        }).start();
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
