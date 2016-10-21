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

import java.util.Calendar;
import java.util.Locale;

import hr.nas2skupa.eleventhhour.R;

/**
 * Dialog for booking a service.
 */
@EFragment(R.layout.fragment_booking_dialog)
public class BookingDialogFragment extends DialogFragment {
    @FragmentArg
    Calendar dateTime;
    @FragmentArg
    String name;
    @FragmentArg
    String price;
    @FragmentArg
    int duration;

    @ViewById(R.id.txt_confirmation)
    TextView txtConfirmation;

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
        Calendar to = (Calendar) dateTime.clone();
        to.add(Calendar.MINUTE, duration);

        txtConfirmation.setText(String.format(Locale.getDefault(),
                getString(R.string.confirmation_text),
                DateUtils.formatDateTime(getContext(), dateTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE),
                DateUtils.formatDateTime(getContext(), dateTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME),
                DateUtils.formatDateTime(getContext(), to.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME)
        ));
    }
}
