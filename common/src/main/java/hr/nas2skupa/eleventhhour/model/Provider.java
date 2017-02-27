package hr.nas2skupa.eleventhhour.model;

import java.util.HashMap;

/**
 * Created by nas2skupa on 21/09/16.
 */

public class Provider {
    private String key;
    private String category;
    private String subcategory;

    private String name;
    private float rating;
    private int ratingsCnt;
    private Location location;
    private boolean sale;

    private String description;
    private String phone;
    private String address;
    private String web;
    private String email;
    private String hours;
    private HashMap<String, Boolean> payment = new HashMap<>();

    // User related properties
    private boolean favorite;
    private float userRating;


    public Provider() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCategory() {
        return category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public String getName() {
        return name;
    }

    public float getRating() {
        return rating;
    }

    public int getRatingsCnt() {
        return ratingsCnt;
    }

    public boolean isSale() {
        return sale;
    }

    public String getDescription() {
        return description;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getWeb() {
        return web;
    }

    public String getEmail() {
        return email;
    }

    public String getHours() {
        return hours;
    }

    public HashMap<String, Boolean> getPayment() {
        return payment;
    }


    // User related properties
    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public float getUserRating() {
        return userRating;
    }

    public void setUserRating(float userRating) {
        this.userRating = userRating;
    }
}
