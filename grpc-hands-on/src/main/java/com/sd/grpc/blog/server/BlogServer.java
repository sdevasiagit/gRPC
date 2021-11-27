package com.sd.grpc.blog.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

public class BlogServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50051)
                .addService(new BlogServiceImpl())
                .addService(ProtoReflectionService.newInstance()) //reflection
                .build();

        server.start();
        System.out.println("server started...");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
            System.out.println("server shutdown completed...");
        }));

        server.awaitTermination();
    }
}
