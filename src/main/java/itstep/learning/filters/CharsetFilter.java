package itstep.learning.filters;

import com.google.inject.Singleton;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Random;

@Singleton
public class CharsetFilter implements Filter {
    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        request.setAttribute("charset", "UTF-8");

        boolean flag = new Random().nextBoolean();
        request.setAttribute("Control", flag);


        chain.doFilter(request, response);
    }


    @Override
    public void destroy() {
        this.filterConfig = null;
    }
}

/*
* Особливість кодування символів у JSP полягаэ у тому, що її неможливо
* переключити після першого звернення на читання/запис з req/resp.
* Відповідно, перемикання кодування має здіснюватися якомога раніше,
* у первинних фільтрах системи.
*
* Аналогічно до сервлетів, фільтри треба реєструвати і також є три способи
* - web.xml
* - @WebFilter - не гарантується порядок роботи фільтрів
* - IoC (Guice)
* */
