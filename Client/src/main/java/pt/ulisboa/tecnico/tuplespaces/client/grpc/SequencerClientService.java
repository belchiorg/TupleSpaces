package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.sequencer.contract.SequencerGrpc;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass;
import pt.ulisboa.tecnico.tuplespaces.client.ClientMain;

import java.util.*;

public class SequencerClientService {

    private final ManagedChannel channel;

    public final SequencerGrpc.SequencerBlockingStub stub;

    public SequencerClientService(String host, String port) {
        final int portInt = Integer.parseInt(port);
        final String target = host + ":" + portInt;

        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = SequencerGrpc.newBlockingStub(channel);
    }

    public void shutdownService() {
        this.channel.shutdownNow();
    }

    public int getSeqNumberService() throws StatusRuntimeException {
        if (ClientMain.is_debug_enabled()) {
            System.out.println("will get sequence number");
        }
        SequencerOuterClass.GetSeqNumberResponse response = this.stub.getSeqNumber(SequencerOuterClass.GetSeqNumberRequest.newBuilder().build());

        if (ClientMain.is_debug_enabled()) {
            System.out.println("Got: " + response.getSeqNumber());
        }
        return response.getSeqNumber();
    }

}
