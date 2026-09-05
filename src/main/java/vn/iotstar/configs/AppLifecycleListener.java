package vn.iotstar.configs;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppLifecycleListener
        implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        JPAConfig.shutdown();
    }
}