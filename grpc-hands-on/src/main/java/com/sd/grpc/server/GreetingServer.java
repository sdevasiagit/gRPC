package com.sd.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.File;
import java.io.IOException;

public class GreetingServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50051)
                .addService(new GreetServiceImpl())
                .addService(ProtoReflectionService.newInstance()) //reflection
                .build();

        //Secure Server
//        Server server = ServerBuilder.forPort(50051)
//                .addService(new GreetServiceImpl())
//                .useTransportSecurity(
//                        new File("ssl/server.crt"),
//                        new File("ssl/server.pem"))
//                .build();

        server.start();
        System.out.println("server started...");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
            System.out.println("server shutdown completed...");
        }));

        server.awaitTermination();
    }
}
