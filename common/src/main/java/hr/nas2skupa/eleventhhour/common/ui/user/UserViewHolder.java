package hr.nas2skupa.eleventhhour.common.ui.user;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import hr.nas2skupa.eleventhhour.common.R;
import hr.nas2skupa.eleventhhour.common.model.User;

/**
 * Created by nas2skupa on 15/10/2017.
 */
public class UserViewHolder extends RecyclerView.ViewHolder {
    private TextView titleView;

    public UserViewHolder(View itemView) {
        super(itemView);

        titleView = itemView.findViewById(R.id.txt_name);
    }

    public void bindToUser(final User user) {
        titleView.setText(user.name);
    }
}
