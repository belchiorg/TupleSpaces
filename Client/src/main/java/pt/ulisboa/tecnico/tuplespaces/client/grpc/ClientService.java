package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.client.ClientMain;
import pt.ulisboa.tecnico.tuplespaces.client.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.client.ResponseObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.TokenHandler;

import java.util.*;

public class ClientService {
    OrderedDelayer delayer;

    private final List<String> qualifiers;

    private final List<ManagedChannel> channels;

    private final List<TupleSpacesReplicaGrpc.TupleSpacesReplicaStub> stubs;

    private final TokenHandler tokenHandler;

    private final int numServers;

    public int getServerIdByQualifier(String qualifier) {
        return this.qualifiers.indexOf(qualifier);
    }

    public ClientService(List<List<String>> serverEntries, TokenHandler tokenHandler) {
        this.qualifiers = new ArrayList<>();
        this.channels = new ArrayList<>();
        this.stubs = new ArrayList<>();
        this.tokenHandler = tokenHandler;

        for(List<String> entry: serverEntries) {
            String qualifier = entry.get(0);
            String target = entry.get(1);

            ManagedChannel newChannel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            TupleSpacesReplicaGrpc.TupleSpacesReplicaStub newStub = TupleSpacesReplicaGrpc.newStub(newChannel);

            qualifiers.add(qualifier);
            channels.add(newChannel);
            stubs.add(newStub);
        }
        this.numServers = channels.size();
        this.delayer = new OrderedDelayer(this.numServers);
    }

    public void setDelay(int id, int delay) {
        delayer.setDelay(id, delay);
    }

    public void shutdownService() {
        for (ManagedChannel channel : this.channels)
            channel.shutdownNow();
    }

    public void putService(String tuple, int seqNumber) throws StatusRuntimeException{
        if (ClientMain.is_debug_enabled()) {
            System.err.println("ClientService.putService(" + tuple + ")");
        }

        tokenHandler.incrementMessageId();
        ResponseCollector<TupleSpacesReplicaTotalOrder.PutResponse> responseCollector = new ResponseCollector<>();

        for (Integer i: this.delayer) {
            TupleSpacesReplicaTotalOrder.PutRequest request = TupleSpacesReplicaTotalOrder.PutRequest.newBuilder()
                    .setNewTuple(tuple)
                    .setSeqNumber(seqNumber)
                    .build();
            try {
                stubs.get(i).put(request, new ResponseObserver<>(responseCollector));
            } catch (StatusRuntimeException e){
                System.out.println("Erro ao enviar mensagem: " + e.getStatus());
            }
        }

        try {
            responseCollector.waitForAllResponses(this.numServers);
        } catch (InterruptedException e) {
            // Interrupted (possibly due to shutdownNow)
            System.out.println("While waiting for responses" + e.getMessage());
        }

        ArrayList<TupleSpacesReplicaTotalOrder.PutResponse> responses = (ArrayList<TupleSpacesReplicaTotalOrder.PutResponse>) responseCollector.getResponses();

        if(responseCollector.collectedResponses.size() != this.numServers) {
            System.out.println("Errors while inserting tuple:");
            responseCollector.collectedErrors.forEach((error) -> System.out.println(error.getMessage()));
        }
    }

    public String readService(String searchPattern) throws StatusRuntimeException {
        if (ClientMain.is_debug_enabled()) {
            System.err.println("ClientService.readService(" + searchPattern + ")");
        }

        ResponseCollector<TupleSpacesReplicaTotalOrder.ReadResponse> responseCollector = new ResponseCollector<>();
        tokenHandler.incrementMessageId();

        for (Integer i: this.delayer) {
            TupleSpacesReplicaTotalOrder.ReadRequest request = TupleSpacesReplicaTotalOrder.ReadRequest.newBuilder().setSearchPattern(searchPattern)
                    .build();
            stubs.get(i).read(request, new ResponseObserver<>(responseCollector));
        }

        try {
            responseCollector.waitForFirstResponse();
        } catch (InterruptedException e) {
            // Interrupted (possibly due to shutdownNow)
            System.out.println(e.getMessage());
        }

        if (responseCollector.collectedResponses.size() != 1) {
            System.out.println("Errors while reading tuple:");
            responseCollector.collectedErrors.forEach((error) -> System.out.println(error.getMessage()));
            return "";
        }

        TupleSpacesReplicaTotalOrder.ReadResponse response = responseCollector.getFirstResponse();

        if (ClientMain.is_debug_enabled()) {
            System.err.println("ReadService response: " + response.getResult());
        }

        return response.getResult();
    }

    public String takeService(String searchPattern, int seqNumber) throws StatusRuntimeException {
        if (ClientMain.is_debug_enabled()) {
            System.err.println("ClientService.takeService(" + searchPattern + ")");
        }

        ResponseCollector<TupleSpacesReplicaTotalOrder.TakeResponse> responseCollector;
        tokenHandler.incrementMessageId();

        while(true) {
            responseCollector = new ResponseCollector<>();


            for (Integer i : this.delayer) {
                TupleSpacesReplicaTotalOrder.TakeRequest request = TupleSpacesReplicaTotalOrder.TakeRequest.newBuilder()
                        .setSearchPattern(searchPattern)
                        .setSeqNumber(seqNumber)
                        .build();

                // Corre a primeira fase do take
                stubs.get(i).take(request, new ResponseObserver<>(responseCollector));
            }

            try {
                responseCollector.waitForAllResponses(this.numServers);

                List<TupleSpacesReplicaTotalOrder.TakeResponse> responses = responseCollector.getResponses();

                if (responses.size() != this.numServers) {
                    System.out.println("Errors while taking tuple:");
                    responseCollector.collectedErrors.forEach((error) -> System.out.println(error.getMessage()));
                    return "";
                }

                return responses.get(0).getResult();
            } catch (InterruptedException e) {
                // Interrupted (possibly due to shutdownNow)
                System.out.println(e.getMessage());
            }
        }
    }

    public List<String> getTupleSpacesStateService(String qualifier) {
        if (ClientMain.is_debug_enabled()) {
            System.err.println("ClientService.getTupleSpacesStateService()");
        }
        ResponseCollector<TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse> responseCollector = new ResponseCollector<>();

        for (Integer i: this.delayer) {
            if(this.qualifiers.get(i).equals(qualifier)) {
                stubs.get(i).getTupleSpacesState(TupleSpacesReplicaTotalOrder.getTupleSpacesStateRequest.newBuilder().build(), new ResponseObserver<>(responseCollector));
            }
        }
        try {
            responseCollector.waitForFirstResponse();
        } catch (InterruptedException e) {
            // Interrupted (possibly due to shutdownNow)
            System.out.println(e.getMessage());
        }
        TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse response = responseCollector.getFirstResponse();

        return response.getTupleList();

    }
}
