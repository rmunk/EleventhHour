package hr.nas2skupa.eleventhhour.common.model;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

import java.util.HashMap;

/**
 * Created by nas2skupa on 26/07/2017.
 */

public class User {
    @Exclude public String key;

    public String name;
    public String email;
    public String photoUrl;
    public String city;
    public String age;
    public String sex;
    public float rating;
    public int ratings;

    public User() {
    }

    @Exclude
    public HashMap<String, Object> toMap() {
        final HashMap<String, Object> map = new HashMap<>();

        map.put("name", name);
        map.put("email", email);
        map.put("photoUrl", photoUrl);
        map.put("city", city);
        map.put("age", age);
        map.put("sex", sex);
        map.put("rating", rating);
        map.put("ratings", ratings);
        return map;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof User && key != null && key.equals(((User) obj).key);
    }
}
