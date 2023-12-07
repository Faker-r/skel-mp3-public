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
                    String request = null;
                    if ((request = reader.readLine()) != null) {

                        String[] parts = request.split("\\|");
                        Request newRequest = new Request(RequestType.valueOf(parts[1]), RequestCommand.valueOf(parts[2])
                                , parts[4]);
                        server.processIncomingRequest(newRequest);

                        //respond to request
                    }
                }
            }

            else if (type.equals("Sensor")){
                while (true) {
                    //read from server for commands

                    String sensorEvent = reader.readLine();
                    String[] parts = sensorEvent.split("\\|");
                    SensorEvent newEvent = new SensorEvent(Double.parseDouble(parts[0]), Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]), parts[3], Double.parseDouble(parts[4]));
                    server.processIncomingEvent(newEvent);
                }
            }

            else if (type.equals("Actuator")){
                while (true) {
                    //Read from queue
                    String actuatorEvent = null;
                    actuatorEvent = reader.readLine();
                    String[] parts = actuatorEvent.split("\\|");
                    ActuatorEvent newEvent = new ActuatorEvent(Double.parseDouble(parts[0]), Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]), parts[3], Boolean.parseBoolean(parts[4]));
                    server.processIncomingEvent(newEvent);
                }
            }
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

    }
}