package com.carlkarenfort.test;

import androidx.annotation.NonNull;

import java.util.concurrent.CompletableFuture;

import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class CustomContinuation<T> implements Continuation<T> {
    private final CompletableFuture<T> future;

    public CustomContinuation(CompletableFuture<T> future) {
        this.future = future;
    }

    @NonNull
    @Override
    public CoroutineContext getContext() {
        return EmptyCoroutineContext.INSTANCE;
    }

    @Override
    public void resumeWith(@NonNull Object o) {
        if (o instanceof Result.Failure)
            future.completeExceptionally(((Result.Failure) o).exception);
        else
            future.complete((T) o);
    }
}
