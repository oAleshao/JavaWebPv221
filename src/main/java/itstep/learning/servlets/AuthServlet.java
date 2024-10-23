package itstep.learning.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.RoleDao;
import itstep.learning.dal.dao.TokenDao;
import itstep.learning.dal.dao.UserDao;
import itstep.learning.dal.dao.shop.CartDao;
import itstep.learning.dal.dto.Role;
import itstep.learning.dal.dto.Token;
import itstep.learning.dal.dto.User;
import itstep.learning.rest.RestServlet;
import itstep.learning.services.cacheMaster.CacheMaster;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
public class AuthServlet extends RestServlet {
    private final Logger logger;
    private final UserDao userDao;
    private final TokenDao tokenDao;
    private final RoleDao roleDao;
    private final CacheMaster cacheMaster;
    private final CartDao cartDao;
    int maxAge;

    @Inject
    public AuthServlet(Logger logger,
                       UserDao userDao,
                       TokenDao tokenDao,
                       RoleDao roleDao,
                       CacheMaster cacheMaster,
                       CartDao cartDao) {
        this.logger = logger;
        this.userDao = userDao;
        this.tokenDao = tokenDao;
        this.roleDao = roleDao;
        this.cacheMaster = cacheMaster;
        this.cartDao = cartDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        String authHeader = req.getHeader("Authorization");

        if (authHeader == null) {
            super.sendRest(401, "Missing or empty credentials");
            return;
        }

        if (!authHeader.startsWith("Basic ")) {
            if(authHeader.startsWith("UserGet ")) {
                String tokenId = authHeader.substring("UserGet ".length());
                try{
                    User user = tokenDao.getUserByToken(UUID.fromString(tokenId));
                    super.sendRest(200, user);
                    return;
                }catch (Exception e){
                    super.sendRest(500, e.getMessage());
                    return;
                }
            }
            if(authHeader.startsWith("UserRoleGet ")) {
                String roleId = authHeader.substring("UserRoleGet ".length());
                try{
                    Role role = roleDao.getById(UUID.fromString(roleId));
                    super.sendRest(200, role);
                    return;
                }catch (Exception e){
                    super.sendRest(500, e.getMessage());
                    return;
                }
            }
        }

        this.maxAge = cacheMaster.getMaxAge("token");
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
                super.sendRest(401, "Invalid login or password");
                return;
            }

            UUID lastCartId = cartDao.getLastCart(user.getId());
            String tmpId = req.getParameter("tmp-id");
            if(lastCartId != null) {
                cartDao.closeCart(lastCartId, false);
            }
            if(tmpId != null) {
                cartDao.setNewCart(user.getId().toString(), tmpId);
            }

            Token token = tokenDao.create(user);
            super.sendRest(200, token, this.maxAge);
        }catch (Exception e) {
            logger.warning(e.getMessage());
            super.sendRest(500,e.getMessage());
        }
    }

}
