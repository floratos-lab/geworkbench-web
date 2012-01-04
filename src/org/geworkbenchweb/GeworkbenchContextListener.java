package org.geworkbenchweb;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class GeworkbenchContextListener implements ServletContextListener {

    public void contextDestroyed(ServletContextEvent arg0) {

    }

    public void contextInitialized(ServletContextEvent arg0) {
        try {
            FacadeFactory.registerFacade("default", true);
            
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        System.setProperty("authentication.password.salt",
                "NikhilReddyPodduturi");
        System.setProperty("authentication.password.validation.length", "6");

    }

}
