package itstep.learning.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class RestService {
    public void sendRestError(HttpServletResponse resp, String message) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus("Error");
        restResponse.setData(message);
        sendRest(resp, restResponse);
    }

    public void setRestResponse(HttpServletResponse resp, Object data) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus("OK");
        restResponse.setData(data);
        sendRest(resp, restResponse);
    }

    public void sendRest(HttpServletResponse resp, RestResponse rest) throws IOException {
        resp.setContentType("application/json");
        Gson gson = new GsonBuilder().serializeNulls().create();
        resp.getWriter().write(gson.toJson(rest));
    }

}
