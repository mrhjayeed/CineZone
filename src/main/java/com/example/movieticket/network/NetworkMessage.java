package com.example.movieticket.network;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Message structure for network communication between client and server
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkMessage {
    private String type;
    private String eventType;
    private Object data;
    private Integer screeningId;
    private long timestamp;

    public NetworkMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public NetworkMessage(String type, String eventType, Object data) {
        this();
        this.type = type;
        this.eventType = eventType;
        this.data = data;
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Integer getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(Integer screeningId) {
        this.screeningId = screeningId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
