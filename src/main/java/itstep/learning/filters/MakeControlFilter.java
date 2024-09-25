package itstep.learning.filters;

import com.google.inject.Singleton;

import javax.servlet.*;
import java.io.IOException;
import java.util.Random;

@Singleton
public class MakeControlFilter implements Filter {

    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        boolean flag = new Random().nextBoolean();
        request.setAttribute("control", flag);
        chain.doFilter(request, response);
    }
}
