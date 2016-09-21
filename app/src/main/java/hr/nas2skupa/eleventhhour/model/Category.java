package hr.nas2skupa.eleventhhour.model;

import java.util.HashMap;

/**
 * Created by nas2skupa on 08/09/16.
 */

public class Category {
    private HashMap<String, String> name;
    private String color;
    private String icon;

    public Category() {
    }

    public HashMap<String, String> getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }
}
