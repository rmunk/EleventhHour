package hr.nas2skupa.eleventhhour.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Service;

/**
 * Created by nas2skupa on 19/10/2016.
 */

public class ServiceViewHolder extends RecyclerView.ViewHolder {
    private TextView txtName;
    private ImageView imgSale;
    private TextView txtPrice;


    public ServiceViewHolder(View itemView) {
        super(itemView);

        txtName = (TextView) itemView.findViewById(R.id.txt_service_name);
        imgSale = (ImageView) itemView.findViewById(R.id.img_service_sale);
        txtPrice = (TextView) itemView.findViewById(R.id.txt_service_price);
    }

    public void bindToService(final Service service) {
        txtName.setText(service.getName());
        imgSale.setVisibility(service.isSale() ? View.VISIBLE : View.GONE);
        txtPrice.setText(service.getPrice());
    }
}
