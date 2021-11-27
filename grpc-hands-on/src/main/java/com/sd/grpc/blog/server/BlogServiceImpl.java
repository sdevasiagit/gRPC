package com.sd.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("grpcdb");
    private MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {
        System.out.println("Server: Create request");
        Blog blog = request.getBlog();

        Document doc = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        collection.insertOne(doc);
        String id = doc.getObjectId("_id").toString();
        System.out.println("Server: inserted blog {}" + id);


        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder().setId(id)).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
        System.out.println("Server: read request: " + request.getBlogId());
        Document result = null;
        try {
            result = collection.find(eq("_id", new ObjectId(request.getBlogId())))
                    .first();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.NOT_FOUND.asRuntimeException()
            );
        }

        if (result == null) {
            responseObserver.onError(
                    Status.NOT_FOUND.asRuntimeException()
            );
        } else {
            Blog blog = getBlogFromResult(result);
            responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(blog).build());
            responseObserver.onCompleted();
        }
    }

    private Blog getBlogFromResult(Document result) {
        Blog blog = Blog.newBuilder()
                .setAuthorId(result.getString("author_id"))
                .setContent(result.getString("content"))
                .setTitle(result.getString("title"))
                .setId(result.getObjectId("_id").toString())
                .build();
        return blog;
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
        String blogId = request.getBlog().getId();
        System.out.println("Server: upate request: " + blogId);

        Document result = null;
        try {
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.NOT_FOUND.asRuntimeException()
            );
        }

        if (result == null) {
            responseObserver.onError(
                    Status.NOT_FOUND.asRuntimeException()
            );
        } else {
            Document doc = new Document("author_id", request.getBlog().getAuthorId())
                    .append("title", request.getBlog().getTitle())
                    .append("_id", new ObjectId(blogId))
                    .append("content", request.getBlog().getContent());
            collection.replaceOne(eq("_id", result.getObjectId("_id")), doc);
            System.out.println("Blog updated...");
            responseObserver.onNext(UpdateBlogResponse.newBuilder().setBlog(getBlogFromResult(doc)).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {
        System.out.println("Server: delete request: " + request.getBlogId());
        String blogId = request.getBlogId();
        DeleteResult result = null;
        try {
            result = collection.deleteOne(eq("_id", new ObjectId(blogId)));
        } catch (Exception e) {
            responseObserver.onError(
                    Status.NOT_FOUND.asRuntimeException()
            );
        }

        if (result.getDeletedCount() == 0) {
            responseObserver.onError(
                    Status.NOT_FOUND.asRuntimeException()
            );
        } else {
            responseObserver.onNext(DeleteBlogResponse.newBuilder().setBlogId(blogId).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listBlog(ListBlogRequest request, StreamObserver<ListBlogResponse> responseObserver) {
        System.out.println("List request");
        collection.find().forEach(doc -> {
            responseObserver.onNext(ListBlogResponse.newBuilder().setBlog(getBlogFromResult(doc)).build());
        });
        responseObserver.onCompleted();
    }
}
