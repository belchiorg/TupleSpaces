package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.nameserver.contract.NameServer;
import pt.ulisboa.tecnico.nameserver.contract.TupleSpacesNameServerGrpc;
import pt.ulisboa.tecnico.tuplespaces.server.ServerMain;


public class NameService {
    private final ManagedChannel channel;

    public final TupleSpacesNameServerGrpc.TupleSpacesNameServerBlockingStub stub;

    public NameService() {
        final String target = "localhost:5001";

        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = TupleSpacesNameServerGrpc.newBlockingStub(channel);
    }

    public void close() { this.channel.shutdownNow(); }

    public String register(int port, String qualifier) throws StatusRuntimeException {
        if (ServerMain.is_debug_enabled()) {
            System.out.println("NameService.register(" + port + ", " + qualifier + ")");
        }
        return this.stub.register(NameServer.RegisterRequest.newBuilder()
                .setServiceName("TupleSpace")
                .setTarget("localhost:" + port)
                .setQualifier(qualifier)
                .build()).getError();
    }

    public String delete(int port) {
        if (ServerMain.is_debug_enabled()) {
            System.out.println("NameService.delete(" + port + ")");
        }
        return this.stub.delete(NameServer.DeleteRequest.newBuilder()
                .setServiceName("TupleSpace")
                .setTarget("localhost:" + port)
                .build()).getError();
    }
}
