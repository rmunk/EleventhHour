package hr.nas2skupa.eleventhhour.common.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import hr.nas2skupa.eleventhhour.common.Preferences_;
import hr.nas2skupa.eleventhhour.common.R;
import hr.nas2skupa.eleventhhour.common.model.OpenHours;

/**
 * Created by nas2skupa on 04/10/2017.
 */

@EFragment(resName = "dialog_hours_edit")
public class HoursEditDialog extends DialogFragment {
    private View view;
    private HoursEditDialogListener listener;

    @FragmentArg String providerKey;
    @Pref Preferences_ preferences;

    @ViewById TextView txtMonClosed;
    @ViewById EditText txtMonFrom;
    @ViewById EditText txtMonTo;
    @ViewById TextView txtTueClosed;
    @ViewById EditText txtTueFrom;
    @ViewById EditText txtTueTo;
    @ViewById TextView txtWedClosed;
    @ViewById EditText txtWedFrom;
    @ViewById EditText txtWedTo;
    @ViewById TextView txtThuClosed;
    @ViewById EditText txtThuFrom;
    @ViewById EditText txtThuTo;
    @ViewById TextView txtFriClosed;
    @ViewById EditText txtFriFrom;
    @ViewById EditText txtFriTo;
    @ViewById TextView txtSatClosed;
    @ViewById EditText txtSatFrom;
    @ViewById EditText txtSatTo;
    @ViewById TextView txtSunClosed;
    @ViewById EditText txtSunFrom;
    @ViewById EditText txtSunTo;

    public HoursEditDialog() {
        // Required empty public constructor
    }

    public void setHoursEditDialogListener(HoursEditDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_hours_edit, null);

        builder.setTitle("Edit hours");
        builder.setView(view)
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) listener.onHoursSet(null);
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) listener.onCancelled();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return alertDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (listener != null) listener.onCancelled();
    }

    void fetchProviderHours() {
        FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("data")
                .child(providerKey)
                .child("hours")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            OpenHours value = dataSnapshot.getValue(OpenHours.class);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @CheckedChange(resName = {"switch_mon", "switch_tue", "switch_wed", "switch_thu", "switch_fri", "switch_sat", "switch_sun"})
    void switchToggle(Switch aSwitch, boolean checked) {
        TextView closed;
        EditText from;
        EditText to;

        if (aSwitch.getId() == R.id.switch_mon) {
            closed = txtMonClosed;
            from = txtMonFrom;
            to = txtMonTo;
        } else if (aSwitch.getId() == R.id.switch_tue) {
            closed = txtTueClosed;
            from = txtTueFrom;
            to = txtTueTo;
        } else if (aSwitch.getId() == R.id.switch_wed) {
            closed = txtWedClosed;
            from = txtWedFrom;
            to = txtWedTo;
        } else if (aSwitch.getId() == R.id.switch_thu) {
            closed = txtThuClosed;
            from = txtThuFrom;
            to = txtThuTo;
        } else if (aSwitch.getId() == R.id.switch_fri) {
            closed = txtFriClosed;
            from = txtFriFrom;
            to = txtFriTo;
        } else if (aSwitch.getId() == R.id.switch_sat) {
            closed = txtSatClosed;
            from = txtSatFrom;
            to = txtSatTo;
        } else if (aSwitch.getId() == R.id.switch_sun) {
            closed = txtSunClosed;
            from = txtSunFrom;
            to = txtSunTo;
        } else return;

        closed.setVisibility(checked ? View.INVISIBLE : View.VISIBLE);
        from.setVisibility(checked ? View.VISIBLE : View.INVISIBLE);
        to.setVisibility(checked ? View.VISIBLE : View.INVISIBLE);

        View next = from.focusSearch(View.FOCUS_DOWN);
        if (checked) from.requestFocus();
        else if (next != null) next.requestFocus();
    }

    public interface HoursEditDialogListener {
        void onHoursSet(OpenHours hours);

        void onCancelled();
    }
}
