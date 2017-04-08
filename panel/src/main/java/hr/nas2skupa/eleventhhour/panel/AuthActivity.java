package hr.nas2skupa.eleventhhour.panel;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import hr.nas2skupa.eleventhhour.auth.SignInActivity;
import hr.nas2skupa.eleventhhour.common.ui.helpers.DelayedProgressDialog;

/**
 * Created by nas2skupa on 15/03/2017.
 */

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ProgressDialog progressDialog = DelayedProgressDialog.show(this, null, getString(R.string.checking_permissions), 500l);
        progressDialog.setCancelable(false);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("providers")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        progressDialog.dismiss();
                        progressDialog.cancel();

                        List<String> providersKeys = new ArrayList<String>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            providersKeys.add(snapshot.getKey());
                        }
                        if (providersKeys.isEmpty()) {

                            new AlertDialog.Builder(AuthActivity.this)
                                    .setCancelable(false)
                                    .setMessage(String.format(getString(R.string.user_without_providers), user.getDisplayName()))
                                    .setNegativeButton(R.string.action_sign_out, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            signOut();
                                        }
                                    })
                                    .show();
                        } else {
                            String providerKey = providersKeys.get(0);

                            String token = FirebaseInstanceId.getInstance().getToken();
                            FirebaseDatabase.getInstance().getReference()
                                    .child("notificationTokens")
                                    .child("panel")
                                    .child(providerKey)
                                    .child(token)
                                    .setValue(true);

                            MainActivity_.intent(AuthActivity.this)
                                    .providerKey(providerKey)
                                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK).start();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                        progressDialog.cancel();
                        signOut();
                    }
                });
    }

    void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(AuthActivity.this, SignInActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AuthActivity.this, R.string.sign_out_failed, Toast.LENGTH_LONG);
                            finish();
                        }
                    }
                });
    }
}
