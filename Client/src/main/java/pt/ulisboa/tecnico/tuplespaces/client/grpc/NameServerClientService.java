package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.nameserver.contract.TupleSpacesNameServerGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServer;

import java.util.*;

public class NameServerClientService {

    private final ManagedChannel channel;

    public final TupleSpacesNameServerGrpc.TupleSpacesNameServerBlockingStub stub;

    public NameServerClientService(String host, String port) {
        final int portInt = Integer.parseInt(port);
        final String target = host + ":" + portInt;

        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = TupleSpacesNameServerGrpc.newBlockingStub(channel);
    }



    public void shutdownService() {
        this.channel.shutdownNow();
    }

    public List<List<String>> lookupService(String service, String qualifier) throws StatusRuntimeException {
        NameServer.LookupResponse response = this.stub.lookup(NameServer.LookupRequest.newBuilder().setServiceName(service).setQualifier(qualifier).build());

        List<List<String>> servers = new ArrayList<>();
        for (NameServer.ServerInfo serverInfo: response.getServersList()) {
            // For each server, add an entry of type [qualifier, target]
            List<String> serverEntry = new ArrayList<>();
            serverEntry.add(serverInfo.getQualifier());
            serverEntry.add(serverInfo.getTarget());
            servers.add(serverEntry);
        }

        return servers;
    }

    public ArrayList<Integer> generateIdService() throws StatusRuntimeException {
        NameServer.GenerateIdResponse response = this.stub.generateId(NameServer.GenerateIdRequest.getDefaultInstance());
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(response.getClientId());
        list.add(response.getServerNumber());
        return list;
    }


}
