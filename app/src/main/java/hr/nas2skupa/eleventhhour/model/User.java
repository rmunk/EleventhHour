package hr.nas2skupa.eleventhhour.model;

import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

/**
 * User
 */
@IgnoreExtraProperties
public class User {

    private String username;
    private String email;
    private String pictureUrl;
    private HashMap<String, Boolean> favorites;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, Uri pictureUrl) {
        this.username = username;
        this.email = email;
        this.pictureUrl = pictureUrl != null ? pictureUrl.getPath() : null;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public HashMap<String, Boolean> getFavorites() {
        return favorites;
    }
}

