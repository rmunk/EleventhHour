package hr.nas2skupa.eleventhhour.admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import hr.nas2skupa.eleventhhour.auth.SignInActivity;


@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.menu_main)
public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance()
                .getReference("admins")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild(user.getUid())) {
                            Toast.makeText(MainActivity.this, user.getDisplayName() + " is not EleventhHour administrator!", Toast.LENGTH_LONG).show();
                            signOut();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        signOut();
                    }
                });
    }

    @OptionsItem(R.id.menu_sign_out)
    void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(MainActivity.this, SignInActivity.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.sign_out_failed, Toast.LENGTH_LONG);
                        }
                    }
                });
    }
}
