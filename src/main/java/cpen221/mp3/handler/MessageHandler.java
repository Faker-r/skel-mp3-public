package cpen221.mp3.handler;

import cpen221.mp3.server.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageHandler {
    private ServerSocket serverSocket;
    private int port;
    private ArrayList<Socket> clients;
    private ArrayList<Socket> entities;
    private Map<Integer, Server> servers;  //Client ID: Server


    // you may need to add additional private fields and methods to this class

    public MessageHandler(int port) {
        this.port = port;
        clients = new ArrayList<>();
        entities = new ArrayList<>();
        servers = new HashMap<>();
    }

    public void start() {
        // the following is just to get you started
        // you may need to change it to fit your implementation
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket incomingSocket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(incomingSocket.getInputStream()));
                String introMessage = reader.readLine();

                if (introMessage != null) {
                    // Parse the intro message
                    String[] parts = introMessage.split("\\|");

                    // String introMessage = "Client|" + this.clientId;
                    String type = parts[0]; //Client, Sensor, Actuator
                    int clientID = Integer.parseInt(parts[1]);

                    if (!servers.containsKey(clientID)) {
                        servers.put(clientID, new Server(clientID));
                    }
                    Server thisServer = servers.get(clientID);
                    Thread newObject = new Thread(new MessageHandlerThread(incomingSocket, type, clientID, thisServer, reader));
                    newObject.start();
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // you would need to initialize the RequestHandler with the port number
        // and then start it here

        
    }
}
