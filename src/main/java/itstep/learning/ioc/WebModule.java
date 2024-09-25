package itstep.learning.ioc;

import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import itstep.learning.filters.*;
import itstep.learning.filters.auth.SessionAuthFilter;
import itstep.learning.servlets.*;

public class WebModule extends ServletModule {
    @Override
    protected void configureServlets() {
        filter("/*").through(CharsetFilter.class);
        filter("/*").through(MakeControlFilter.class);
        filter("/*").through(SessionAuthFilter.class);


        serve("/").with(HomeServlet.class);
        serve("/auth").with(AuthServlet.class);
        serve("/servlets").with(ServletsServlet.class);
        serve("/signup").with(SignupServlet.class);
        serve("/file/*").with(DownloadServlet.class);
        serve("/spa").with(SpaServlet.class);
    }
}
