package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.SequencerClientService;

import java.util.List;
import java.util.Scanner;

public class CommandProcessor {

    private static final String SPACE = " ";
    private static final String BGN_TUPLE = "<";
    private static final String END_TUPLE = ">";
    private static final String PUT = "put";
    private static final String READ = "read";
    private static final String TAKE = "take";
    private static final String SLEEP = "sleep";
    private static final String SET_DELAY = "setdelay";
    private static final String EXIT = "exit";
    private static final String GET_TUPLE_SPACES_STATE = "getTupleSpacesState";

    private final ClientService clientService;

    private final SequencerClientService sequencerClientService;

    public CommandProcessor(ClientService clientService, SequencerClientService sequencerClientService) {
        this.clientService = clientService;
        this.sequencerClientService = sequencerClientService;
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String[] split = line.split(SPACE);
             switch (split[0]) {
                case PUT:
                    this.put(split);
                    break;

                case READ:
                    this.read(split);
                    break;

                case TAKE:
                    this.take(split);
                    break;

                case GET_TUPLE_SPACES_STATE:
                    this.getTupleSpacesState(split);
                    break;

                case SLEEP:
                    this.sleep(split);
                    break;

                case SET_DELAY:
                    this.setdelay(split);
                    break;

                case EXIT:
                    exit = true;
                    break;

                default:
                    this.printUsage();
                    break;
             }
        }

        // The channel should be shutdown before stopping the process.
        clientService.shutdownService();
    }

    private void put(String[] split){

        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }
        String tuple = split[1];

        try{
            // read the tuple
            int seqNumber = this.sequencerClientService.getSeqNumberService();
            this.clientService.putService(tuple, seqNumber);
            System.out.println("OK");
            System.out.println();
        } catch (StatusRuntimeException e) {
            System.out.println("Exception caught with message: " + e.getStatus().getDescription());
        }
    }

    private void read(String[] split){

        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }
        String result;
        // get the tuple
        String searchPattern = split[1];

        try{
            // read the tuple
            result = this.clientService.readService(searchPattern);
            System.out.println("OK");
            System.out.println(result);
            System.out.println();
        } catch (StatusRuntimeException e) {
            System.err.println("Exception caught with message: " + e.getStatus().getDescription());
        }
    }


    private void take(String[] split){
        String result;

         // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        // get the tuple
        String searchPattern = split[1];
        try{
            // take the tuple
            int seqNumber = this.sequencerClientService.getSeqNumberService();
            result = this.clientService.takeService(searchPattern, seqNumber);
            if (result == null) {
                return;
            }
            System.out.println("OK");
            System.out.println(result);
            System.out.println();
        } catch (StatusRuntimeException e){
            System.err.println("Exception caught with message: " + e);
        }
    }

    private void getTupleSpacesState(String[] split){
        if (split.length != 2){
            this.printUsage();
            return;
        }
        String qualifier = split[1];
        // get the tuple spaces state
        List<String> tuplesList =  this.clientService.getTupleSpacesStateService(qualifier);
        System.out.println("OK");
        System.out.println(tuplesList.toString());
        System.out.println();
    }

    private void sleep(String[] split) {
      if (split.length != 2){
        this.printUsage();
        return;
      }
      Integer time;

      // checks if input String can be parsed as an Integer
      try {
         time = Integer.parseInt(split[1]);
      } catch (NumberFormatException e) {
        this.printUsage();
        return;
      }

      try {
        Thread.sleep(time*1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    private void setdelay(String[] split) {
        if (split.length != 3){
          this.printUsage();
          return;
        }
        String qualifier = split[1];
        Integer time;

        // checks if input String can be parsed as an Integer
        try {
          time = Integer.parseInt(split[2]);
        } catch (NumberFormatException e) {
          this.printUsage();
          return;
        }

        Integer id = this.clientService.getServerIdByQualifier(qualifier);
        if (id == -1) {
            System.out.println("Server not found.");
            return;
        }

        // register delay <time> for when calling server <qualifier>

        this.clientService.setDelay(id, time);
        System.out.println();
        System.out.println();
    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                "- put <element[,more_elements]>\n" +
                "- read <element[,more_elements]>\n" +
                "- take <element[,more_elements]>\n" +
                "- getTupleSpacesState <server>\n" +
                "- sleep <integer>\n" +
                "- setdelay <server> <integer>\n" +
                "- exit\n");
    }

    private boolean inputIsValid(String[] input){
        if (input.length < 2
            ||
            !input[1].substring(0,1).equals(BGN_TUPLE)
            ||
            !input[1].endsWith(END_TUPLE)
            ||
            input.length > 2
            ) {
            System.out.println("Input validation: " + input[1]);
            this.printUsage();
            return false;
        }
        else {
            return true;
        }
    }
}
