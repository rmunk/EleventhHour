package hr.nas2skupa.eleventhhour.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Provider;
import hr.nas2skupa.eleventhhour.utils.Utils;

/**
 * Created by nas2skupa on 18/09/16.
 */
public class ProviderViewHolder extends RecyclerView.ViewHolder {
    private TextView titleView;


    public ProviderViewHolder(View itemView) {
        super(itemView);

        titleView = (TextView) itemView.findViewById(R.id.txt_provider_name);
    }

    public void bindToProvider(Provider provider) {
        titleView.setText(Utils.getLocaleName(provider.getName()));
    }
}
