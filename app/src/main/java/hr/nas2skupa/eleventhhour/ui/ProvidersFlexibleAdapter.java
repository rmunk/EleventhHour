package hr.nas2skupa.eleventhhour.ui;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * Created by nas2skupa on 22/11/2016.
 */

public class ProvidersFlexibleAdapter extends FlexibleAdapter<ProviderItem> {

    public ProvidersFlexibleAdapter(@Nullable List<ProviderItem> items) {
        super(items);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (holder instanceof ProviderItem.ViewHolder) {
            ProviderItem.ViewHolder providerHolder = (ProviderItem.ViewHolder) holder;
            providerHolder.removeProviderListener();
        }
    }
}
