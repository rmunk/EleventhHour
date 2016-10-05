package hr.nas2skupa.eleventhhour.events;

/**
 * Created by nas2skupa on 05/10/2016.
 */

public class FavoriteStatusChangedEvent {
    private String providerKey;
    private boolean favorite;

    public FavoriteStatusChangedEvent(String providerKey, boolean favorite) {
        this.providerKey = providerKey;
        this.favorite = favorite;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public boolean isFavorite() {
        return favorite;
    }
}
