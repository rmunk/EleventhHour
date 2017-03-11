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

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setRatingsCnt(int ratingsCnt) {
        this.ratingsCnt = ratingsCnt;
    }

    public void setSale(boolean sale) {
        this.sale = sale;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public void setPayment(HashMap<String, Boolean> payment) {
        this.payment = payment;
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

    public HashMap<String, Object> toMap() {
        final HashMap<String, Object> providerMap = new HashMap<>();

        providerMap.put("category", category);
        providerMap.put("subcategory", subcategory);

        providerMap.put("name", name);
        providerMap.put("address", address);
        providerMap.put("description", description);
        providerMap.put("phone", phone);
        providerMap.put("web", web);
        providerMap.put("email", email);
        providerMap.put("hours", hours);

        providerMap.put("rating", rating);
        providerMap.put("ratingsCnt", ratingsCnt);
        providerMap.put("sale", sale);
        return providerMap;
    }
}
