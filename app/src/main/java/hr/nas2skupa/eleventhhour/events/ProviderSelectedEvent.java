package hr.nas2skupa.eleventhhour.events;

import android.view.View;

/**
 * Created by nas2skupa on 08/10/2016.
 */

public class ProviderSelectedEvent {
    View view;
    private String providerKey;

    public ProviderSelectedEvent(View view, String providerKey) {
        this.view = view;
        this.providerKey = providerKey;
    }

    public View getView() {
        return view;
    }

    public String getProviderKey() {
        return providerKey;
    }
}
