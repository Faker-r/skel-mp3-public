package cpen221.mp3.client;

import cpen221.mp3.entity.Entity;
import cpen221.mp3.server.Server;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client {

    private final int clientId;
    private String email;
    private String serverIP;
    private int serverPort;
    private Socket clientSocket;
    private PrintWriter clientPrint;
    private BufferedReader clientReader;

    private static Map<Client, Entity> listOfEntity = new HashMap<>();

    // you would need additional fields to enable functionalities required for this class

    public Client(int clientId, String email, String serverIP, int serverPort) {
        this.clientId = clientId;
        this.email = email;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try{
            clientSocket = new Socket(serverIP,serverPort);
            clientPrint = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e){
            System.out.println("Client Cannot be initialized");
        }

    }

    public int getClientId() {
        return clientId;
    }

    /**
     * Registers an entity for the client
     *
     * @return true if the entity is new and gets successfully registered, false if the Entity is already registered
     */
    public boolean addEntity(Entity entity) {
        if(listOfEntity.containsValue(entity)){
            return false;
        }
        else {
            listOfEntity.put(this, entity);
            return true;
        }
    }

    // sends a request to the server
    public void sendRequest(Request request) {
        if(serverIP == null){
            System.out.println("message cannot be initialized, check serverIP");
        }
        else{
            String stringRequest = clientId + "|" + request.toString();
            clientPrint.println(stringRequest);
            clientPrint.flush();
        }
        // implement this method
        // note that Request is a complex object that you need to serialize before sending
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Client){
            Client k = (Client) obj;
            return k.getClientId() == this.getClientId();
        }
        return super.equals(obj);
    }
}