package pt.ulisboa.tecnico.tuplespaces.server.util;

public class TakeTicket {
    private int seqNumber;
    private String tuple;

    private boolean isSolved = false;

    private String result = null;

    public TakeTicket(int seqNumber, String tuple) {
        this.seqNumber = seqNumber;
        this.tuple = tuple;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public String getTuple() {
        return tuple;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public synchronized void setSolved() {
        isSolved = true;
    }

    public String getResult(){
        return this.result;
    }

    public void setResult(String result){
        this.result = result;
        setSolved();
    }
}
