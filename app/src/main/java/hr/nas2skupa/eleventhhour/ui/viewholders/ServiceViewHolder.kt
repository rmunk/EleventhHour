package hr.nas2skupa.eleventhhour.ui.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import hr.nas2skupa.eleventhhour.common.model.Service
import kotlinx.android.synthetic.main.item_service.view.*

/**
 * Created by nas2skupa on 19/10/2016.
 */

class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(service: Service) = with(itemView) {
        txt_service_name.text = service.name
        img_service_sale.visibility = if (service.onSale) View.VISIBLE else View.GONE
        txt_service_price.text = service.price
        txt_service_duration.text = String.format("%d:%02d", service.duration / 60, service.duration % 60)
    }
}
