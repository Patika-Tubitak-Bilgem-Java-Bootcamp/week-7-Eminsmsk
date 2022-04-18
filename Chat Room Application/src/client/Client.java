package client;

import common.ClientManager;
import common.Config;
import common.Handler;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Handler {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private String roomName;

    public Client(Socket socket, String username, String roomName) {

        try {
            this.socket = socket;
            this.username = username;
            this.roomName = roomName;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            kill(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            bufferedWriter.write(roomName);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write("[" + username + "]: " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            kill(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromChatRoom;

                while (socket.isConnected()) {
                    try {
                        messageFromChatRoom = bufferedReader.readLine();
                        System.out.println(messageFromChatRoom);
                    } catch (IOException e) {
                        kill(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username for the chat room: ");
        String username = scanner.nextLine();
        System.out.print("Enter (J) to join a chat room OR (C) to create a chat room: ");
        String choice = scanner.nextLine();
        String roomName;

        // if room does not exist it automaticaly creates with given name, if it does exist then it automatically let the user join that room
        while (true) {
            if ("J".equalsIgnoreCase(choice)) {
                System.out.print("Enter chat room name: ");
                roomName = scanner.nextLine();
                break;
            } else if ("C".equalsIgnoreCase(choice)) {
                System.out.print("Enter chat room name to create: ");
                roomName = scanner.nextLine();
                break;
            } else {
                System.out.println("Wrong Input!");
            }
            System.out.print("Enter (J) to join a chat room OR (C) to create a chat room: ");
            choice = scanner.nextLine();
        }
        Socket socket = new Socket(Config.host, Config.portNo);
        Client client = new Client(socket, username, roomName);
        client.listenForMessage(); //thread1 handles infinite while to receive messages
        client.sendMessage(); //thread2 handles infinite while to send messages
    }
}
