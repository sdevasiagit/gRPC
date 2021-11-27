package com.sd.grpc.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import javax.net.ssl.SSLException;
import java.io.File;

public class BlogClient {

    public static void main(String[] args) throws SSLException {
        BlogClient client = new BlogClient();
        client.run();
    }

    public void run() throws SSLException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        ManagedChannel secureChannel = NettyChannelBuilder.forAddress("localhost", 50051)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
                .build();

        doCreate(channel);
    }

    private void doCreate(ManagedChannel channel) {
        BlogServiceGrpc.BlogServiceBlockingStub client = BlogServiceGrpc.newBlockingStub(channel);

        //create
        Blog blog = Blog.newBuilder()
                .setAuthorId("Devasia")
                .setTitle("new Devasia")
                .setContent("twinkle & Adam...")
                .build();
        CreateBlogRequest request = CreateBlogRequest.newBuilder()
                .setBlog(blog)
                .build();
        CreateBlogResponse response = client.createBlog(request);
        System.out.println("create completed: "+ response.toString());

        //Read
        ReadBlogResponse readResponse = client.readBlog(ReadBlogRequest.newBuilder().setBlogId(response.getBlog().getId()).build());
        System.out.println("Read from server : " + readResponse.toString());

        //Update
        Blog newBlog = Blog.newBuilder()
                .setId(readResponse.getBlog().getId())
                .setAuthorId("Devasia")
                .setTitle("new Devasia1")
                .setContent("twinkle & Judith... updated")
                .build();
        UpdateBlogResponse updatedResponse = client
                .updateBlog(UpdateBlogRequest.newBuilder().setBlog(newBlog).build());
        System.out.println("Updated response: "+ updatedResponse.toString());

        //Delete
        DeleteBlogRequest deleteBlogRequest = DeleteBlogRequest.newBuilder()
                .setBlogId(updatedResponse.getBlog().getId())
                .build();
        DeleteBlogResponse deleteResponse = client.deleteBlog(deleteBlogRequest);
        System.out.println("deleted :"+deleteResponse.getBlogId());

        //List
        System.out.println("listing data.......");
        client.listBlog(ListBlogRequest.newBuilder().build()).forEachRemaining(listBlogResponse -> {
            System.out.println(listBlogResponse.getBlog().toString());
        });

    }


}
