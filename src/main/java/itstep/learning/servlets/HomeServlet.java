package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.RoleDao;
import itstep.learning.dal.dao.TokenDao;
import itstep.learning.dal.dao.UserDao;
import itstep.learning.dal.dao.shop.CartDao;
import itstep.learning.dal.dao.shop.CategoryDao;
import itstep.learning.dal.dao.shop.ProductDao;
import itstep.learning.dal.dto.Role;
import itstep.learning.dal.dto.Token;
import itstep.learning.models.helpers.ProductList;
import itstep.learning.services.hash.HashService;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.util.UUID;

@Singleton
public class HomeServlet extends HttpServlet {

    private final HashService digest;
    private final HashService signature;
    private final UserDao userDao;
    private final TokenDao tokenDao;
    private final CategoryDao categoryDao;
    private final ProductDao productDao;
    private final RoleDao roleDao;
    private final CartDao cartDao;

    @Inject
    public HomeServlet(@Named("digest") HashService digest,
                       @Named("signature") HashService signature,
                       UserDao userDao,
                       TokenDao tokenDao,
                       CategoryDao categoryDao,
                       ProductDao productDao,
                       RoleDao roleDao,
                       CartDao cartDao) {
        this.digest = digest;
        this.signature = signature;
        this.tokenDao = tokenDao;
        this.userDao = userDao;
        this.categoryDao = categoryDao;
        this.productDao = productDao;
        this.roleDao = roleDao;
        this.cartDao = cartDao;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

//        roleDao.add(new Role().setRoleName("admin")
//                .setId(UUID.randomUUID())
//                .setCanCreate(true)
//                .setCanEdit(true)
//                .setCanRead(true)
//                .setCanDelete(true)
//                .setCanBan(true)
//                .setCanBlock(true));
//        roleDao.add(new Role().setRoleName("moderator")
//                .setId(UUID.randomUUID())
//                .setCanCreate(false)
//                .setCanEdit(true)
//                .setCanRead(true)
//                .setCanDelete(true)
//                .setCanBan(false)
//                .setCanBlock(false));
//        roleDao.add(new Role().setRoleName("user")
//                .setId(UUID.randomUUID())
//                .setCanCreate(false)
//                .setCanEdit(false)
//                .setCanRead(false)
//                .setCanDelete(false)
//                .setCanBan(false)
//                .setCanBlock(false));
        try {
            HttpSession session = req.getSession();
            String access = session.getAttribute("access").toString();

            req.setAttribute("access", access);
        } catch (Exception ignore) {
        }


        req.setAttribute("hash", cartDao.installTables() ? "Tables OK" : "Tables NOT OK");
        req.setAttribute("productList", new ProductList().getProductList());
        req.setAttribute("page", "home");
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }
}
