package cpen221.mp3.client;

import cpen221.mp3.entity.Entity;

public class Client {

    private final int clientId;
    private String email;
    private String serverIP;
    private int serverPort;

    // you would need additional fields to enable functionalities required for this class

    public Client(int clientId, String email, String serverIP, int serverPort) {
        this.clientId = clientId;
        this.email = email;
        this.serverIP = serverIP;
        this.serverPort = serverPort; //hello
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
        // implement this method
        return false;
    }

    // sends a request to the server
    public void sendRequest(Request request) {
        // implement this method

        // note that Request is a complex object that you need to serialize before sending
    }

}