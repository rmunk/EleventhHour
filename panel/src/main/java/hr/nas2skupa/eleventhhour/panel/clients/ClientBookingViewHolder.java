package hr.nas2skupa.eleventhhour.panel.clients;

import android.support.v7.widget.RecyclerView;

import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.panel.databinding.ItemClientBookingBinding;

/**
 * Created by nas2skupa on 15/10/2017.
 */

class ClientBookingViewHolder extends RecyclerView.ViewHolder {
    ItemClientBookingBinding binding;

    public ClientBookingViewHolder(ItemClientBookingBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Booking booking) {
        binding.setBooking(booking);
        binding.executePendingBindings();
    }
}
