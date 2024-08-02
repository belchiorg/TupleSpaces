package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.stub.StreamObserver;

/*
    * This class is used to collect the responses from the server
    * Generic type R is the type of the response (e.g. PutResponse, ReadResponse, TakeResponse)
 */
public class ResponseObserver<R> implements StreamObserver<R> {
    ResponseCollector<R> responses;

    public ResponseObserver(ResponseCollector<R> c){
        responses = c;
    }
    public void onNext(R r) {
        responses.addResponse(r);
        if (ClientMain.is_debug_enabled()) {
            System.out.println("Received response: " + r);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        responses.notifyError(throwable);
        if (ClientMain.is_debug_enabled()) {
            System.out.println("Received error: " + throwable);
        }
    }

    @Override
    public void onCompleted() {
        if (ClientMain.is_debug_enabled()) {
            System.out.println("Request completed");
        }
    }
}
