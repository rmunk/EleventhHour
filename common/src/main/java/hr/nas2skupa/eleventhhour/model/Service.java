package hr.nas2skupa.eleventhhour.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

/**
 * Created by nas2skupa on 19/10/2016.
 */

public class Service {
    @Exclude public String key;

    public String name;
    public String price;
    public int duration;
    public boolean onSale;

    public Service() {
    }

    @Exclude
    public HashMap<String, Object> toMap() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("price", price);
        map.put("duration", duration);
        map.put("onSale", onSale);
        return map;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Service && key != null && key.equals(((Service) obj).key);
    }
}
