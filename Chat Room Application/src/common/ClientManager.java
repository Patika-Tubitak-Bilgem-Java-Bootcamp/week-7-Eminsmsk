package common;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

//Class that handles clients by using threads
public class ClientManager implements Runnable, Handler {

    public static final TreeMap<String, List<ClientManager>> clientManagers = new TreeMap<>();
    private Socket socket;
    private BufferedReader bufferedReader; // to read messages
    private BufferedWriter bufferedWriter; // to send messages
    private String clientUsername;
    private String roomName;

    public ClientManager(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUsername = bufferedReader.readLine();
            this.roomName = bufferedReader.readLine();
            if (!clientManagers.containsKey(this.roomName)) {
                clientManagers.put(this.roomName, new ArrayList<>());
            }
            clientManagers.get(this.roomName).add(this);
            broadcastMessage("[SERVER]: " + clientUsername + " has entered the chat!");
            listClients();
        } catch (IOException e) {
            kill(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                kill(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        List<ClientManager> clientsInRoom = clientManagers.get(this.roomName);
        for (ClientManager clientManager : clientsInRoom) {
            try {
                if (!clientManager.clientUsername.equalsIgnoreCase(clientUsername)) {
                    clientManager.bufferedWriter.write(messageToSend);
                    clientManager.bufferedWriter.newLine(); // for new line character AKA enter
                    clientManager.bufferedWriter.flush();
                }
            } catch (IOException e) {
                deleteClientManager();
                kill(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public String getClients() {
        List<ClientManager> clientsInRoom = clientManagers.get(this.roomName);
        List<String> clients = clientsInRoom.stream().map(ClientManager::getClientUsername).toList();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[SERVER]: ").append(clients.size()).append(" clients available in this chat room!\n").append("[SERVER]: Available users: ");
        clients.forEach(client -> stringBuilder.append(client).append(" "));
        return stringBuilder.toString();
    }

    public void listClients() {
        try {
            this.bufferedWriter.write(getClients());
            this.bufferedWriter.newLine(); // for new line character AKA enter
            this.bufferedWriter.flush();

        } catch (IOException e) {
            deleteClientManager();
            kill(socket, bufferedReader, bufferedWriter);
        }

    }

    public void deleteClientManager() {
        clientManagers.remove(this);
        broadcastMessage("[SERVER]: " + clientUsername + " has left the chat!");
    }

    public String getClientUsername() {
        return clientUsername;
    }

}
