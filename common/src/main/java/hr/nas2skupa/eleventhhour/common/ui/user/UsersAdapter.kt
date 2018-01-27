package hr.nas2skupa.eleventhhour.common.ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import hr.nas2skupa.eleventhhour.common.R
import hr.nas2skupa.eleventhhour.common.model.User

/**
 * Created by nas2skupa on 13/11/2017.
 */
internal class UsersAdapter(
        options: FirebaseRecyclerOptions<User>,
        val onUsersLoaded: () -> Unit,
        val onUserSelected: (User) -> Void
) : FirebaseRecyclerAdapter<User, UserViewHolder>(options) {

    override fun onDataChanged() {
        super.onDataChanged()
        onUsersLoaded()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false))
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int, user: User) {
        user.key = getRef(position).key
        holder.bind(user, onUserSelected)
    }
}
