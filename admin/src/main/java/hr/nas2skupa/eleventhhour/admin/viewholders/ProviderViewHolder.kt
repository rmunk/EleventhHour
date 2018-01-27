package hr.nas2skupa.eleventhhour.admin.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import hr.nas2skupa.eleventhhour.common.model.Provider
import kotlinx.android.synthetic.main.item_provider.view.*

/**
 * Created by nas2skupa on 28/02/2017.
 */

class ProviderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(provider: Provider) = with(itemView) {
        txt_provider_name.text = provider.name
    }
}