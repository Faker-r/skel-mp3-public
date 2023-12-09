package cpen221.mp3.server;

import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.handler.PARSER;
import java.util.*;


import cpen221.mp3.server.DoubleOperator;
import cpen221.mp3.server.Filter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserTest {

    @Test
    public void parserTest1(){
        Request test1 = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "5.2");
        String test1String = test1.toString();
        Request test1Parser = PARSER.clientRequest(test1String);
        assertEquals(test1, test1Parser);
    }

    @Test
    public void parserTest2(){
        boolean check = false;
        try{
            Request test1 = new Request(RequestType.CONTROL, RequestCommand.PREDICT_NEXT_N_VALUES, "5.2");
            String test1String = test1.toString();
            Request test1Parser = PARSER.clientRequest(test1String);
        } catch (Exception e){
            check = true;
        }
        assertTrue(check);
    }

    @Test
    public void parserTest3(){
        boolean check = false;
        try{
            Request test1 = new Request(RequestType.CONFIG, RequestCommand.PREDICT_NEXT_N_VALUES, "5.2");
        } catch (Exception e){
            check = true;
        }
        try{
            Request test3 = new Request(RequestType.PREDICT, RequestCommand.PREDICT_NEXT_N_VALUES, "5.2");
        } catch (Exception e){
            check = true;
        }
        try{
            Request test1 = new Request(RequestType.ANALYSIS, RequestCommand.PREDICT_NEXT_N_VALUES, "5.2");
        } catch (Exception e){
            check = true;
        }
        assertTrue(check);
    }

    @Test
    public void test6(){
        Event testEvent1 = new SensorEvent(0.1, 10, 10, "CO2", 2.3);
        Event testEvent2 = new SensorEvent(0.2, 19, 11, "water", 67 );
        String testEventString1 = testEvent1.toString();
        String testEventString2 = testEvent2.toString();
        Event event1 = PARSER.SensorEvent(testEventString1);
        Event event2 = PARSER.SensorEvent(testEventString2);
        assertEquals(event1.getClientId(),testEvent1.getClientId());
        assertEquals(event2.getEntityId(), testEvent2.getEntityId());
    }

    public void test7(){
        Event testEvent1 = new ActuatorEvent(0.1, 10, 10, "CO2", true);
        Event testEvent2 = new ActuatorEvent(0.2, 19, 11, "water", false);
        String testEventString1 = testEvent1.toString();
        String testEventString2 = testEvent2.toString();
        Event event1 = PARSER.actuatorEvent(testEventString1);
        Event event2 = PARSER.actuatorEvent(testEventString2);
        assertEquals(event1.getClientId(),testEvent1.getClientId());
        assertEquals(event2.getEntityId(), testEvent2.getEntityId());
    }

    public void test8(){
        Event testEvent1 = new ActuatorEvent(0.1, 10, 10, "CO2", true);
        Event testEvent2 = new ActuatorEvent(0.2, 19, 11, "water", false);
        List<Event> testList = new ArrayList<>();
        testList.add(testEvent1);
        testList.add(testEvent2);
        String stringList = PARSER.ParsingListOfEvent(testList);
        List<Event> testListOutcome = PARSER.ListOfEvent(stringList);
        assertEquals(2, testListOutcome.size());
        Filter sensorValueFilter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 23);
    }






}
