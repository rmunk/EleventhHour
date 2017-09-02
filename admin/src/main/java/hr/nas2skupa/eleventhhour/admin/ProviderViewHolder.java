package hr.nas2skupa.eleventhhour.admin;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import hr.nas2skupa.eleventhhour.common.model.Provider;

/**
 * Created by nas2skupa on 28/02/2017.
 */

public class ProviderViewHolder extends RecyclerView.ViewHolder {
    private TextView titleView;


    public ProviderViewHolder(View itemView) {
        super(itemView);

        titleView = (TextView) itemView.findViewById(R.id.txt_provider_name);
    }

    public void bindToProvider(Provider provider) {
        titleView.setText(provider.name);
    }
}