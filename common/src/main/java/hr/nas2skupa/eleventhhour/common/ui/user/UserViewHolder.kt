package hr.nas2skupa.eleventhhour.common.ui.user

import android.support.v7.widget.RecyclerView
import android.view.View
import hr.nas2skupa.eleventhhour.common.model.User
import kotlinx.android.synthetic.main.item_user.view.*

/**
 * Created by nas2skupa on 15/10/2017.
 */
class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(user: User, listener: (User) -> Void) = with(itemView) {
        txt_name.text = user.name
        setOnClickListener { listener(user) }
    }
}
