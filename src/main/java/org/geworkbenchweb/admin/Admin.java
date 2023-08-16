package org.geworkbenchweb.admin;
// TODO we should have an admin utility or app eventually

import java.util.HashMap;
import java.util.Map;

import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.persistence.data.AbstractPojo;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class Admin {
    static private long findUserId(String username) {
        if (FacadeFactory.getFacade() == null) {
            try {
                FacadeFactory.registerFacade("default", true);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", username);
        AbstractPojo x = FacadeFactory.getFacade().find("SELECT c FROM User c WHERE c.username = :name", params);
        return x.getId();
    }

    static void unlock(long idOfLockedAccount) {
        if (FacadeFactory.getFacade() == null) {
            try {
                FacadeFactory.registerFacade("default", true);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        User user = FacadeFactory.getFacade().find(User.class, idOfLockedAccount);
        user.clearFailedLoginAttempts();
        user.setAccountLocked(false);
        FacadeFactory.getFacade().store(user);
        System.out.println("user account " + idOfLockedAccount + " unlocked");
    }

    public static void main(String[] args) {
        long id = findUserId(args[0]);
        unlock(id);
    }
}
