package hr.nas2skupa.eleventhhour.ui.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Booking;
import hr.nas2skupa.eleventhhour.model.BookingStatus;
import hr.nas2skupa.eleventhhour.utils.StringUtils;

/**
 * Created by nas2skupa on 18/09/16.
 */
public class BookingViewHolder extends RecyclerView.ViewHolder {
    private final TextView txtService;
    private final TextView txtProvider;
    private final TextView txtTime;
    private final TextView txtStatus;


    public BookingViewHolder(View itemView) {
        super(itemView);

        txtService = (TextView) itemView.findViewById(R.id.txt_booking_service);
        txtProvider = (TextView) itemView.findViewById(R.id.txt_booking_provider);
        txtTime = (TextView) itemView.findViewById(R.id.txt_booking_time);
        txtStatus = (TextView) itemView.findViewById(R.id.txt_booking_status);
    }

    public void bind(Context context, Booking booking) {
        txtService.setText(booking.getServiceName());
        txtService.setEnabled(booking.getStatus() != BookingStatus.PROVIDER_REJECTED);
        txtProvider.setText(booking.getProviderName());
        Date from = new Date(booking.getFrom());
        Date to = new Date(booking.getTo());
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        txtTime.setText(String.format(Locale.getDefault(),
                "%s - %s", format.format(from), format.format(to)));
        txtStatus.setText(StringUtils.printBookingStatus(context, booking.getStatus()));
    }
}
