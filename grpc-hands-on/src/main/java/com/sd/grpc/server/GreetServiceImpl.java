package com.sd.grpc.server;

import com.proto.greet.*;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();

        String result = "Hello " + firstName;
        GreetResponse response = GreetResponse.newBuilder()
                .setResult(result)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();

        try {
            for (int i = 0; i < 10; i++) {
                String result = "Helo " + firstName + ", sent response " + i;
                GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder()
                        .setResult(result)
                        .build();
                responseObserver.onNext(response);
                Thread.sleep(1000l);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {
        StreamObserver<LongGreetRequest> requestObserver = new StreamObserver<LongGreetRequest>() {
            String result = "";

            @Override
            public void onNext(LongGreetRequest value) {
                //client sends a request
                result += " | Hello : " + value.getGreeting().getFirstName();
                System.out.println();
            }

            @Override
            public void onError(Throwable t) {
                //client sends an error
            }

            @Override
            public void onCompleted() {
                //client is done
                responseObserver.onNext(LongGreetResponse.newBuilder().setResult(result).build());
                responseObserver.onCompleted();
            }
        };
        return requestObserver;
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {

        StreamObserver<GreetEveryoneRequest> requestStreamObserver = new StreamObserver<GreetEveryoneRequest>() {
            @Override
            public void onNext(GreetEveryoneRequest value) {
                responseObserver.onNext(GreetEveryoneResponse.newBuilder()
                        .setResult("Hello..." + value.getGreeting().getFirstName())
                        .build());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };

        return requestStreamObserver;
    }

    @Override
    public void greetSquare(GreetSquareRequest request, StreamObserver<GreetSquareResponse> responseObserver) {

        Integer number = request.getNumber();

        if( number > 0) {
            responseObserver.onNext(GreetSquareResponse.newBuilder().setNumberRoot(Math.sqrt(number)).build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("negative number").asRuntimeException());
        }

        super.greetSquare(request, responseObserver);
    }

    @Override
    public void greetWithDeadline(GreetWithDeadlineRequest request, StreamObserver<GreetWithDeadlineResponse> responseObserver) {
        Context ctx = Context.current();

        for (int i = 0; i < 3; i++) {
            try {
                System.out.println("sleep for 100ms");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("sending response...");
        responseObserver.onNext(GreetWithDeadlineResponse.newBuilder()
                .setResult("Hello.." + request.getGreeting().getFirstName())
                .build());

        responseObserver.onCompleted();
    }
}
