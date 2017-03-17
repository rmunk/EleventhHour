package hr.nas2skupa.eleventhhour.model;

/**
 * Created by nas2skupa on 19/10/2016.
 */

public class Service {
    private String name;
    private String price;
    private int duration;
    private boolean sale;

    public Service() {
    }


    // Getters
    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isSale() {
        return sale;
    }


    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setSale(boolean sale) {
        this.sale = sale;
    }
}
