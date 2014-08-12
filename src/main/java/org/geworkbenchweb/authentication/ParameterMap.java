package org.geworkbenchweb.authentication;
import java.util.HashMap;

/**
 * A convenience class for storing request parameters.
 */
public class ParameterMap extends HashMap {

    public String getParameter(String name) {
        Object value = get(name);
        return value == null ? null :
                value instanceof String ?
                        (String) value :
                        ((String[]) value)[0];
    }

    public void addParameter(String name, String value) {
        Object curValue = get(name);
        if (curValue == null) {
            put(name, value);
        } else if (curValue instanceof String) {
            put(name, new String[]{(String) curValue, value});
        } else {
            String[] curArray = (String[]) curValue;
            String[] newArray = new String[curArray.length + 1];
            System.arraycopy(curArray, 0, newArray, 0, curArray.length);
            newArray[newArray.length - 1] = value;
            put(name, newArray);
        }
    }

    public String[] getParameters(String name) {
        Object value = get(name);
        return value == null ? null :
                value instanceof String ?
                        new String[]{(String) value} :
                        (String[]) value;
    }

}