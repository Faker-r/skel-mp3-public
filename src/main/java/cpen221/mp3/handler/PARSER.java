package cpen221.mp3.handler;

import cpen221.mp3.client.*;
import cpen221.mp3.event.*;

import java.util.ArrayList;
import java.util.List;

public class PARSER {
    public static Request clientRequest(String input){
        String[] parsedInput = input.split("[|=,]");
        double timeStamp = Long.parseLong(parsedInput[1]);
        RequestType requestType = RequestType.valueOf(parsedInput[3]);
        RequestCommand requestCommand = RequestCommand.valueOf(parsedInput[5]);
        String requestData = parsedInput[7];
        return new Request(requestType,requestCommand,requestData);
    }

//    public String toString() {
//        return "Request{" +
//                "timeStamp=" + this.getTimeStamp() +
//                ",RequestType=" + this.getRequestType() +
//                ",RequestCommand=" + this.getRequestCommand() +
//                ",Requestdata=" + this.getRequestData();
//    }

    public static ActuatorEvent actuatorEvent(String input){
        String[] parsedInput = input.split("[=,}]");
        double timeStamp = Long.parseLong(parsedInput[1]);
        int clientID = Integer.parseInt(parsedInput[3]);
        int entityID = Integer.parseInt(parsedInput[5]);
        String entityType = parsedInput[7];
        boolean valueBoolean = Boolean.parseBoolean(parsedInput[9]);
        return new ActuatorEvent(timeStamp,clientID,entityID,entityType,valueBoolean);
    }


    //    public String toString() {
//        return "ActuatorEvent{" +
//                "TimeStamp=" + getTimeStamp() +
//                ",ClientId=" + getClientId() +
//                ",EntityId=" + getEntityId() +
//                ",EntityType=" + getEntityType() +
//                ",Value=" + getValueBoolean() +
//                '}';
//    }

    public static SensorEvent SensorEvent(String input){
        String[] parsedInput = input.split("[=,}]");
        double timeStamp = Long.parseLong(parsedInput[1]);
        int clientID = Integer.parseInt(parsedInput[3]);
        int entityID = Integer.parseInt(parsedInput[5]);
        String entityType = parsedInput[7];
        double valueDouble = Double.parseDouble(parsedInput[9]);
        return new SensorEvent(timeStamp,clientID,entityID,entityType,valueDouble);
    }

    //    public String toString() {
//        return "SensorEvent{" +
//                "TimeStamp=" + getTimeStamp() +
//                ",ClientId=" + getClientId() +
//                ",EntityId=" + getEntityId() +
//                ",EntityType=" + getEntityType() +
//                ",Value=" + getValueDouble() +
//                '}';
//    }

    /**
     *List of Event, translating string into List of Event
     * Requires non-empty string with format
     * " ................. event" + "|" + "....Event........." + "|" + "......Event........"
     * @param input non-empty with specifies as above
     * @return List of related
     */
    public static List<Event> ListOfEvent(String input){
        String[] splitInput = input.split("\\|");
        List<Event> output = new ArrayList<>();
        for(String k : splitInput){
            Event pashedEvent = PARSER.actuatorEvent(k);
            output.add(pashedEvent);
        }
        return output;
    }

    /**
     * Translating ListOFEvent into String, with specified format
     * " ................. event" + "|" + "....Event........." + "|" + "......Event........"
     * @param input non-empty List OF Event
     * @return String, which is the translation of the Input
     */
    public static String ParsingListOfEvent(List<Event> input){
        StringBuilder output = new StringBuilder();
        for(Event x : input){
            output.append(x.toString());
            output.append("|");
        }
        return output.toString();
    }





}
