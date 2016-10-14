package hr.nas2skupa.eleventhhour.events;

/**
 * Created by nas2skupa on 08/10/2016.
 */

public class UserRatingChangedEvent {
    private String providerKey;
    private float userRating;

    public UserRatingChangedEvent(String providerKey, float userRating) {
        this.providerKey = providerKey;
        this.userRating = userRating;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public float getUserRating() {
        return userRating;
    }
}
