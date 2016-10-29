package hr.nas2skupa.eleventhhour.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Booking;

/**
 * Created by nas2skupa on 18/09/16.
 */
public class BookingViewHolder extends RecyclerView.ViewHolder {
    private TextView titleView;


    public BookingViewHolder(View itemView) {
        super(itemView);

        titleView = (TextView) itemView.findViewById(R.id.title);
    }

    public void bind(Booking booking) {
        titleView.setText(booking.getServiceName());
    }
}
