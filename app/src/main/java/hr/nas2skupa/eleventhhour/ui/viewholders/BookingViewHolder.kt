package hr.nas2skupa.eleventhhour.ui.viewholders

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.View
import hr.nas2skupa.eleventhhour.common.model.Booking
import hr.nas2skupa.eleventhhour.common.utils.StringUtils
import hr.nas2skupa.eleventhhour.events.ShowBookingDetailsEvent
import kotlinx.android.synthetic.main.item_booking.view.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by nas2skupa on 18/09/16.
 */
class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @SuppressLint("WrongConstant")
    fun bind(booking: Booking) = with(itemView) {
        txt_booking_service.text = booking.serviceName
        txt_booking_service.isEnabled = booking.getStatus() >= 0
        txt_booking_provider.text = booking.providerName
        txt_booking_time.text = booking.time
        txt_booking_status.text = StringUtils.printBookingStatus(booking.getStatus())
        img_more.setOnClickListener { EventBus.getDefault().post(ShowBookingDetailsEvent(booking)) }
    }
}
