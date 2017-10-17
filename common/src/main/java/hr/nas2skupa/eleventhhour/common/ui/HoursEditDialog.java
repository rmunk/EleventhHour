package hr.nas2skupa.eleventhhour.common.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.sharedpreferences.Pref;

import hr.nas2skupa.eleventhhour.common.Preferences_;
import hr.nas2skupa.eleventhhour.common.R;
import hr.nas2skupa.eleventhhour.common.R2;
import hr.nas2skupa.eleventhhour.common.databinding.DialogHoursEditBinding;
import hr.nas2skupa.eleventhhour.common.model.OpenHours;

/**
 * Created by nas2skupa on 04/10/2017.
 */

@EFragment(R2.layout.dialog_hours_edit)
public class HoursEditDialog extends DialogFragment {
    @FragmentArg String providerKey;
    @Pref Preferences_ preferences;

    private View view;
    private HoursEditDialogListener listener;
    private DialogHoursEditBinding binding;
    private DatabaseReference hoursReference;

    public HoursEditDialog() {
        // Required empty public constructor
    }

    public void setHoursEditDialogListener(HoursEditDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hoursReference = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("data")
                .child(providerKey)
                .child("hours");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.dialog_hours_edit, null, false);
        view = binding.getRoot();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit hours");
        builder.setView(view)
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // This is handled in onResume to avoid
                        // automatic dismissal of the dialog
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
    public void onResume() {
        super.onResume();
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (binding.getHours() != null && binding.getHours().areValid()) {
                        if (listener != null) listener.onHoursSet(binding.getHours());
                        dialog.dismiss();
                    } else {
                        Snackbar.make(getView(), R.string.provider_error_hours, BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.unbind();
        binding = null;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (listener != null) listener.onCancelled();
    }

    @AfterViews
    void fetchProviderHours() {
        hoursReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                binding.progressBar.setVisibility(View.GONE);
                binding.scrollView.setVisibility(View.VISIBLE);
                if (dataSnapshot.exists()) {
                    OpenHours hours = dataSnapshot.getValue(OpenHours.class);
                    if (hours != null) binding.setHours(hours);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                binding.progressBar.setVisibility(View.GONE);
                binding.scrollView.setVisibility(View.VISIBLE);
            }
        });
    }

    public interface HoursEditDialogListener {
        void onHoursSet(OpenHours hours);

        void onCancelled();
    }
}
