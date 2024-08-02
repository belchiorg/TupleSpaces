package pt.ulisboa.tecnico.tuplespaces.server.domain;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized;
import pt.ulisboa.tecnico.tuplespaces.server.ServerMain;
import pt.ulisboa.tecnico.tuplespaces.server.util.TakeTicket;

import java.util.ArrayList;

public class ServerState {

  private ArrayList<Tuple> tuples;

  private ArrayList<TakeTicket> takeTickets = new ArrayList<>();
  private int instructionCounter;

  public ServerState() {
    this.tuples = new ArrayList<Tuple>();
    this.instructionCounter = 1;
  }

  public synchronized void incrementCounter() {
    this.instructionCounter++;
    notifyAll();
  }

  public boolean takeWaitingForPut(String tuple){
    for (TakeTicket takeTicket : takeTickets){
      if(tuple.equals(takeTicket.getTuple())){
        takeTicket.setResult(tuple);
        return true;
      }
    }
    return false;
  }
  public synchronized void put(String tuple, int seqNumber) {
    if (ServerMain.is_debug_enabled()) {
      System.out.println("counter: " + instructionCounter + " seqnumber: " + seqNumber);
    }
    while (this.instructionCounter != seqNumber) {
      try {
        wait();
      } catch (InterruptedException e) {
        // Interrupted
        System.out.println("Put operation interrupted: " + e.getMessage());
      }
    }
    // Se não há nenhum take à espera deste put adiciono-o ao TupleSpace
    if(!takeWaitingForPut(tuple)){
      tuples.add(new Tuple(tuple));
    }
    notifyAll();
    incrementCounter();
  }

  private Tuple getMatchingTuple(String pattern) {
    for (Tuple tuple : this.tuples) {
      if (tuple.getTuple().matches(pattern)) {
        return tuple;
      }
    }
    return null;
  }

  public synchronized String read(String pattern) throws RuntimeException{
    Tuple match = getMatchingTuple(pattern);
    while(match == null) {
      try {
        wait();
      } catch (InterruptedException e) {
        // Interrupted
        System.out.println("Read operation interrupted" + e.getMessage());
      }
      match = getMatchingTuple(pattern);
    }
    return match.getTuple();
  }

  public void addSortedTicket(TakeTicket ticket) {
    int i = 0;
    while (i < takeTickets.size() && takeTickets.get(i).getSeqNumber() < ticket.getSeqNumber()) {
      i++;
    }
    takeTickets.add(i, ticket);
  }

  public synchronized String take(String pattern, int seqNumber) throws RuntimeException {
    Tuple matchingTuple = null;
    String result = null;

    if (ServerMain.is_debug_enabled()) {
      System.out.println("counter: " + instructionCounter + " seqnumber: " + seqNumber);
    }

    while (this.instructionCounter != seqNumber) {
      try {
        wait();
      } catch (InterruptedException e) {
        // Interrupted
        System.out.println("Take operation interrupted: " + e.getMessage());
      }
    }

    matchingTuple = getMatchingTuple(pattern);
    if (matchingTuple == null) {
        TakeTicket ticket = new TakeTicket(seqNumber, pattern);
        this.takeTickets.add(ticket);
        incrementCounter();
        this.notifyAll();
        while (!ticket.isSolved()) {
            try {
                wait();
            } catch (InterruptedException e) {
                // Interrupted
                System.out.println("Take operation interrupted: " + e.getMessage());
            }
        }
        takeTickets.remove(ticket);
        return ticket.getResult();
    } else {
        this.tuples.remove(matchingTuple);
        incrementCounter();
    }

    return matchingTuple.getTuple();
  }

  public ArrayList<String> getTupleSpacesState() {
    return new ArrayList<>(this.tuples.stream().map(Tuple::getTuple).toList());
  }
}
