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
}
