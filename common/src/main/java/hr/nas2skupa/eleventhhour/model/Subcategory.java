package hr.nas2skupa.eleventhhour.model;

import java.util.HashMap;

import hr.nas2skupa.eleventhhour.utils.Utils;

/**
 * Created by nas2skupa on 18/09/16.
 */
public class Subcategory {
    private HashMap<String, String> name;

    public Subcategory() {
    }

    public String getName() {
        return Utils.getLocaleString(name);
    }
}
