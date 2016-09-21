package hr.nas2skupa.eleventhhour.events;

/**
 * Created by nas2skupa on 21/09/16.
 */

public class SubcategorySelectedEvent {
    private String subcategoryKey;

    public SubcategorySelectedEvent(String subcategoryKey) {
        this.subcategoryKey = subcategoryKey;
    }

    public String getSubcategoryKey() {
        return subcategoryKey;
    }
}
