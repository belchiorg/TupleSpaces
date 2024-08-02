package pt.ulisboa.tecnico.tuplespaces.server.domain;

public class Tuple {
    private String tuple = "";

    private boolean isLocked;
    private int clientId;

    public Tuple(String tuple) {
        this.tuple = tuple;
        this.isLocked = false;
        this.clientId = -1;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void lockTuple(int clientId) {
        this.isLocked = true;
        this.clientId = clientId;
    }

    public void unlockTuple() {
        this.isLocked = false;
        this.clientId = -1;
    }

    public int getClientId() {
        return clientId;
    }

    public String getTuple() {
        return tuple;
    }

}
