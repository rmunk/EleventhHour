package hr.nas2skupa.eleventhhour.common.ui.provider;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.common.R;
import hr.nas2skupa.eleventhhour.common.model.Service;

/**
 * Created by nas2skupa on 15/03/2017.
 */

@EFragment(resName = "dialog_service")
public class ServiceDialog extends DialogFragment implements DialogInterface.OnShowListener {
    @FragmentArg String providerKey;
    @FragmentArg String serviceKey;

    @ViewById EditText txtName;
    @ViewById EditText txtDuration;
    @ViewById EditText txtPrice;

    @ViewById TextInputLayout layoutName;
    @ViewById TextInputLayout layoutDuration;
    @ViewById TextInputLayout layoutPrice;

    @ViewById CheckBox chkSale;

    private View view;
    Service service = new Service();

    @AfterViews
    void loadService() {
        if (serviceKey == null) return;

        DatabaseReference serviceReference = FirebaseDatabase.getInstance().getReference()
                .child("providerServices")
                .child(providerKey)
                .child("data")
                .child(serviceKey);

        serviceReference.addListenerForSingleValueEvent(new ServiceListener());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_service, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null);

        if (serviceKey != null) {
            builder.setNeutralButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(String.format(getString(R.string.action_delete_title), service.name))
                            .setMessage(String.format(getString(R.string.action_delete_message), service.name))
                            .setPositiveButton(getString(R.string.action_delete), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("providerServices")
                                            .child(providerKey)
                                            .child("data")
                                            .child(serviceKey)
                                            .removeValue();
                                }
                            })
                            .setNegativeButton(R.string.action_cancel, null)
                            .create()
                            .show();
                }
            });
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(this);
        return alertDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view;
    }

    private boolean validate() {
        boolean valid = true;
        if (txtName.getText().toString().isEmpty()) {
            layoutName.setError(getString(R.string.service_error_name));
            valid = false;
        } else layoutName.setError(null);
        if (!txtDuration.getText().toString().matches("\\d+")) {
            layoutDuration.setError(getString(R.string.service_error_duration));
            valid = false;
        } else layoutDuration.setError(null);
        if (txtPrice.getText().toString().isEmpty()
                || !txtPrice.getText().toString().matches(getString(R.string.currency_regex))) {
            layoutPrice.setError(getString(R.string.service_error_price));
            valid = false;
        } else layoutPrice.setError(null);
        return valid;
    }

    @Override
    public void onShow(final DialogInterface dialog) {
        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!validate()) return;

                service.name = txtName.getText().toString();
                service.duration = Integer.parseInt(txtDuration.getText().toString());
                service.price = txtPrice.getText().toString();
                service.onSale = chkSale.isChecked();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("providerServices")
                        .child(providerKey)
                        .child("data");
                if (serviceKey == null) serviceKey = reference.push().getKey();
                reference.child(serviceKey).setValue(service);
                dialog.dismiss();
            }
        });
    }

    private class ServiceListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            service = dataSnapshot.getValue(Service.class);

            if (service != null) {
                txtName.setText(service.name);
                txtDuration.setText(String.valueOf(service.duration));
                txtPrice.setText(service.price);
                chkSale.setChecked(service.onSale);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}