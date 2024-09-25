package itstep.learning.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.TokenDao;
import itstep.learning.dal.dao.UserDao;
import itstep.learning.dal.dto.Token;
import itstep.learning.dal.dto.User;
import itstep.learning.rest.RestResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Logger;

@Singleton
public class AuthServlet extends HttpServlet {
    private final Logger logger;
    private final UserDao userDao;
    private final TokenDao tokenDao;

    @Inject
    public AuthServlet(Logger logger, UserDao userDao, TokenDao tokenDao) {
        this.logger = logger;
        this.userDao = userDao;
        this.tokenDao = tokenDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String authHeader = req.getHeader("Authorization");

        if (authHeader == null) {
            sendRestError(resp,"Missing or empty credentials");
            return;
        }

        if (!authHeader.startsWith("Basic ")) {
            sendRestError(resp, "Basic Authorization scheme only");
            return;
        }

        String credentials64 = authHeader.substring("Basic ".length());
        String credentials;
        try {
            credentials
                    = new String(
                    Base64.getDecoder().decode(credentials64)
            );
            String[] parts = credentials.split(":", 2);
            User user = userDao.authenticate(parts[0], parts[1]);
            if(user == null) {
                sendRestError(resp, "Invalid login or password");
                return;
            }

            Token token = tokenDao.create(user);
            setRestResponse(resp, token);
        }catch (Exception e) {
            logger.warning(e.getMessage());
            sendRestError(resp,"Illegal credentials format");
        }
    }

    private void sendRestError(HttpServletResponse resp, String message) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus("Error");
        restResponse.setData(message);
        sendRest(resp, restResponse);
    }

    private void setRestResponse(HttpServletResponse resp, Object data) throws IOException {
        RestResponse restResponse = new RestResponse();
        restResponse.setStatus("OK");
        restResponse.setData(data);
        sendRest(resp, restResponse);
    }

    private void sendRest(HttpServletResponse resp, RestResponse rest) throws IOException {
        resp.setContentType("application/json");
        Gson gson = new GsonBuilder().serializeNulls().create();
        resp.getWriter().write(gson.toJson(rest));
    }
}
