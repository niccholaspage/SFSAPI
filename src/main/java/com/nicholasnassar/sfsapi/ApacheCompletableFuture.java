package com.nicholasnassar.sfsapi;

import org.apache.http.concurrent.FutureCallback;

import java.util.concurrent.CompletableFuture;

public class ApacheCompletableFuture<T> extends CompletableFuture<T> implements FutureCallback<T> {
    @Override
    public void completed(T result) {
        complete(result);
    }

    @Override
    public void failed(Exception ex) {
        ex.printStackTrace();

        completeExceptionally(ex);
    }

    @Override
    public void cancelled() {
        //TODO: Hopefully does not occur
    }
}
