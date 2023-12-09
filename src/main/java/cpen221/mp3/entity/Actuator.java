package cpen221.mp3.entity;

import cpen221.mp3.client.Client;
import cpen221.mp3.client.Request;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.handler.PARSER;
import cpen221.mp3.server.Server;
import cpen221.mp3.server.SeverCommandToActuator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Actuator implements Entity {

    /**
     * Rep Invariant
     *
     * id       :  integer more than 0, represent the ID of the actuator
     * clientID :integer more than 0, represent the ID of the client
     * type     : non-empty String, represent the object of the actuator
     * state    : non-empty boolean, represent the current state
     * EventGenerationFrequency    : double more than 0, represent the frequency that the message is sent
     * serverPort : integer more than 0, represent the port of the connected server
     * serverIP : non-empty String, represent the IP od the connected server
     * host : integer more than 0, represent the host of the actuator, as the actuator also works as Server
     * port : integer more than 0, represent the port of the actuator, as the actuator also works as Server
     * actuatorSocket : non-empty socket type, represent the socket of the actuator object
     * actuatorWriter : non-empty printWriter, represent the writer that can send data to the socket
     *
     */

    private final int id;
    private int clientId;
    private final String type;
    private boolean state;
    private double eventGenerationFrequency = 0.2; // default value in Hz (1/s)
    // the following specifies the http endpoint that the actuator should send events to
    private String serverIP = null;
    private int serverPort = 0;
    // the following specifies the http endpoint that the actuator should be able to receive commands on from server
    private String host = null;
    private int port = 0;
    private Socket actuatorSocket;
    private BufferedReader actuatorRead;
    private PrintWriter actuatorWrite;
    private int numFail = 0;

    public Actuator(int id, String type, boolean init_state) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
        this.state = init_state;

    }

    public Actuator(int id, int clientId, String type, boolean init_state) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
    }

    public Actuator(int id, String type, boolean init_state, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public Actuator(int id, int clientId, String type, boolean init_state, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        this.serverPort = serverPort;



        start();

    }

    public void start() {
        try{
            actuatorSocket = new Socket(serverIP,serverPort);
            actuatorWrite = new PrintWriter(new OutputStreamWriter(actuatorSocket.getOutputStream()));
            actuatorRead = new BufferedReader(new InputStreamReader(actuatorSocket.getInputStream()));
            String introMessage = "Actuator|" + clientId + "|" + id;
            actuatorWrite.println(introMessage);
            actuatorWrite.flush();

            //START TWO THREADS TO READ AND WRITE

        } catch(IOException e){
            System.out.println("Failing to initialize");
        }

        startThreads();
    }

    private void startThreads() {
        Thread writeThread = new Thread(() -> {
            while (true) {
                ActuatorEvent newEvent = new ActuatorEvent(System.currentTimeMillis(), clientId, id, "Actuator", state);
                sendEvent(newEvent);
                try {
                    Thread.sleep((long) (1000/eventGenerationFrequency));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        writeThread.start();

        Thread readThread = new Thread(() -> {
            while (true) {
                try {
                    String message = actuatorRead.readLine();
                    Request request = PARSER.clientRequest(message);
                    processServerMessage(request);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        readThread.start();
    }

    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public String getType() {
        return type;
    }

    public boolean isActuator() {
        return true;
    }

    public boolean getState() {
        return state;
    }

    public String getIP() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void updateState(boolean new_state) {
        this.state = new_state;
    }

    /**
     * Registers the actuator for the given client
     *
     * @return true if the actuator is new (clientID is -1 already) and gets successfully registered or if it is already registered for clientId, else false
     */
    public boolean registerForClient(int clientId) {
        boolean ans;
        if(this.clientId == -1 || this.clientId == clientId){
            this.clientId = clientId;

            ans = true;
        }
        else ans = false;

        if (clientId != -1 && serverIP != null) {
            start();
        }
        return ans;
    }

    /**
     * Sets or updates the http endpoint that
     * the actuator should send events to
     *
     * @param serverIP the IP address of the endpoint
     * @param serverPort the port number of the endpoint
     */
    public void setEndpoint(String serverIP, int serverPort){
        this.serverIP = serverIP;
        this.serverPort = serverPort;

        if (clientId != -1 && serverIP != null) {
            start();
        }
    }

    /**
     * Sets the frequency of event generation
     *
     * @param frequency the frequency of event generation in Hz (1/s)
     * @throws IllegalArgumentException if the frequency less than equals to 0
     */
    public void setEventGenerationFrequency(double frequency){
        if(frequency <= 0 ){
            throw new IllegalArgumentException("Invalid Input");
        }
        else this.eventGenerationFrequency = frequency;
    }

    public void sendEvent(Event event) {
        if(this.serverIP == null){
            System.out.println("Message cannot be initalized, CheckServerIP");
            numFail++;

        }
        else {
            String stringEvent = event.toString();
            actuatorWrite.println(stringEvent);
            actuatorWrite.flush();

            if(actuatorWrite.checkError()){
                System.out.println("Message cannot be sent, id : " + this.id);
                numFail++;
            }
        }
        if(numFail == 10){
            try{
                Thread.sleep(10000);
            } catch (InterruptedException e){

            }
        }
    }

    public void processServerMessage(Request command) {
        switch (command.getRequestCommand()){
            case CONTROL_SET_ACTUATOR_STATE:
                state = Boolean.TRUE;
                break;
            case CONTROL_TOGGLE_ACTUATOR_STATE:
                if(state){
                    state = Boolean.FALSE;
                } else {
                    state = Boolean.TRUE;
                }
                break;
        }
    }

    @Override
    public String toString() {
        return "Actuator{" +
                "getId=" + getId() +
                ",ClientId=" + getClientId() +
                ",EntityType=" + getType() +
                ",IP=" + getIP() +
                ",Port=" + getPort() +
                '}';
    }

    // you will most likely need additional helper methods for this class\

}