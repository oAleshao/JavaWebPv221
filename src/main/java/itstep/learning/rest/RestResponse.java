package itstep.learning.rest;

import java.util.Objects;

public class RestResponse {
    private RestResponseStatus status;
    private RestMetaData meta;
    private Object data;

    public Object getData() {
        return data;
    }

    public RestResponse setData(Object message) {
        this.data = message;
        return this;
    }

    public RestResponseStatus getStatus() {
        return status;
    }

    public RestResponse setStatus(RestResponseStatus status) {
        this.status = status;
        return this;
    }

    public RestResponse setStatus(int  code) {
        return this.setStatus(new RestResponseStatus(code));
    }

    public RestMetaData getMeta() {
        return meta;
    }
    public RestResponse setMeta(RestMetaData meta) {
        this.meta = meta;
        return this;
    }
}
