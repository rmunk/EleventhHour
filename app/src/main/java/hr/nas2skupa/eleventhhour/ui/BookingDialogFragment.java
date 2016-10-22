package hr.nas2skupa.eleventhhour.ui;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Locale;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.events.MakeNewBookingEvent;
import hr.nas2skupa.eleventhhour.model.Booking;
import hr.nas2skupa.eleventhhour.model.BookingStatus;
import hr.nas2skupa.eleventhhour.utils.Utils;

/**
 * Dialog for booking a service.
 */
@EFragment(R.layout.fragment_booking_dialog)
public class BookingDialogFragment extends DialogFragment {
    @FragmentArg
    String providerKey;
    @FragmentArg
    String serviceKey;
    @FragmentArg
    Calendar from;
    @FragmentArg
    Calendar to;
    @FragmentArg
    String name;
    @FragmentArg
    String price;

    @ViewById(R.id.txt_booking_confirmation)
    TextView txtConfirmation;
    @ViewById(R.id.txt_booking_note)
    TextView txtNote;

    private View view;


    public BookingDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.fragment_booking_dialog, null);

        builder.setView(view)
                .setTitle(name + " (" + price + ")")
                .setPositiveButton(R.string.book_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Booking booking = new Booking(
                                Utils.getMyUid(),
                                providerKey,
                                serviceKey,
                                from.getTimeInMillis(),
                                to.getTimeInMillis(),
                                txtNote.getText().toString(),
                                BookingStatus.PENDING
                        );
                        EventBus.getDefault().post(new MakeNewBookingEvent(booking));

                        BookingDialogFragment.this.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BookingDialogFragment.this.dismiss();
                    }
                });

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view;
    }

    @AfterViews
    public void setConfirmationText() {
        txtConfirmation.setText(String.format(Locale.getDefault(),
                getString(R.string.confirmation_text),
                DateUtils.formatDateTime(getContext(), from.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE),
                DateUtils.formatDateTime(getContext(), from.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME),
                DateUtils.formatDateTime(getContext(), to.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME)
        ));
    }
}
