package itstep.learning.ioc;

import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import itstep.learning.filters.*;
import itstep.learning.servlets.*;

public class WebModule extends ServletModule {
    @Override
    protected void configureServlets() {
        filter("/*").through(CharsetFilter.class);
        serve("/").with(HomeServlet.class);
        serve("/servlets").with(ServletsServlet.class);
        serve("/signup").with(SignupServlet.class);
    }
}
