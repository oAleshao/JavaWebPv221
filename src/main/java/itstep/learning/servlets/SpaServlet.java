package itstep.learning.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.TokenDao;
import itstep.learning.dal.dto.Token;
import itstep.learning.dal.dto.User;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
public class SpaServlet extends HttpServlet {
    private final Logger logger;
    private final TokenDao tokenDao;
    private final RestService restService;

    @Inject
    public SpaServlet(Logger logger, TokenDao tokenDao, RestService restService) {
        this.logger = logger;
        this.tokenDao = tokenDao;
        this.restService = restService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("page", "spa");
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");

        if (authHeader == null) {
            sendRestError(resp,"Missing or empty credentials");
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            sendRestError(resp, "Bearer Authorization scheme only");
            return;
        }

        String token = authHeader.substring("Bearer ".length());
        UUID tokenId;
        try {
           tokenId = UUID.fromString(token);
        }catch (Exception e) {
            logger.warning(e.getMessage());
            sendRestError(resp,"Illegal token format");
            return;
        }

        try {
            User user = tokenDao.getUserByToken(tokenId);
            setRestResponse(resp,user);
        }catch (Exception ex){
            logger.warning(ex.getMessage());
            sendRestError(resp,ex.getMessage());
            return;
        }
    }



    private void sendRestError(HttpServletResponse resp, String message) throws IOException {
        restService.sendRestError(resp, message);
    }

    private void setRestResponse(HttpServletResponse resp, Object data) throws IOException {
        restService.setRestResponse(resp, data);
    }

    private void sendRest(HttpServletResponse resp, RestResponse rest) throws IOException {
        resp.setContentType("application/json");
        Gson gson = new GsonBuilder().serializeNulls().create();
        resp.getWriter().write(gson.toJson(rest));
    }

}
