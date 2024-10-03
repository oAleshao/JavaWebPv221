package itstep.learning.filters.auth;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.TokenDao;
import itstep.learning.dal.dto.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Singleton
public class TokenAuthFilter implements Filter {
    private final TokenDao tokenDao;

    @Inject
    public TokenAuthFilter(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            User user = null;
            String token = authHeader.substring("Bearer ".length());
            try {
                user = tokenDao.getUserByToken(UUID.fromString(token));
            }catch (Exception ignore) {}
            if(user != null) {
                req.setAttribute("Claims.Sid", user.getId().toString());
                req.setAttribute("Claims.Name", user.getName());
                req.setAttribute("Claims.Email", user.getEmail());
                req.setAttribute("Claims.Birthday", user.getBirthday());
                req.setAttribute("Claims.Avatar", user.getAvatar());
            }
        }

        chain.doFilter(req, resp);
    }
}
