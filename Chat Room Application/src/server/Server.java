package server;

import common.ClientManager;
import common.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected to the server!");
                ClientManager manageClient = new ClientManager(socket);
                Thread thread = new Thread(manageClient);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void closeServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(Config.portNo);
        Server server = new Server(serverSocket);
        server.startServer();


    }
}
