package cpen221.mp3.event;

import cpen221.mp3.server.Server;

public class ActuatorEvent implements Event {
    // TODO: Implement this class
    private double TimeStamp;
    private int CLientId;
    private int EntityId;
    String EntityType;
    boolean Value;
    // you can add private fields and methods to this class

    public ActuatorEvent(double TimeStamp,
                         int ClientId,
                         int EntityId,
                         String EntityType,
                         boolean Value) {
        this.TimeStamp = TimeStamp;
        this.CLientId = ClientId;
        this.EntityId = EntityId;
        this.EntityType = EntityType;
        this.Value = Value;

    }

    /**
     * Returning the timestamp of an event
     * @return double Time stamp (Done)
     */
    public double getTimeStamp() {
        return this.TimeStamp;
    }

    /**
     * Retuning the Client ID
     * @return int Client ID (Done)
     */
    public int getClientId() {
        return this.CLientId;
    }

    /**
     * Returning the EntityID
     * @return int entityID (Done)
     */
    public int getEntityId() {
        return this.EntityId;
    }

    /**
     * Returning the Entity type of the event
     * @return String Entity Type
     */
    public String getEntityType() {
        return this.EntityType;
    }

    /**
     * Returning the Boolean value of the event
     * @return Boolean of Value
     */
    public boolean getValueBoolean() {
        return this.Value;
    }

    // Actuator events do not have a double value
    // no need to implement this method
    public double getValueDouble() {
        return -1;
    }

    @Override
    public String toString() {
        return "ActuatorEvent{" +
                "TimeStamp=" + getTimeStamp() +
                ",ClientId=" + getClientId() +
                ",EntityId=" + getEntityId() +
                ",EntityType=" + getEntityType() +
                ",Value=" + getValueBoolean() +
                '}';
    }
}
