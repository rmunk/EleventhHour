package hr.nas2skupa.eleventhhour.common.ui.provider;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import hr.nas2skupa.eleventhhour.common.R;
import hr.nas2skupa.eleventhhour.common.model.Service;
import hr.nas2skupa.eleventhhour.common.utils.StringUtils;

/**
 * Created by nas2skupa on 19/10/2016.
 */

public class ServiceViewHolder extends RecyclerView.ViewHolder {
    private TextView txtName;
    private ImageView imgSale;
    private TextView txtPrice;
    private TextView txtDuration;


    public ServiceViewHolder(View itemView) {
        super(itemView);

        txtName = (TextView) itemView.findViewById(R.id.txt_service_name);
        imgSale = (ImageView) itemView.findViewById(R.id.img_service_sale);
        txtPrice = (TextView) itemView.findViewById(R.id.txt_service_price);
        txtDuration = (TextView) itemView.findViewById(R.id.txt_service_duration);
    }

    public void bindToService(final Service service) {
        txtName.setText(service.name);
        imgSale.setVisibility(service.onSale ? View.VISIBLE : View.GONE);
        txtPrice.setText(service.price);
        String duration = StringUtils.minutesToTime(service.duration);
        txtDuration.setText(duration);
    }
}
