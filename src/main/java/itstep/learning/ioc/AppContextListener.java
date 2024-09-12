package itstep.learning.ioc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import javax.servlet.ServletContextEvent;

public class AppContextListener extends GuiceServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);

    }


    @Override
    protected Injector getInjector() {
        return Guice.createInjector(
            new ServicesModule(),
                new WebModule()
        );
    }


    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
    }
}
