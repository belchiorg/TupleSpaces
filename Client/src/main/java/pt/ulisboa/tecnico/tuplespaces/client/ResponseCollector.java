package pt.ulisboa.tecnico.tuplespaces.client;

import java.util.ArrayList;
import java.util.List;

public class ResponseCollector<Response> {
    public List<Response> collectedResponses;
    public List<Throwable> collectedErrors;
    public int errors;

    public  ResponseCollector(){
        collectedResponses = new ArrayList<>();
        collectedErrors = new ArrayList<>();
        int errors=0;
    }
    public synchronized void notifyError(Throwable throwable){
        errors++;
        collectedErrors.add(throwable);
        notify();
    }
    public synchronized void addResponse(Response s){
        collectedResponses.add(s);
        notify();
    }

    public synchronized List<Response> getResponses(){
        return new ArrayList<>(this.collectedResponses);
    }

    public synchronized Response getFirstResponse() {

        return this.collectedResponses.get(0);
    }

    public synchronized void waitForAllResponses(int n) throws InterruptedException{
        while(collectedResponses.size() + errors < n)
            wait();
    }

    public synchronized void waitForFirstResponse() throws InterruptedException {
        while (collectedResponses.isEmpty())
            wait();
    }
}

