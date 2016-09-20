package hr.nas2skupa.eleventhhour.model;

import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * User
 */
@IgnoreExtraProperties
public class User {

    private String username;
    private String email;
    private String pictureUrl;

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

    public void setUsername(final String newUsername) {
        username = newUsername;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String newEmail) {
        email = newEmail;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}

