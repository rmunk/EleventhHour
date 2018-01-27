package hr.nas2skupa.eleventhhour.ui.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import hr.nas2skupa.eleventhhour.common.model.Subcategory
import kotlinx.android.synthetic.main.item_subcategory.view.*

/**
 * Created by nas2skupa on 18/09/16.
 */
class SubcategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(subcategory: Subcategory) = with(itemView) {
        txt_subcategory_name.text = subcategory.localName
    }
}
