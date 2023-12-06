package cpen221.mp3.server;

import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;

import java.util.ArrayList;
import java.util.List;

enum DoubleOperator {
    EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_OR_EQUALS,
    LESS_THAN_OR_EQUALS
}

enum BooleanOperator {
    EQUALS,
    NOT_EQUALS
}

public class Filter {
    // you can add private fields and methods to this class
    private BooleanOperator booleanOperator;
    private DoubleOperator doubleOperator;
    private boolean booleanValue = true;
    private String field;
    private double doubleValue = 0.0;
    private List<Filter> listOfFilters = new ArrayList<>();


    /**
     * Constructs a filter that compares the boolean (actuator) event value
     * to the given boolean value using the given BooleanOperator.
     * (X (BooleanOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A BooleanOperator can be one of the following:
     *
     * BooleanOperator.EQUALS
     * BooleanOperator.NOT_EQUALS
     *
     * @param operator the BooleanOperator to use to compare the event value with the given value
     * @param value the boolean value to match
     */
    public Filter(BooleanOperator operator, boolean value) {
        // TODO: implement this method
        this.booleanOperator = operator;
        this.booleanValue  = value;
        this.field = "value";
        this.listOfFilters.add(this);
    }

    /**
     * Constructs a filter that compares a double field in events
     * with the given double value using the given DoubleOperator.
     * (X (DoubleOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A DoubleOperator can be one of the following:
     *
     * DoubleOperator.EQUALS
     * DoubleOperator.GREATER_THAN
     * DoubleOperator.LESS_THAN
     * DoubleOperator.GREATER_THAN_OR_EQUALS
     * DoubleOperator.LESS_THAN_OR_EQUALS
     *
     * For non-double (boolean) value events, the satisfies method should return false.
     *
     * @param field the field to match (event "value" or event "timestamp")
     * @param operator the DoubleOperator to use to compare the event value with the given value
     * @param value the double value to match
     *
     * @throws IllegalArgumentException if the given field is not "value" or "timestamp"
     */
    public Filter(String field, DoubleOperator operator, double value) {
        // TODO: implement this method
        this.field = field;
        this.doubleOperator = operator;
        this.doubleValue = value;
        this.listOfFilters.add(this);
    }

    /**
     * A filter can be composed of other filters.
     * in this case, the filter should satisfy all the filters in the list.
     * Constructs a complex filter composed of other filters.
     *
     * @param filters the list of filters to use in the composition
     */
    public Filter(List<Filter> filters) {
        // TODO: implement this method
        this.listOfFilters = filters;
    }

    /**
     * Returns true if the given event satisfies the filter criteria.
     *
     * @param event the event to check
     * @return true if the event satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(Event event) {
        // TODO: implement this method
        List<Boolean> listOfAnswers = new ArrayList<>();
        for(Filter x : listOfFilters){
            if("value".equals(x.field)){
                if(event instanceof ActuatorEvent){
                    ActuatorEvent currentEvent = (ActuatorEvent) event;
                    if(x.booleanOperator == null){
                        listOfAnswers.add(false);
                    }
                    else if(x.booleanOperator.equals(BooleanOperator.EQUALS)){
                        listOfAnswers.add(currentEvent.getValueBoolean() == x.booleanValue);
                    }
                    else if(x.booleanOperator.equals(BooleanOperator.NOT_EQUALS)){
                        listOfAnswers.add(currentEvent.getValueBoolean() != x.booleanValue);
                    }
                    else{
                        throw new IllegalArgumentException("Wrong operator Implementation");
                    }
                }
                else if(event instanceof SensorEvent){
                    SensorEvent currentEvent = (SensorEvent) event;
                    if(x.doubleOperator == null){
                        listOfAnswers.add(false);
                    }
                    else if(x.doubleOperator.equals(DoubleOperator.GREATER_THAN)){
                        listOfAnswers.add(currentEvent.getValueDouble() > x.doubleValue);
                    }
                    else if(x.doubleOperator.equals(DoubleOperator.EQUALS)) {
                        listOfAnswers.add(currentEvent.getValueDouble() == x.doubleValue);
                    }
                    else if(x.doubleOperator.equals(DoubleOperator.LESS_THAN)){
                        listOfAnswers.add(currentEvent.getValueDouble() < x.doubleValue);
                    }
                    else if(x.doubleOperator.equals(DoubleOperator.LESS_THAN_OR_EQUALS)){
                        listOfAnswers.add(currentEvent.getValueDouble() <= x.doubleValue);
                    }
                    else if(x.doubleOperator.equals(DoubleOperator.GREATER_THAN_OR_EQUALS)){
                        listOfAnswers.add(currentEvent.getValueDouble() >= x.doubleValue);
                    }
                }
            }
            else if("timestamp".equals(x.field)){
                if(x.doubleOperator.equals(DoubleOperator.GREATER_THAN)){
                    listOfAnswers.add(event.getTimeStamp() > x.doubleValue);
                }
                else if(x.doubleOperator.equals(DoubleOperator.EQUALS)){
                    listOfAnswers.add(event.getTimeStamp() > x.doubleValue);
                }
                else if(x.doubleOperator.equals(DoubleOperator.LESS_THAN)){
                    listOfAnswers.add(event.getTimeStamp() < x.doubleValue);
                }
            }
            else{
                throw new IllegalArgumentException("invalid Event");
            }
        }

        for(Boolean y : listOfAnswers){
            if(y.equals(false)){
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the given list of events satisfies the filter criteria.
     *
     * @param events the list of events to check
     * @return true if every event in the list satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(List<Event> events) {
        // TODO: implement this method
        List<Boolean> listOfStaisfaction = new ArrayList<>();
        for(Event x : events){
            boolean satisfaction = this.satisfies(x);
            if(!satisfaction){
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a new event if it satisfies the filter criteria.
     * If the given event does not satisfy the filter criteria, then this method should return null.
     *
     * @param event the event to sift
     * @return a new event if it satisfies the filter criteria, null otherwise
     */
    public Event sift(Event event) {
        // TODO: implement this method
        if(this.satisfies(event)){
            return event;
        }
        return null;
    }

    /**
     * Returns a list of events that contains only the events in the given list that satisfy the filter criteria.
     * If no events in the given list satisfy the filter criteria, then this method should return an empty list.
     *
     * @param events the list of events to sift
     * @return a list of events that contains only the events in the given list that satisfy the filter criteria
     *        or an empty list if no events in the given list satisfy the filter criteria
     */
    public List<Event> sift(List<Event> events) {
        // TODO: implement this method
        List<Event> outcome = new ArrayList<>();
        for(Event x : events){
            if(this.satisfies(x)){
                outcome.add(x);
            }
        }
        return outcome;
    }

    @Override
    public String toString() {
        if(booleanOperator == null){
            String outcome =
                    "Filter : " +
                            "DoubleOperator : " + doubleValue +
                            "DoubleValue :" + doubleValue;
        }
        else{
            String outcome =
                    "Filter : " +
                            "BooleanOperator :" + booleanOperator.toString()+
                            "BooleanValue :" + booleanValue ;
        }
        return null;
    }
}
