package itstep.learning.servlets.shop;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
import java.text.ParseException;
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
        if (req.getMethod().equals("PATCH")) {
            super.resp = resp;
            this.doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = (String) req.getAttribute("Claims.Sid");
        if (userId == null || userId.isEmpty()) {
            String tmpId = (String) req.getAttribute("Claims.TmpSid");
            if(tmpId != null) {
                userId = tmpId;
            }
            else {
                super.sendRest(401, "Auth token required");
                return;
            }
        }
        if (!req.getContentType().startsWith("application/json")) {
            super.sendRest(415, "application/json media type expected");
            return;
        }

        JsonObject json;
        try {
            json = parseBodyAsObject(req);

            JsonElement element = json.get("userId");
            if (element == null) {
                super.sendRest(422, "JSON must have 'userId' field' ");
                return;
            }

            String cartUserId = element.getAsString();
            if (!userId.equals(cartUserId)) {
                super.sendRest(403, "Authorization mismatch");
            }

            element = json.get("productId");
            if (element == null) {
                super.sendRest(422, "JSON must have 'productId' field' ");
                return;
            }
            UUID cartProductId;
            try {
                cartProductId = UUID.fromString(element.getAsString());
            } catch (Exception e) {
                super.sendRest(400, "product id must be a valid UUID");
                return;
            }

            int quantity;
            element = json.get("quantity");
            if (element == null) {
                quantity = 1;
            }
            try {
                quantity = Integer.parseInt(element.getAsString());
                if (quantity <= 0) {
                    super.sendRest(400, "quantity must be a positive integer");
                    return;
                }
            } catch (Exception e) {
                super.sendRest(422, "Invalid quantity");
                return;
            }


            if (cartDao.add(UUID.fromString(userId), cartProductId, quantity)) {
                super.sendRest(201, "CODE WORKS");
            } else {
                super.sendRest(500, "See server log for details");
            }
        } catch (ParseException e) {
            super.sendRest(415, "Bad request");
        }


    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = (String) req.getAttribute("Claims.Sid");
        if (userId == null) {
            String tmpId = (String) req.getAttribute("Claims.TmpSid");
            if(tmpId != null) {
                userId = tmpId;
            }
            else {
                super.sendRest(401, "Auth token required");
                return;
            }
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        super.restResponse.getMeta().setParams(params);
        super.sendRest(200, cartDao.getCart(userId));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject json;
        try {
            json = parseBodyAsObject(req);
        } catch (ParseException ex) {
            super.sendRest(400, "Bad request");
            return;
        }

        JsonElement element = json.get("cartId");
        if (element == null) {
            super.sendRest(422, "JSON must have 'cartId' field' ");
            return;
        }
        String tmpData = element.getAsString();
        UUID cartUuId;
        try {
            cartUuId = UUID.fromString(tmpData);
        } catch (IllegalArgumentException ignored) {
            super.sendRest(400, "Invalid cartId");
            return;
        }

        element = json.get("productId");
        if (element == null) {
            super.sendRest(422, "JSON must have 'productId' field' ");
            return;
        }
        tmpData = element.getAsString();
        UUID producttUuId;
        try {
            producttUuId = UUID.fromString(tmpData);
        } catch (IllegalArgumentException ignored) {
            super.sendRest(400, "Invalid productId");
            return;
        }

        element = json.get("delta");
        if (element == null) {
            super.sendRest(422, "JSON must have 'productId' field' ");
            return;
        }
        int delta = element.getAsInt();

        try {
            if (cartDao.update(cartUuId, producttUuId, delta)) {
                super.sendRest(200, "Updated");
            } else {
                super.sendRest(409, "Invalid data");
            }
        } catch (Exception e) {
            super.sendRest(500, "See server log for details");
        }

    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
        UUID cartId;
        try {
            cartId = UUID.fromString(req.getParameter("cart-id"));
        } catch (IllegalArgumentException ignored) {
            super.sendRest(400, "Missing important field 'cart-id'");
            return;
        }

        boolean isCanceled = Boolean.parseBoolean(req.getParameter("is-canceled"));

        super.sendRest(202, cartDao.closeCart(cartId, isCanceled));
    }

    private JsonObject parseBodyAsObject(HttpServletRequest req) throws ParseException {
        JsonElement json = parseBody(req);
        if (!json.isJsonObject()) {
            throw new ParseException("JSON root must be a JSON object", 422);
        }
        return json.getAsJsonObject();
    }

    private JsonElement parseBody(HttpServletRequest req) throws ParseException {

        if (!req.getContentType().startsWith("application/json")) {
            throw new ParseException("application/json media type expected", 415);
        }

        String jsonString;
        JsonElement json;
        try {
            jsonString = stringReader.read(req.getInputStream());
            try {
                json = gson.fromJson(jsonString, JsonElement.class);
                return json;
            } catch (Exception e) {
                logger.warning(e.getMessage());
                throw new ParseException("JSON could not be parsed", 400);
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }


        return null;
    }
}