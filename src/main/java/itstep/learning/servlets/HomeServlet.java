package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.models.helpers.ProductList;
import itstep.learning.services.hash.HashService;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class HomeServlet extends HttpServlet {

    private final HashService hashService;

    @Inject
    public HomeServlet(@Named("digest") HashService hashService) {
        this.hashService = hashService;
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.setAttribute("productList", new ProductList().getProductList());
        req.setAttribute("hash",
                hashService.digest("123") + "<br>" +
                        hashService.hashCode() + "<br>" +
                        this.hashCode());
        req.setAttribute("page", "home");
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }
}
