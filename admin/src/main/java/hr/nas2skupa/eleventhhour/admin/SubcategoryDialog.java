package hr.nas2skupa.eleventhhour.admin;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
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

import hr.nas2skupa.eleventhhour.model.Subcategory;

/**
 * Created by nas2skupa on 15/03/2017.
 */

@EFragment(R.layout.dialog_subcategory)
public class SubcategoryDialog extends DialogFragment implements DialogInterface.OnShowListener {
    @FragmentArg String categoryKey;
    @FragmentArg String subcategoryKey;

    @ViewById EditText txtNameUsa;
    @ViewById EditText txtNameHrv;

    @ViewById TextInputLayout layoutNameUsa;
    @ViewById TextInputLayout layoutNameHrv;

    @ViewById CheckBox chkSale;

    private View view;
    Subcategory subcategory = new Subcategory();

    @AfterViews
    void loadSubcategory() {
        if (subcategoryKey == null) return;

        DatabaseReference subcategoryReference = FirebaseDatabase.getInstance().getReference()
                .child("subcategories")
                .child(categoryKey)
                .child(subcategoryKey);

        subcategoryReference.addListenerForSingleValueEvent(new SubcategoryListener());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_subcategory, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null);

        if (subcategoryKey != null) {
            builder.setNeutralButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(String.format(getString(R.string.action_delete_title), subcategory.getLocalName()))
                            .setMessage(String.format(getString(R.string.action_delete_message), subcategory.getLocalName()))
                            .setPositiveButton(getString(R.string.action_delete), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("subcategories")
                                            .child(categoryKey)
                                            .child(subcategoryKey)
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
        if (txtNameUsa.getText().toString().isEmpty()) {
            layoutNameUsa.setError(getString(R.string.subcategory_error_name));
            valid = false;
        } else layoutNameUsa.setError(null);
        if (txtNameHrv.getText().toString().isEmpty()) {
            layoutNameHrv.setError(getString(R.string.subcategory_error_name));
            valid = false;
        } else layoutNameHrv.setError(null);
        return valid;
    }

    @Override
    public void onShow(final DialogInterface dialog) {
        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!validate()) return;

                subcategory.setLocalName("def", txtNameUsa.getText().toString());
                subcategory.setLocalName("hrv", txtNameHrv.getText().toString());

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("subcategories").child(categoryKey);
                if (subcategoryKey == null) subcategoryKey = ref.push().getKey();
                ref.child(subcategoryKey).setValue(subcategory);
                dialog.dismiss();
            }
        });
    }

    private class SubcategoryListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            subcategory = dataSnapshot.getValue(Subcategory.class);

            if (subcategory != null) {
                txtNameUsa.setText(subcategory.getLocalName("def"));
                txtNameHrv.setText(subcategory.getLocalName("hrv"));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}