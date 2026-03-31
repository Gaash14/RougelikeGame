package com.example.rougelikegame.android.models.meta;

/**
 * GuildMessage stores details about a message, including the sender and timestamp.
 */
public class GuildMessage {
    private String senderId;
    private String senderName;
    private String text;
    private long timestamp;

    /**
     * Default constructor for Firebase/JSON serialization.
     */
    public GuildMessage() {
    }

    /**
     * Constructs a new GuildMessage.
     * @param senderId The unique identifier of the sender.
     * @param senderName The name of the sender.
     * @param text The message content.
     * @param timestamp The time the message was sent.
     */
    public GuildMessage(String senderId, String senderName, String text, long timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
