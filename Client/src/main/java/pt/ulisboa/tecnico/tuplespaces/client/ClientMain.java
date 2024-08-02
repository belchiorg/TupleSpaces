package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.NameServerClientService;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.SequencerClientService;
import pt.ulisboa.tecnico.tuplespaces.client.util.TokenHandler;

import java.util.ArrayList;
import java.util.List;

public class ClientMain {
    private static boolean debug = false;

    public static boolean is_debug_enabled() { return debug; }

    public static void enableDebug() { debug = true; }

    public static void main(String[] args) {
        // receive and print arguments
        System.err.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.err.printf("arg[%d] = %s%n", i, args[i]);
            if (args[i].equals("-debug")) {
                enableDebug();
            }
        }

        // if no arguments are passed, connect to the name server and get the server to connect to
        if (args.length == 0 || (args.length == 1 && args[0].equals("-debug"))) {
            NameServerClientService nameServerClientService = new NameServerClientService("localhost", "5001");
            List<Integer> response;

            try {
                response = nameServerClientService.generateIdService();
            } catch (StatusRuntimeException e) {
                System.err.println("Could not connect to name server, or no server was found. Are these processes running?");
                nameServerClientService.shutdownService();
                return;
            }

            // Fetch the list of servers. This list has entries of these types: [qualifier, target]
            List<List<String>> servers;
            try {
                servers = nameServerClientService.lookupService("TupleSpace", "");
            } catch (StatusRuntimeException e) {
                System.err.println("Could not connect to name server, or no server was found. Are these processes running?");
                nameServerClientService.shutdownService();
                return;
            }

            // Check if there are servers running
            if (servers.isEmpty()) {
                System.out.println("No servers found.");
                nameServerClientService.shutdownService();
                return;
            }
            nameServerClientService.shutdownService();

            // Connect to all servers in the list
            ClientService clientService = new ClientService(servers, new TokenHandler(response.get(0)));

            SequencerClientService sequencerClientService = new SequencerClientService("localhost", "8080");

            // Start reading the requests from clients
            CommandProcessor parser = new CommandProcessor(clientService, sequencerClientService);
            parser.parseInput();

            // The channel should be shutdown before stopping the process.
            clientService.shutdownService();
            sequencerClientService.shutdownService();
        }
        // connect to the server using the host and the port
        else if (args.length == 2 || (args.length == 3 && args[2].equals("-debug"))) {

            // get the host and the port and condense them to a target
            final String target = args[0] + ":" + args[1];
            final String qualifier = args[2];
            final List<List<String>> entry = new ArrayList<>(new ArrayList<>());
            entry.get(0).add(target);
            entry.get(0).add(qualifier);

            // Connect to the server
            ClientService clientService = new ClientService(entry, new TokenHandler());

            SequencerClientService sequencerClientService = new SequencerClientService("localhost", "8080");


            // Start reading the requests from clients
            CommandProcessor parser = new CommandProcessor(clientService, sequencerClientService);
            parser.parseInput();

            // The channel should be shutdown before stopping the process.
            clientService.shutdownService();
            sequencerClientService.shutdownService();
        }
        else {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<host> <port>");
        }
    }
}
