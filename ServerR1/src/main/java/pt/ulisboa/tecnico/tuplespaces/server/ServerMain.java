package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;

import java.io.IOException;

public class ServerMain {

    private static boolean debug = false;

    public static boolean is_debug_enabled() { return debug; }

    public static void enableDebug() { debug = true; }

    public static void main(String[] args) {
        System.err.println("Tuple Spaces Server R1");

        // receive the arguments and print them
        System.err.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.err.printf("arg[%d] = %s%n", i, args[i]);

            // check if the debug flag exists
            if (args[i].equals("-debug")) {
                enableDebug();
            }
        }

        // check arguments
        if (args.length < 1) {
            System.err.println("Server could not be started - Missing argument(s)");
            System.err.printf("Usage: java %s port [qualifier] [-debug]%n", ServerMain.class.getName());
            return;
        }

        // Get the server port and qualifier
        final int port = Integer.parseInt(args[0]);
        final String qualifier = args[1];

        // Connect to a NameServer in order to register the server's port
        NameService nameServerBroker = new NameService();
        try {
            String error = nameServerBroker.register(port, qualifier);
            if (!error.isEmpty()) {
                System.err.printf("NameServer could not register server: %s%n", error);
                nameServerBroker.delete(port);
                nameServerBroker.close();
                return;
            }
        } catch (StatusRuntimeException e) {
            System.out.println("Could not reach the name server. Is it running?");
            nameServerBroker.close();
            return;
        }

        // Get the server port and create the object for sending requests using Grpc
        final BindableService impl = new ServiceImpl();

        // Create and start a server object for listening on the port provided on the arguments
        Server server = ServerBuilder.forPort(port).addService(impl).build();
        try {
            server.start();
        } catch (IOException e) {
            System.err.printf("Server could not be started - IOException: %s%n", e.getMessage());
            nameServerBroker.delete(port);
            nameServerBroker.close();
            return;
        }
        System.out.printf("Server started on port %d%n", port);

        // Create a shutdown hook in order to delete the server's connection details from the
        // name server before exiting the application
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            nameServerBroker.delete(port);
            nameServerBroker.close();
        }));

        // Threads are being run in the background. Wait for them to finish, before terminating the process
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            System.out.printf("Server stopped due to an exception: %s%n", e.getMessage());
        }
        nameServerBroker.delete(port);
        nameServerBroker.close();
    }
}

