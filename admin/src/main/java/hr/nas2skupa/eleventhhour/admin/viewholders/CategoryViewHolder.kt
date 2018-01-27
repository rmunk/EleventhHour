package hr.nas2skupa.eleventhhour.admin.viewholders

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import hr.nas2skupa.eleventhhour.common.model.Category
import kotlinx.android.synthetic.main.item_category.view.*

/**
 * Created by nas2skupa on 18/09/16.
 */
class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindToCategory(category: Category) = with(itemView) {
        txt_category_name.text = category.localName
        txt_category_name.isSelected = true
        try {
            category_background.setBackgroundColor(Color.parseColor(category.color))
        } catch (ignored: Exception) {

        }

        Picasso.with(img_category_icon.context).load(category.icon)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fit()
                .into(img_category_icon, object : Callback {
                    override fun onSuccess() {}

                    override fun onError() {
                        //Try again online if cache failed
                        Picasso.with(img_category_icon.context)
                                .load(category.icon)
                                .fit()
                                .into(img_category_icon)
                    }
                })
    }
}
