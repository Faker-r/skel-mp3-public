package cpen221.mp3.handler;

import cpen221.mp3.client.Request;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.server.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
                    if (type.equals("Client")) {
                        PrintWriter printer = new PrintWriter(new OutputStreamWriter(incomingSocket.getOutputStream()), true);
                        Thread clientReaderThread = new Thread(new ClientReadThread(reader, clientID, thisServer, null));
                        Thread clientWriterThread = new Thread(new ClientWriteThread(printer, clientID, thisServer, null));
                        clientWriterThread.start();
                        clientReaderThread.start();
                    }

                    if (type.equals("Actuator")) {
                        PrintWriter printer = new PrintWriter(new OutputStreamWriter(incomingSocket.getOutputStream()), true);
                        Thread actuatorReaderThread = new Thread(new ActuatorReadThread(reader, clientID, thisServer, parts[2]));
                        Thread actuatorWriterThread = new Thread(new ActuatorWriteThread(printer, clientID, thisServer, parts[2]));
                        actuatorWriterThread.start();
                        actuatorReaderThread.start();
                    }
                    if (type.equals("Sensor")) {
                        Thread sensorReaderThread = new Thread(new SensorReadThread(reader, clientID, thisServer, parts[2]));
                        sensorReaderThread.start();
                    }


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


abstract class ReaderThreadBase implements Runnable {
    protected BufferedReader reader;
    protected int clientID;
    protected Server server;
    protected String entityID;

    public ReaderThreadBase(BufferedReader reader, int clientID, Server server, String entityID) {
        this.reader = reader;
        this.clientID = clientID;
        this.server = server;
        this.entityID = entityID;
    }
}
abstract class WriterThreadBase implements Runnable {
    protected PrintWriter writer;
    protected int clientID;
    protected Server server;
    protected String entityID;

    public WriterThreadBase(PrintWriter writer, int clientID, Server server, String entityID) {
        this.writer = writer;
        this.clientID = clientID;
        this.server = server;
        this.entityID = entityID;
    }
}

class ClientReadThread extends ReaderThreadBase {
    public ClientReadThread(BufferedReader reader, int clientID, Server server, String entityID) {
        super(reader, clientID, server, entityID);
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                Request newRequest = PARSER.clientRequest(message);

                server.executorService.schedule(() -> server.processIncomingRequest(newRequest), (long)server.getMaxWaitTime(), TimeUnit.SECONDS);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientWriteThread extends WriterThreadBase {
    private BlockingQueue<String> messageQueue = server.getClientNotifications();

    public ClientWriteThread(PrintWriter printer, int clientID, Server server, String entityID) {
        super(printer, clientID, server, entityID);
    }
    @Override
    public void run() {
        try {
            while (true) {
                String message = messageQueue.take();
                writer.println(message);
                writer.flush();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class SensorReadThread extends ReaderThreadBase {
    public SensorReadThread(BufferedReader reader, int clientID, Server server, String entityID) {
        super(reader, clientID, server, entityID);
    }

    @Override
    public void run() {

        try {
            String message;
            while ((message = reader.readLine()) != null) {
                SensorEvent newEvent = PARSER.SensorEvent(message);
                server.processIncomingEvent(newEvent);

                toggleIFQueue.add(newEvent);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ActuatorReadThread extends ReaderThreadBase {
    public ActuatorReadThread(BufferedReader reader, int clientID, Server server, String entityID) {
        super(reader, clientID, server, entityID);
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                ActuatorEvent newEvent = PARSER.actuatorEvent(message);

                server.processIncomingEvent(newEvent);

                toggleIFQueue.add(newEvent);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ActuatorWriteThread extends WriterThreadBase {
    private BlockingQueue<String> messageQueue = server.entityQueues.get(entityID);

    public ActuatorWriteThread(PrintWriter writer, int clientID, Server server, String entityID) {
        super(writer, clientID, server, entityID);
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = messageQueue.take();
                writer.println(message);
                writer.flush();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}