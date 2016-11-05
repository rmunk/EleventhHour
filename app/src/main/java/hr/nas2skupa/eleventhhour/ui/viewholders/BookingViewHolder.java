package hr.nas2skupa.eleventhhour.ui.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.events.ShowBookingDetailsEvent;
import hr.nas2skupa.eleventhhour.model.Booking;
import hr.nas2skupa.eleventhhour.utils.StringUtils;

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

        txtService = (TextView) itemView.findViewById(R.id.txt_booking_service);
        txtProvider = (TextView) itemView.findViewById(R.id.txt_booking_provider);
        txtTime = (TextView) itemView.findViewById(R.id.txt_booking_time);
        txtStatus = (TextView) itemView.findViewById(R.id.txt_booking_status);
        imgMore = (ImageView) itemView.findViewById(R.id.img_more);
        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ShowBookingDetailsEvent(booking));
            }
        });
    }

    public void bind(Context context, Booking booking) {
        this.booking = booking;
        txtService.setText(booking.getServiceName());
        txtService.setEnabled(booking.getStatus() >= 0);
        txtProvider.setText(booking.getProviderName());
        txtTime.setText(booking.getTime());
        if (booking.getTo() > new Date().getTime())
            txtStatus.setText(StringUtils.printBookingStatus(context, booking.getStatus()));
        else txtStatus.setText("Finished");
    }
}
