package com.busted_moments.core.api.internal;

import java.net.http.HttpRequest;

public abstract class GetRequest<T> extends Request<T>  {
    public GetRequest(Object... args) {
        super(args);
    }

    @Override
    public HttpRequest build() {
        return getBuilder().build();
    }
}
