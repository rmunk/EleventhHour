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

import com.google.firebase.auth.FirebaseAuth;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Locale;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.common.utils.Utils;
import hr.nas2skupa.eleventhhour.events.MakeNewBookingEvent;

/**
 * Dialog for booking a service.
 */
@EFragment(R.layout.dialog_make_booking)
public class MakeBookingDialog extends DialogFragment {
    @FragmentArg
    String providerKey;
    @FragmentArg
    String serviceKey;
    @FragmentArg
    Calendar from;
    @FragmentArg
    Calendar to;
    @FragmentArg
    String providerName;
    @FragmentArg
    String serviceName;
    @FragmentArg
    String price;

    @ViewById(R.id.txt_booking_confirmation)
    TextView txtConfirmation;
    @ViewById(R.id.txt_booking_note)
    TextView txtNote;

    private View view;


    public MakeBookingDialog() {
        // Required empty public constructor
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
                                Utils.getMyUid(),
                                providerKey,
                                serviceKey,
                                FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                                providerName,
                                serviceName,
                                price,
                                from.getTimeInMillis(),
                                to.getTimeInMillis(),
                                txtNote.getText().toString()
                        );
                        EventBus.getDefault().post(new MakeNewBookingEvent(booking));

                        MakeBookingDialog.this.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
        txtConfirmation.setText(String.format(Locale.getDefault(),
                getString(R.string.confirmation_text),
                DateUtils.formatDateTime(getContext(), from.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE),
                DateUtils.formatDateTime(getContext(), from.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME),
                DateUtils.formatDateTime(getContext(), to.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME)
        ));
    }
}
