package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

import java.util.ArrayList;

import static io.grpc.Status.INVALID_ARGUMENT;

public class ServiceImpl extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {

    /** TupleSpaces Implementation */
    private ServerState state = new ServerState();

    @Override
    public void put(TupleSpacesReplicaTotalOrder.PutRequest request, StreamObserver<TupleSpacesReplicaTotalOrder.PutResponse> responseObserver) {
        if (ServerMain.is_debug_enabled()) {
            System.err.println("Received put request: " + request.getNewTuple());
        }
        int seqNumber = request.getSeqNumber();
        String newTuple = request.getNewTuple();
        state.put(newTuple, seqNumber);

        TupleSpacesReplicaTotalOrder.PutResponse response = TupleSpacesReplicaTotalOrder.PutResponse.newBuilder().build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void read(TupleSpacesReplicaTotalOrder.ReadRequest request, StreamObserver<TupleSpacesReplicaTotalOrder.ReadResponse> responseObserver) {

        if (ServerMain.is_debug_enabled()) {
            System.err.println("Received read request: " + request.getSearchPattern());
        }

        try {
            String searchPattern = request.getSearchPattern();
            String result = state.read(searchPattern);

            if (ServerMain.is_debug_enabled()) {
                System.err.println("Read result: " + result);
            }

            TupleSpacesReplicaTotalOrder.ReadResponse response = TupleSpacesReplicaTotalOrder.ReadResponse.newBuilder().setResult(result).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (RuntimeException e){
            if (ServerMain.is_debug_enabled()) {
                System.err.println("Thread interrupted while waiting for match for read");
            }

            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for match for read", e);
        }
    }

    @Override
    public void take(TupleSpacesReplicaTotalOrder.TakeRequest request, StreamObserver<TupleSpacesReplicaTotalOrder.TakeResponse> responseObserver) {
        int seqNumber = request.getSeqNumber();

        if (ServerMain.is_debug_enabled()) {
            System.err.println("Received take request: " + request.getSearchPattern());
        }

        try {
            String searchPattern = request.getSearchPattern();

            String result = state.take(searchPattern, seqNumber);

            if (ServerMain.is_debug_enabled()) {
                System.err.println("Take result: " + result);
            }

            TupleSpacesReplicaTotalOrder.TakeResponse response = TupleSpacesReplicaTotalOrder.TakeResponse.newBuilder().setResult(result).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (RuntimeException e){
            if (ServerMain.is_debug_enabled()) {
                System.err.println("Thread interrupted while waiting for match for takePhase1");
            }
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for match for takePhase1", e);
        }
    }

    @Override
    public void getTupleSpacesState(TupleSpacesReplicaTotalOrder.getTupleSpacesStateRequest request, StreamObserver<TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse> responseObserver ) {

        if (ServerMain.is_debug_enabled()) {
            System.err.println("Received getTupleSpacesState request");
        }

        ArrayList<String> tuples = state.getTupleSpacesState();

        TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse.Builder builder = TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse.newBuilder();

        // Add all tuples to the response
        if (tuples != null) {
            for (String tuple : tuples) {
                if (ServerMain.is_debug_enabled()) {
                    System.err.println("Tuple: " + tuple);
                }
                builder.addTuple(tuple);
            }
        }

        TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse response = builder.build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


}
