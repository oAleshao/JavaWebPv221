package itstep.learning.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class RestService {
    public void sendRestError(HttpServletResponse resp, String message) throws IOException {
        sendRestError(resp, message, 400);
    }

    public void sendRestError(HttpServletResponse resp, String message, int code) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus(new RestResponseStatus(code));
        restResponse.setData(message);
        sendRest(resp, restResponse);
    }

    public void setRestResponse(HttpServletResponse resp, Object data) throws IOException {
        setRestResponse(resp, data, 200);
    }

    public void setRestResponse(HttpServletResponse resp, Object data, int code) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus(new RestResponseStatus(code));
        restResponse.setData(data);
        sendRest(resp, restResponse);
    }

    public void sendRest(HttpServletResponse resp, RestResponse rest) throws IOException {
        sendRest(resp, rest, 0);
    }

    public void sendRest(HttpServletResponse resp, RestResponse rest, int maxAge) throws IOException {
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", maxAge == 0 ? "no-cache" : "max-age=" + maxAge);
        Gson gson = new GsonBuilder().serializeNulls().create();
        resp.getWriter().write(gson.toJson(rest));
    }

}
