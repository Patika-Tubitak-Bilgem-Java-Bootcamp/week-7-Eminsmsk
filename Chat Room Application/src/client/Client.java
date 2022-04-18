package client;

import common.Handler;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Handler {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username) {

        try {
            this.socket = socket;
            this.username = username;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            kill(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage(){
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while(socket.isConnected()){
                String messageToSend = scanner.nextLine();
                bufferedWriter.write("[" + username + "]: " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            kill(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromChatRoom;

                while(socket.isConnected()){
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
        Socket socket = new Socket("localhost", 1111);
        Client client = new Client(socket, username);
        client.listenForMessage(); //thread1 handles infinite while to receive messages
        client.sendMessage(); //thread2 handles infinite while to send messages
    }
}
