package cpen221.mp3.entity;

import com.sun.java.accessibility.util.AccessibilityListenerList;
import cpen221.mp3.event.Event;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class Sensor implements Entity {

    /**
     * Rep Invariant
     *
     * id       :  integer more than 0, represent the ID of the sensor
     * clientID : integer more than 0, represent the ID of the client
     * type     : non-empty String, represent the object of the sensor
     * state    : double morethan 0, represent the current state
     * EventGenerationFrequency    : double more than 0, represent the frequency that the message is sent
     * serverPort : integer more than 0, represent the port of the connected server
     * serverIP : non-empty String, represent the IP of the connected server
     * sensorSocket : non-empty socket type, represent the socket of the sensor object
     * sensorWriter : non-empty printWriter, represent the writer that can send data to the socket
     *
     */

    private final int id;
    private int clientId;
    private final String type;
    private String serverIP = null;
    private int serverPort = 0;
    private double eventGenerationFrequency = 0.2; // default value in Hz (1/s)
    private Socket sensorSocket;
    private PrintWriter sensorWriter;
    private int numFail = 0;

    public Sensor(int id, String type) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
    }

    public Sensor(int id, int clientId, String type) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        try{
            sensorSocket = new Socket(serverIP,serverPort);
            sensorWriter = new PrintWriter(new OutputStreamWriter(sensorSocket.getOutputStream()));
            System.out.println("Sucessfully Build Sensor : (id = " + id + ")");
        } catch (IOException e){
            System.out.println("Fail Build Sensor : (id = " + id + ")");
        }
    }

    public Sensor(int id, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = -1;   // remains unregistered
        this.type = type;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try{
            sensorSocket = new Socket(serverIP,serverPort);
            sensorWriter = new PrintWriter(new OutputStreamWriter(sensorSocket.getOutputStream()));
            System.out.println("Sucessfully Build and connect server : (id = " + id + ")");
        } catch (IOException e){
            System.out.println("Fail Build Sensor : (id = " + id + ")");
        }

    }

    public Sensor(int id, int clientId, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try{
            sensorSocket = new Socket(serverIP,serverPort);
            sensorWriter = new PrintWriter(new OutputStreamWriter(sensorSocket.getOutputStream()));
            System.out.println("Sucessfully Build Sensor and connect : (id = " + id + ")");
        } catch (IOException e){
            System.out.println("Fail Build Sensor : (id = " + id + ")");
        }
    }

    /**
     * returning the ID of the Sensor
     * id more than 0
     * @return int, the ID of the Sensor
     */
    public int getId() {
        return id;
    }

    /**
     * Retuning the ID of the client
     * clientID more than 0
     * @return int, the ID of the client
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Returning the type of the Senso
     * type is non-empty string
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * Retuning if the object is an actuator
     * @return boolean false
     */
    public boolean isActuator() {
        return false;
    }

    /**
     * Registers the sensor for the given client
     *
     * @return true if the sensor is new (clientID is -1 already) and gets successfully registered or if it is already registered for clientId, else false
     */
    public boolean registerForClient(int clientId) {
        if(this.clientId == -1 || this.clientId == clientId){
            this.clientId = clientId;
            return true;
        }
        else return false;
    }

    /**
     * Sets or updates the http endpoint that
     * the sensor should send events to
     *
     * @param serverIP the IP address of the endpoint
     * @param serverPort the port number of the endpoint
     */
    public void setEndpoint(String serverIP, int serverPort){
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try{
            sensorSocket = new Socket(serverIP,serverPort);
            sensorWriter = new PrintWriter(new OutputStreamWriter(sensorSocket.getOutputStream()));
            System.out.println("Sucessfully connect server : (id = " + id + ")");
        } catch (IOException e){
            System.out.println("Fail Build Sensor : (id = " + id + ")");
        }
    }

    /**
     * Sets the frequency of event generation
     *
     * @param frequency the frequency of event generation in Hz (1/s)
     * @throws IllegalArgumentException if the frequency less tan equals to 0
     */
    public void setEventGenerationFrequency(double frequency){
        if(this.eventGenerationFrequency <= 0){
            throw new IllegalArgumentException("Invalid Input");
        }
        else this.eventGenerationFrequency = frequency;
    }

    /**
     * send the current event of the entity object to the connected server
     * serverIP is not null
     * Server port more than 0
     * @param event non_null Event Object, represent the event object that is going to be sen tot the server
     */

    public void sendEvent(Event event) {
        if(serverIP == null){
            System.out.println("The sensor has not been initialized");
            numFail++;
        }
        else{
            String sendEvent = event.toString();
            sensorWriter.println(sendEvent);
            sensorWriter.flush();

            if(sensorWriter.checkError()){
                System.out.println("Error while sending, id :" + this.id);
                numFail++;
            }
            else {
                System.out.println("Sent!");
            }

            try {
                Thread.sleep((long) (1000.0/eventGenerationFrequency));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        if(numFail == 10){
            try {
                Thread.sleep(10000);
                numFail = 0;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // implement this method

        // note that Event is a complex object that you need to serialize before sending
    }

}
