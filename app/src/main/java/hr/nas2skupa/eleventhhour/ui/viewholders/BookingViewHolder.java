package hr.nas2skupa.eleventhhour.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.common.utils.StringUtils;
import hr.nas2skupa.eleventhhour.events.ShowBookingDetailsEvent;

/**
 * Created by nas2skupa on 18/09/16.
 */
public class BookingViewHolder extends RecyclerView.ViewHolder {
    private final TextView txtService;
    private final TextView txtProvider;
    private final TextView txtTime;
    private final TextView txtStatus;
    private final ImageView imgMore;
    private Booking booking;


    public BookingViewHolder(View itemView) {
        super(itemView);

        txtService = itemView.findViewById(R.id.txt_booking_service);
        txtProvider = itemView.findViewById(R.id.txt_booking_provider);
        txtTime = itemView.findViewById(R.id.txt_booking_time);
        txtStatus = itemView.findViewById(R.id.txt_booking_status);
        imgMore = itemView.findViewById(R.id.img_more);
        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ShowBookingDetailsEvent(booking));
            }
        });
    }

    public void bind(Booking booking) {
        this.booking = booking;
        txtService.setText(booking.serviceName);
        txtService.setEnabled(booking.getStatus() >= 0);
        txtProvider.setText(booking.providerName);
        txtTime.setText(booking.getTime());
        txtStatus.setText(StringUtils.printBookingStatus(booking.getStatus()));
    }
}
