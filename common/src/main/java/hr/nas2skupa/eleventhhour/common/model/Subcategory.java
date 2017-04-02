package hr.nas2skupa.eleventhhour.common.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

import hr.nas2skupa.eleventhhour.common.utils.Utils;

/**
 * Created by nas2skupa on 18/09/16.
 */
public class Subcategory {
    @Exclude public String key;

    public HashMap<String, String> name = new HashMap<>();

    public Subcategory() {

    }

    @Exclude
    public String getLocalName() {
        return Utils.getLocaleString(name);
    }

    @Exclude
    public String getLocalName(String locale) {
        return name.get(locale);
    }

    @Exclude
    public void setLocalName(String locale, String name) {
        this.name.put(locale, name);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Subcategory && key != null && key.equals(((Subcategory) obj).key);
    }
}
