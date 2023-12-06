package com.busted_moments.core.http;

import java.net.http.HttpRequest;

public abstract class GetRequest<T> extends AbstractRequest<T> {
    public GetRequest(Object... args) {
        super(args);
    }

    @Override
    public HttpRequest build() {
        return getBuilder().build();
    }
}
