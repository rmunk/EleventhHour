package hr.nas2skupa.eleventhhour.common.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

/**
 * Created by nas2skupa on 21/09/16.
 */

public class Provider {
    @Exclude public String key;

    public String category;
    public HashMap<String, Boolean> subcategories;

    public String name;
    public float rating;
    public int ratings;
    public boolean hasSale;

    public Location location;
    public String description;
    public String phone;
    public String city;
    public String address;
    public String web;
    public String email;
    public OpenHours hours;
    public HashMap<String, Boolean> payment = new HashMap<>();

    // User related properties
    @Exclude public boolean favorite;
    @Exclude public float userRating;


    public Provider() {
    }

    @Exclude
    public HashMap<String, Object> toMap() {
        final HashMap<String, Object> map = new HashMap<>();

        map.put("category", category);
        map.put("subcategories", subcategories);

        map.put("name", name);
        map.put("location", location);
        map.put("city", city);
        map.put("address", address);
        map.put("description", description);
        map.put("phone", phone);
        map.put("web", web);
        map.put("email", email);
        map.put("hours", hours);

        map.put("rating", rating);
        map.put("ratings", ratings);
        map.put("hasSale", hasSale);
        return map;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Provider && key != null && key.equals(((Provider) obj).key);
    }
}
