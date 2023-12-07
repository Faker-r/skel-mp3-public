package cpen221.mp3.handler;

import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

class MessageHandlerThread implements Runnable {
    private Socket incomingSocket;
    private Server server;
    private String type;
    private int ClientID;
    private BufferedReader reader;

    public MessageHandlerThread(Socket incomingSocket, String type, int ClientID, Server server, BufferedReader reader) {
        this.incomingSocket = incomingSocket;
        this.type = type;
        this.ClientID = ClientID;
        this.reader = reader;
    }

    //request = reader.readLine();
    @Override
    public void run() {
        try{
            if (type.equals("Client")){
                while (true) {
                    try{
                        String request = reader.readLine();
                        Request newRequest = PARSER.clientRequest(request);
                        server.processIncomingRequest(newRequest);
                    } catch (IOException e){
                        e.printStackTrace();
                        System.out.println("Unable to read request , id = " + String.valueOf(this.ClientID));
                    }
                }
            }

            else if (type.equals("Sensor")){
                while (true) {
                    try{
                        String sensorEvent = reader.readLine();
                        Event newEvent = PARSER.SensorEvent(sensorEvent);
                        server.processIncomingEvent(newEvent);
                    } catch (IOException e){
                        e.printStackTrace();
                        System.out.println("Unable to get sensor data, EntityID" + String.valueOf("")); //fixing if the entityID
                    }
                    //read from server for commands
                }
            }

            else if (type.equals("Actuator")){
                while (true) {
                    try {
                        String actuatorEvent = reader.readLine();
                        Event newEvent = PARSER.actuatorEvent(actuatorEvent);
                        server.processIncomingEvent(newEvent);
                    } catch (IOException e){
                        e.printStackTrace();
                        System.out.println("Unable to get sensor data, EntityID" + String.valueOf("")); //fixing if the entityID
                    }
                }
            }
        }

        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

    }
}