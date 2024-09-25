package itstep.learning.servlets;

import com.google.inject.Singleton;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Singleton
public class ServletsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Boolean flag = (Boolean) req.getAttribute("control");
        if(!flag){
            HttpSession session = req.getSession();
            session.setAttribute("access", "You do not have access to servlet page");

            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        req.setAttribute("page", "servlets");
        req.getRequestDispatcher("WEB-INF/views/_layout.jsp").forward(req, resp);
    }
}
