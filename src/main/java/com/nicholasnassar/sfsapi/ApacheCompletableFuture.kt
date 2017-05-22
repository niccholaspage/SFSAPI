package com.nicholasnassar.sfsapi

import org.apache.http.concurrent.FutureCallback
import java.lang.Exception
import java.util.concurrent.CompletableFuture

class ApacheCompletableFuture<T> : CompletableFuture<T>(), FutureCallback<T> {
    override fun completed(result: T) {
        complete(result)
    }

    override fun failed(ex: Exception) {
        ex.printStackTrace()

        completeExceptionally(ex)
    }

    override fun cancelled() {
        //TODO: Hopefully does not occur
    }
}