package com.sd.grpc.client;

import com.proto.greet.*;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public void run() throws SSLException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        ManagedChannel secureChannel = NettyChannelBuilder.forAddress("localhost", 50051)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
                .build();

        doUnary(secureChannel);
//        doServerStreaming(channel);
//        doClientStreaming(channel);
//        doBidirectionalStreaming(channel);
//        doErrorCall(channel);
//        doDeadlineCall(channel);
        channel.shutdown();
    }

    private void doDeadlineCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Shyam")
                .build();

        GreetWithDeadlineRequest request = GreetWithDeadlineRequest.newBuilder()
                .setGreeting(greeting)
                .build();
        try {
            System.out.println("sending request with 3000ms deadline");
            GreetWithDeadlineResponse response = greetClient
                    .withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(request);
            System.out.println(response.getResult());
        } catch (StatusRuntimeException ex) {
            if(ex.getStatus() == Status.DEADLINE_EXCEEDED){
                System.out.println("Deadline has been exceeded");
            } else {
                ex.printStackTrace();
            }
        }

        try {
            System.out.println("sending request with 100ms deadline");
            GreetWithDeadlineResponse response = greetClient
                    .withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(request);
            System.out.println(response.getResult());
        } catch (StatusRuntimeException ex) {
            if(ex.getStatus() == Status.DEADLINE_EXCEEDED){
                System.out.println("Deadline has been exceeded");
            } else {
                ex.printStackTrace();
            }
        }
    }

    private void doBidirectionalStreaming(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestStreamObserver = asyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.println("response from server: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("server done");
                latch.countDown();
            }
        });

        Arrays.asList("Shyam", "Judith", "Alexi", "Adam").forEach(name -> {
            requestStreamObserver.onNext(GreetEveryoneRequest.newBuilder()
                    .setGreeting(Greeting.newBuilder()
                            .setFirstName(name)
                            .build())
                    .build());
        });

        requestStreamObserver.onCompleted();

        try {
            latch.await(3l, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void doClientStreaming(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestStreamObserver = asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                // response from server
                System.out.println("Received a response from server");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                // error from server
            }

            @Override
            public void onCompleted() {
                // server done sending data
                System.out.println("Server completed sending response");
                latch.countDown();
            }
        });

        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Shyam").build())
                .build());

        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Joseph").build())
                .build());

        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Devasia").build())
                .build());

        requestStreamObserver.onCompleted();

        try {
            latch.await(3l, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doServerStreaming(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        GreetManyTimesRequest request = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Shyam").setLastName("Devasia").build())
                .build();
        greetClient.greetManyTimes(request).forEachRemaining(response -> {
            System.out.println(response.getResult());
        });
    }

    private void doUnary(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Shyam")
                .setLastName("Devasia")
                .build();

        GreetRequest request = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        GreetResponse response = greetClient.greet(request);
        System.out.println("Response: " + response.getResult());
    }

    private void doErrorCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        GreetSquareResponse response = greetClient.greetSquare(GreetSquareRequest.newBuilder().setNumber(-1).build());
        System.out.println("Response: " + response.getNumberRoot());
    }

    public static void main(String[] args) throws SSLException {
        GreetingClient client = new GreetingClient();
        client.run();
    }
}
