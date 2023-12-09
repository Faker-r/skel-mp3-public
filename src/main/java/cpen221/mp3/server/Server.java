package cpen221.mp3.server;

import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.client.Client;
import cpen221.mp3.entity.Entity;
import cpen221.mp3.entity.Sensor;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.client.Request;

import java.awt.event.HierarchyEvent;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Server {
    private Client client;
    private int clientId;
    private double maxWaitTime = 2; // in seconds

    private Socket socketForServer;
    private PrintWriter serverWriter;
    private BufferedReader serverReader;
    private List<Event> historyOfEvent = new ArrayList<>();
    private Map<Integer, Integer> mostActiveEntity;
    private Queue<Event> currentEvent = new PriorityQueue<>();
    private ArrayList<Socket> sensorSockets;
    private ArrayList<Socket> actuatorSockets;
    private ArrayList<Integer> log;
    private double logTime;

    private BlockingQueue<String> clientNotifications = new LinkedBlockingQueue<>();
    private  Map<Integer, Entity> entityIDs;


    private  Map<Integer, Actuator> ActuatorIDs;

    private  Map<Integer, Sensor> SensorIDs;

    private Socket clientSocket;
    private ArrayList<BlockingQueue> actuatorQueues = new ArrayList<>();

    private Filter logIf;
    private Filter toggleIf;
    private Filter setIf;
    private Integer toggleIfActuator;
    private Integer setIfActuator;

    public ConcurrentHashMap<Integer, BlockingQueue<String>> entityQueues  = new ConcurrentHashMap<>(); //Entity ID, list of commands
    public ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1); // Shared executor

    // you may need to add additional private fields

    public Server(Client client){
        this.clientId = client.getClientId();
        // implement the Server constructor
        this.client = client;
        //Make new thread to consume queue objects
        runEventQueueThread();
    }
    public Server(int clientId){
        this.clientId = clientId;
        this.logTime = Double.MAX_VALUE;
        this.setIfActuator = 0;
        this.toggleIfActuator = 0;
        runEventQueueThread();
    }
    public void runEventQueueThread() {

    }
    public void addClient(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    public void addSensors(Socket sensorSocket) {
        sensorSockets.add(sensorSocket);
    }
    public void addActuator(int actuatorID) {
        entityQueues.put(actuatorID, new LinkedBlockingQueue<Request>());
    }

    /**
     * Update the max wait time for the client.
     * The max wait time is the maximum amount of time
     * that the server can wait for before starting to process each event of the client:
     * It is the difference between the time the message was received on the server
     * (not the event timeStamp from above) and the time it started to be processed.
     *
     * @param maxWaitTime the new max wait time
     */
    public void updateMaxWaitTime(double maxWaitTime) {
        // implement this method
        this.maxWaitTime = maxWaitTime;

        // Important note: updating maxWaitTime may not be as simple as
        // just updating the field. You may need to do some additional
        // work to ensure that events currently being processed are not
        // dropped or ignored by the change in maxWaitTime.
    }

    /**
     * Set the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event
     * that was received by the server the latest.
     *
     * If the actuator is not registered for the client, then this method should do nothing.
     *
     * @param filter the filter to check
     * @param actuator the actuator to set the state of as true
     */
    public void setActuatorStateIf(Filter filter, Actuator actuator) {

        // implement this method and send the appropriate SeverCommandToActuator as a Request to the actuator
        boolean x = historyOfEvent.stream()
                .filter(m -> m.getEntityId() == actuator.getId())
                .max(Comparator.comparingDouble(Event::getTimeStamp))
                .map(Event::getValueBoolean)
                .orElse(Boolean.FALSE);
        if (x){
            Request Command = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE
                    , "ON");
            actuator.updateState(true);
            //SEND COMMAND TO ACTUATOR
        }
    }

    public void setActuatorStateIf(Filter filter, int ActuatorID) throws InterruptedException {

        // implement this method and send the appropriate SeverCommandToActuator as a Request to the actuator
        setIf = filter;
        setIfActuator = ActuatorID;

    }

    /**
     * Toggle the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event
     * that was received by the server the latest.
     *
     * If the actuator has never sent an event to the server, then this method should do nothing.
     * If the actuator is not registered for the client, then this method should do nothing.
     *
     * @param filter the filter to check
     * @param actuator the actuator to toggle the state of (true -> false, false -> true)
     */
    public void toggleActuatorStateIf(Filter filter, Actuator actuator) {
        // implement this method and send the appropriate SeverCommandToActuator as a Request to the actuator
        boolean x = historyOfEvent.stream()
                .filter(m -> m.getEntityId() == actuator.getId())
                .max(Comparator.comparingDouble(Event::getTimeStamp))
                .map(Event::getValueBoolean)
                .orElse(Boolean.FALSE);
        if(x){
            actuator.updateState(!actuator.getState());
        }

    }

    public void toggleActuatorStateIf(Filter filter, int actuatorID) throws InterruptedException {

        toggleIfActuator = actuatorID;
        toggleIf = filter;
    }
    public void toggleActuator(Filter filter, int EntityID) {

    }

    /**
     * Log the event ID for which a given filter was satisfied.
     * This method is checked for every event received by the server.
     *
     * @param filter the filter to check
     */
    public void logIf(Filter filter) {
        logIf = filter;
        for(int i = historyOfEvent.size() - 1; i >= 0; i--){
            if (historyOfEvent.get(i).getTimeStamp() > logTime ){
                if(filter.satisfies((historyOfEvent.get(i)))) {
                    log.add(historyOfEvent.get(i).getEntityId());
                }
            } else {
                return;
            }
        }
    }

    /**
     * Return all the logs made by the "logIf" method so far.
     * If no logs have been made, then this method should return an empty list.
     * The list should be sorted in the order of event timestamps.
     * After the logs are read, they should be cleared from the server.
     *
     * @return list of entity IDs
     */
    public List<Integer> readLogs() {
        ArrayList<Integer> clone = (ArrayList<Integer>) log.clone();
        log.clear();
        return clone;
    }

    /**
     * List all the events of the client that occurred in the given time window.
     * Here the timestamp of an event is the time at which the event occurred, not
     * the time at which the event was received by the server.
     * If no events occurred in the given time window, then this method should return an empty list.
     *
     * @param timeWindow the time window of events, inclusive of the start and end times
     * @return list of the events for the client in the given time window
     */
    public List<Event> eventsInTimeWindow(TimeWindow timeWindow) {
        List<Event> eventsInWindow = historyOfEvent.stream()
                .filter(event -> event.getTimeStamp() >= timeWindow.startTime && event.getTimeStamp() <= timeWindow.endTime)
                .toList();
        return eventsInWindow;
    }

    /**
     * Returns a set of IDs for all the entities of the client for which
     * we have received events so far.
     * Returns an empty list if no events have been received for the client.
     *
     * @return list of all the entities of the client for which we have received events so far
     */
    public List<Integer> getAllEntities() {
        List<Integer> uniqueIds = historyOfEvent.stream()
                .map(Event::getEntityId)
                .distinct()
                .collect(Collectors.toList());
        return uniqueIds;
    }

    /**
     * List the latest n events of the client.
     * Here the order is based on the original timestamp of the events, not the time at which the events were received by the server.
     * If the client has fewer than n events, then this method should return all the events of the client.
     * If no events exist for the client, then this method should return an empty list.
     * If there are multiple events with the same timestamp in the boundary,
     * the ones with largest EntityId should be included in the list.
     *
     * @param n the max number of events to list
     * @return list of the latest n events of the client
     */
    public List<Event> lastNEvents(int n) {
        return historyOfEvent.subList(historyOfEvent.size(), historyOfEvent.size() - n);
    }

    /**
     * returns the ID corresponding to the most active entity of the client
     * in terms of the number of events it has generated.
     *
     * If there was a tie, then this method should return the largest ID.
     *
     * @return the most active entity ID of the client
     */
    public int mostActiveEntity() {
        Optional<Integer> maxKey = mostActiveEntity.entrySet()
                .stream()
                .max(Entry.comparingByValue())
                .map(Entry::getKey);
        if(maxKey.isPresent()){
            return maxKey.orElse(0);
        }
        return 0;
    }

    /**
     * the client can ask the server to predict what will be
     * the next n timestamps for the next n events
     * of the given entity of the client (the entity is identified by its ID).
     *
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     *
     * @param entityId the ID of the entity
     * @param n the number of timestamps to predict
     * @return list of the predicted timestamps
     */
    public List<Double> predictNextNTimeStamps(int entityId, int n) {
        // implement this method
        return null;
    }

    /**
     * the client can ask the server to predict what will be
     * the next n values of the timestamps for the next n events
     * of the given entity of the client (the entity is identified by its ID).
     * The values correspond to Event.getValueDouble() or Event.getValueBoolean()
     * based on the type of the entity. That is why the return type is List<Object>.
     *
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     *
     * @param entityId the ID of the entity
     * @param n the number of double value to predict
     * @return list of the predicted timestamps
     */
    public List<Object> predictNextNValues(int entityId, int n) {
        // implement this method
        return null;
    }

    synchronized public void processIncomingEvent(Event event) {
        for(int i = historyOfEvent.size(); i <= 0; i--){
             if (historyOfEvent.get(i).getTimeStamp() < event.getTimeStamp()){
                historyOfEvent.add(i, event);
                break;
            }
        }
        if(mostActiveEntity.containsKey(event.getEntityId())){
            mostActiveEntity.replace(event.getEntityId(), mostActiveEntity.get(event.getEntityId())
            , mostActiveEntity.get(event.getEntityId()) + 1);
        }
        else {
            mostActiveEntity.put(event.getEntityId(), 1);
        }
        if(event.getTimeStamp() > logTime){
            if(logIf.satisfies(event)){
                log.add(event.getEntityId());
            }
        }
        if(toggleIfActuator != 0){
            if(toggleIf.satisfies(event)){
                Request Command = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE
                        , "ON");
                entityQueues.get(event.getEntityId()).put(Command);
            }
        }
        if(setIfActuator != 0){
            if(setIf.satisfies(event)){
                Request Command = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE
                        , "TOGGLE");
                entityQueues.get(event.getEntityId()).put(Command);
            }
        }
    }

    public void processIncomingRequest(Request request) {
        String data = request.getRequestData();
        String[] parts = data.split("\\|");
        switch (request.getRequestCommand()) {
            case ANALYSIS_READ_LOG:
                ArrayList<Integer> currentLog = (ArrayList<Integer>) readLogs();
                break;
            case CONTROL_LOG_IF:
                Filter LOGIF;
                if(parts.length == 3){
                    LOGIF = new Filter(BooleanOperator.valueOf(parts[1]), Boolean.valueOf(parts[2]));
                }
                else {
                    LOGIF = new Filter(parts[1], DoubleOperator.valueOf(parts[2]), Double.valueOf(parts[3]));
                }
                logIf(LOGIF);
                logTime = Integer.valueOf(parts[0]);
                break;

            case PREDICT_NEXT_N_VALUES:
                predictNextNValues(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
                break;

            case ANALYSIS_GET_LATEST_EVENTS:
                lastNEvents(Integer.parseInt(parts[0]));

                break;

            case ANALYSIS_GET_ALL_ENTITIES:
                String Combined = "";
                for(Integer ID : getAllEntities()){
                    Combined += ID + ", ";
                }
                try {
                    clientNotifications.put(Combined);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;

            case PREDICT_NEXT_N_TIMESTAMPS:
                predictNextNTimeStamps(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
                break;

            case CONTROL_SET_ACTUATOR_STATE:

                Filter SETFILTER;
                if(parts.length == 3){
                    SETFILTER = new Filter(BooleanOperator.valueOf(parts[1]), Boolean.valueOf(parts[2]));
                }
                else {
                    SETFILTER = new Filter(parts[1], DoubleOperator.valueOf(parts[2]), Double.valueOf(parts[3]));
                }
                try {
                    toggleActuatorStateIf(SETFILTER, Integer.valueOf(parts[0]));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;

            case CONFIG_UPDATE_MAX_WAIT_TIME:
                updateMaxWaitTime(Double.valueOf(parts[0]));
                break;

            case ANALYSIS_GET_EVENTS_IN_WINDOW:
                TimeWindow window = new TimeWindow(Double.valueOf(parts[0]), Double.valueOf(parts[1]));
                eventsInTimeWindow(window);
                break;

            case CONTROL_TOGGLE_ACTUATOR_STATE:
                Filter TOGGLEFILTER;
                if(parts.length == 3){
                    TOGGLEFILTER = new Filter(BooleanOperator.valueOf(parts[1]), Boolean.valueOf(parts[2]));
                }
                else {
                    TOGGLEFILTER = new Filter(parts[1], DoubleOperator.valueOf(parts[2]), Double.valueOf(parts[3]));
                }
                try {
                    setActuatorStateIf(TOGGLEFILTER, Integer.valueOf(parts[0]));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;

            case ANALYSIS_GET_MOST_ACTIVE_ENTITY:
                mostActiveEntity();
                break;

        }



    }
    public double getMaxWaitTime() {
        return maxWaitTime;
    }

    public BlockingQueue<String> getClientNotifications() {
        return clientNotifications;
    }
    public ConcurrentHashMap<Long, Event> getNewEventsQueue() {
        return newEventsQueue;
    }

    public static void main(String[] args){
        List<Double> x = List.of(1.2,7.9);
        System.out.println(x.stream().max(Comparator.comparingDouble(p -> p)).orElse(100.9));
    }
}