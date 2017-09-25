package hr.nas2skupa.eleventhhour.common.ui;


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
import android.widget.EditText;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;

import hr.nas2skupa.eleventhhour.common.R;
import hr.nas2skupa.eleventhhour.common.model.Booking;

/**
 * Dialog for booking a service.
 */
@EFragment(resName = "dialog_make_booking")
public class MakeBookingDialog extends DialogFragment {
    @FragmentArg
    String userKey;
    @FragmentArg
    String providerKey;
    @FragmentArg
    String serviceKey;
    @FragmentArg
    Calendar from;
    @FragmentArg
    Calendar to;
    @FragmentArg
    String userName;
    @FragmentArg
    String providerName;
    @FragmentArg
    String serviceName;
    @FragmentArg
    String price;

    @ViewById TextView txtConfirmation;
    @ViewById EditText txtName;
    @ViewById EditText txtNote;

    private View view;
    private BookingDialogListener listener;

    public MakeBookingDialog() {
        // Required empty public constructor
    }

    public void setBookingDialogListener(BookingDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_make_booking, null);

        builder.setView(view)
                .setTitle(serviceName + " (" + price + ")")
                .setPositiveButton(R.string.make_booking_action, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Booking booking = new Booking(
                                userKey,
                                providerKey,
                                serviceKey,
                                txtName.getText().toString(),
                                providerName,
                                serviceName,
                                price,
                                from.getTimeInMillis(),
                                to.getTimeInMillis(),
                                txtNote.getText().toString()
                        );

                        if (listener != null) listener.onBookingConfirmed(booking);

                        MakeBookingDialog.this.dismiss();
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) listener.onBookingDismissed();
                        MakeBookingDialog.this.dismiss();
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
        txtName.setVisibility(userName == null ? View.VISIBLE : View.GONE);
        txtName.setText(userName);
        txtConfirmation.setText(getString(R.string.confirmation_text,
                DateUtils.formatDateTime(getContext(), from.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE),
                DateUtils.formatDateTime(getContext(), from.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME),
                DateUtils.formatDateTime(getContext(), to.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME)
        ));
    }

    public interface BookingDialogListener {
        void onBookingConfirmed(Booking booking);

        void onBookingDismissed();
    }
}
