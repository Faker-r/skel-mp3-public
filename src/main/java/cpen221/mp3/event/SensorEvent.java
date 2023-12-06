package cpen221.mp3.event;

public class SensorEvent implements Event {
    // TODO: Implement this class
    // you can add private fields and methods to this class

    private double TimeStamp;
    private int CLientId;
    private int EntityId;
    String EntityType;
    double Value;

    public SensorEvent(double TimeStamp,
                       int ClientId,
                       int EntityId,
                       String EntityType,
                       double Value) {
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
     * @return String Entity Type (Done)
     */
    public String getEntityType() {
        return this.EntityType;
    }

    /**
     * Returning the value of this event
     * @return double of Value (Done)
     */
    public double getValueDouble() {
        return this.Value;
    }

    // Sensor events do not have a boolean value
    // no need to implement this method
    public boolean getValueBoolean() {
        return false;
    }

    @Override
    public String toString() {
        return "SensorEvent{" +
                "TimeStamp=" + getTimeStamp() +
                ",ClientId=" + getClientId() +
                ",EntityId=" + getEntityId() +
                ",EntityType=" + getEntityType() +
                ",Value=" + getValueDouble() +
                '}';
    }
}
