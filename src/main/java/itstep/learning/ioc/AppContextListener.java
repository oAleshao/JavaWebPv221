package itstep.learning.ioc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import itstep.learning.services.stream.BaosStringReader;
import itstep.learning.services.stream.StringReader;

import javax.servlet.ServletContextEvent;

public class AppContextListener extends GuiceServletContextListener {

    private StringReader reader = new BaosStringReader();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
    }


    @Override
    protected Injector getInjector() {
        return Guice.createInjector(
                new ServicesModule(reader),
                new WebModule(),
                new DbModule(reader)
        );
    }


    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
    }
}
