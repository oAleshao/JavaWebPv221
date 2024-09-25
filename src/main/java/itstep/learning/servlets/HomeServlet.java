package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.TokenDao;
import itstep.learning.dal.dao.UserDao;
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

@Singleton
public class HomeServlet extends HttpServlet {

    private final HashService digest;
    private final HashService signature;
    private final UserDao userDao;
    private final TokenDao tokenDao;

    @Inject
    public HomeServlet(@Named("digest") HashService digest, @Named("signature") HashService signature, UserDao userDao, TokenDao tokenDao) {
        this.digest = digest;
        this.signature = signature;
        this.tokenDao = tokenDao;
        this.userDao = userDao;
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            HttpSession session = req.getSession();
            String access = session.getAttribute("access").toString();

            req.setAttribute("access", access);
        }catch (Exception ignore){}


        req.setAttribute("hash", "Signature hash for '123': " + signature.digest("123"));
        req.setAttribute("productList", new ProductList().getProductList());
        req.setAttribute("page", "home");
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }
}
