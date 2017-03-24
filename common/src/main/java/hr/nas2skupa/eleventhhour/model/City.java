package hr.nas2skupa.eleventhhour.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

import hr.nas2skupa.eleventhhour.utils.Utils;

/**
 * Created by nas2skupa on 18/09/16.
 */
public class City {
    @Exclude public String key;

    public HashMap<String, String> name = new HashMap<>();

    public City() {

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
        return this == obj || obj instanceof City && key != null && key.equals(((City) obj).key);
    }

    @Override
    public String toString() {
        return getLocalName();
    }
}
