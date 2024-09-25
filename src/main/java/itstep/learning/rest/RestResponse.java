package itstep.learning.rest;

import java.util.Objects;

public class RestResponse {
    private String status;
    private Object data;

    public Object getData() {
        return data;
    }

    public void setData(Object message) {
        this.data = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
