package pt.ulisboa.tecnico.tuplespaces.client.util;

import java.util.Random;

public class TokenHandler {
    private final int clientId;
    private int messageId;

    public TokenHandler() {
        this.clientId = new Random().nextInt(1000);
        this.messageId = 0;
    }

    public TokenHandler(int clientId) {
        this.clientId = clientId;
        this.messageId = 0;
    }

    public int getClientId() {
        return clientId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void incrementMessageId() {
        messageId++;
    }

    public boolean isCurrentMessage(int clientId,  int messageId) {
        return this.clientId == clientId && this.messageId == messageId;
    }
}
