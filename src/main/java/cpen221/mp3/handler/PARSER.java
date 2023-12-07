package cpen221.mp3.handler;

import cpen221.mp3.client.*;
import cpen221.mp3.event.*;

public class PARSER {
    public static Request clientRequest(String input){
        String[] parsedInput = input.split("[=,]");
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

    public static Event actuatorEvent(String input){
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

    public static Event SensorEvent(String input){
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

}
