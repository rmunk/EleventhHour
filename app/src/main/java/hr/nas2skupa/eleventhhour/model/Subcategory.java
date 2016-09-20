package hr.nas2skupa.eleventhhour.model;

import java.util.HashMap;

/**
 * Created by nas2skupa on 18/09/16.
 */
public class Subcategory {
    private HashMap<String, String> name;

    public Subcategory() {
    }

    public HashMap<String, String> getName() {
        return name;
    }

    public String getLocaleName(String locale) {
        if (name.containsKey(locale)) return name.get(locale);
        else return name.get("def");
    }
}
