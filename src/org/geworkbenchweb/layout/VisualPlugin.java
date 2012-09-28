package org.geworkbenchweb.layout;

import java.lang.reflect.InvocationTargetException;

import org.geworkbenchweb.GeworkbenchRoot;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;

/**
 * Represents one VisualPlugin for type of the node selected in the Menu
 */
abstract public class VisualPlugin {
   
    private static final Object MUTEX = new Object();
    private CustomLayout tutorial;
   
    /**
     * Gets the name of this plugin. Try not to exceed 25 characters too much.
     * 
     * @return
     */
    abstract public String getName();

    /**
     * Gets the dataSetId of this plugin. 
     * 
     * @return
     */
    abstract public Long getDataSetId();
    
    /**
     * Gets the description for this plugin. Should describe what the plugin
     * intends to showcase. May contain HTML. 100 words should be enough, and
     * about 7 rows...
     * 
     * @return the description
     */
    abstract public String getDescription();
    
    /**
     * Check if the plugin is a visualizer
     */
    abstract public boolean checkForVisualizer();
    
    /**
     * Get the example instance. Override if instantiation needs parameters.
     * @param data 
     * 
     * @return
     */
    public Component getLayout(Long dataSetId) {

        String className = this.getClass().getName() + "UI";
        try {
            Class<?> classObject = getClass().getClassLoader().loadClass(
                    className);
           
            try {
				return (Component) classObject.getDeclaredConstructor(Long.class).newInstance(dataSetId);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
		return null;
    }

    public CustomLayout getTutorial() {
        synchronized (MUTEX) {
            if (tutorial == null) {
            	tutorial = new CustomLayout(getName());
            	tutorial.setSizeFull();
            }
        }
        return tutorial;
    }

    public CustomLayout getTutorialHTML() {
        return getTutorial();
    }

    /**
     * Gets the name used when resolving the path for this feature. Usually no
     * need to override, but NOTE that this must be unique within geWorkbench.
     * 
     * @return
     */
    public String getFragmentName() {
        return getClass().getSimpleName();
    }

    /**
     * Gets the base url used to reference theme resources.
     * 
     * @return
     */
    protected static final String getThemeBase() {
        return GeworkbenchRoot.getThemeBase();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        // A plugin is uniquely identified by its class name
        if (obj == null) {
            return false;
        }
        return obj.getClass() == getClass();
    }

    @Override
    public int hashCode() {
        // A plugin is uniquely identified by its class name
        return getClass().hashCode();
    }
}