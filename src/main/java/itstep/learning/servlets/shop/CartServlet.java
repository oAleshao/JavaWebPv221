package itstep.learning.servlets.shop;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mysql.cj.xdevapi.DeleteStatement;
import itstep.learning.dal.dao.shop.CartDao;
import itstep.learning.rest.RestMetaData;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestServlet;
import itstep.learning.services.stream.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
public class CartServlet extends RestServlet {

    private final Logger logger;
    private final CartDao cartDao;
    private final StringReader stringReader;

    @Inject
    public CartServlet(CartDao cartDao,
                       StringReader stringReader,
                       Logger logger) {
        this.cartDao = cartDao;
        this.stringReader = stringReader;
        this.logger = logger;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.restResponse = new RestResponse();
        super.restResponse.setMeta(
                new RestMetaData()
                        .setUri("/shop/cart")
                        .setMethod(req.getMethod())
                        .setLocale("uk-UA")
                        .setServerTime(new Date())
                        .setName("Shop cart API")
                        .setAcceptMethods(new String[]{"GET", "POST", "PUT", "DELETE"})
        );
        super.service(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = (String) req.getAttribute("Claims.Sid");
        if (userId == null || userId.isEmpty()) {
            super.sendRest(401, "Auth token required");
            return;
        }
        if (!req.getContentType().startsWith("application/json")) {
            super.sendRest(415, "application/json media type expected");
            return;
        }

        String jsonString;
        JsonElement json;
        try {
            jsonString = stringReader.read(req.getInputStream());
            try {
                json = gson.fromJson(jsonString, JsonElement.class);
                if (!json.isJsonObject()) {
                    super.sendRest(422, "JSON root must be an object");
                    return;
                }
            }catch (Exception e){
                super.sendRest(400, "JSON could not be parsed");
                return;
            }

            JsonElement element = json.getAsJsonObject().get("userId");
            if (element == null) {
                super.sendRest(422, "JSON must have 'userId' field' ");
                return;
            }

            String cartUserId = element.getAsString();
            if (!userId.equals(cartUserId)) {
                super.sendRest(403, "Authorization mismatch");
            }

            element = json.getAsJsonObject().get("productId");
            if (element == null) {
                super.sendRest(422, "JSON must have 'productId' field' ");
                return;
            }
            UUID cartProductId;
            try {
                cartProductId = UUID.fromString(element.getAsString());
            }catch (Exception e){
                super.sendRest(400, "product id must be a valid UUID");
                return;
            }

            int quantity;
            element = json.getAsJsonObject().get("quantity");
            if (element == null) {
                quantity = 1;
            }
            try {
                quantity = Integer.parseInt(element.getAsString());
                if (quantity <= 0) {
                    super.sendRest(400, "quantity must be a positive integer");
                    return;
                }
            }catch (Exception e){
                super.sendRest(422, "Invalid quantity");
                return;
            }


            if(cartDao.add(UUID.fromString(userId) , cartProductId, quantity)){
                super.sendRest(201, jsonString);
            }
            else {
                super.sendRest(500, "See server log for details");
            }


        } catch (IOException e) {
            logger.warning(e.getMessage());
            super.sendRest(400, "JSON could not be extracted");
        }


    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = (String) req.getAttribute("Claims.Sid");
        if (userId == null || userId.isEmpty()) {
            super.sendRest(401, "Auth token required");
            return;
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        super.restResponse.getMeta().setParams(params);
        super.sendRest(200, cartDao.getCart(userId));
    }
}