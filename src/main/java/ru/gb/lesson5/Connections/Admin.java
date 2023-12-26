package ru.gb.lesson5.Connections;

import ru.gb.lesson5.Server;

import java.io.IOException;
import java.net.Socket;

public class Admin extends Client{

    public static void main(String[] args) throws IOException {
        Socket admin = new Socket("localhost", Server.PORT);
        startInputThread(admin);
        startOutputThread(admin);
    }

}
