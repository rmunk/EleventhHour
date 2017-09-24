package hr.nas2skupa.eleventhhour.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DimensionPixelOffsetRes;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.auth.SignInActivity;
import hr.nas2skupa.eleventhhour.common.model.User;
import hr.nas2skupa.eleventhhour.common.ui.user.UserFragment;
import hr.nas2skupa.eleventhhour.common.ui.user.UserFragment_;
import hr.nas2skupa.eleventhhour.common.utils.Utils;

@EActivity(R.layout.activity_user_details)
@OptionsMenu(R.menu.menu_user_details)
public class UserDetailsActivity extends DrawerActivity {
    @Extra String userKey;

    @ViewById ViewGroup layoutMain;
    @ViewById AppBarLayout appBar;
    @ViewById CollapsingToolbarLayout toolbarLayout;
    @ViewById NestedScrollView nestedScroll;
    @ViewById ViewGroup ratingHolder;
    @ViewById RatingBar ratingBar;

    @DimensionPixelOffsetRes int appBarHeight;

    private DatabaseReference userReference;
    private ValueEventListener userListener;
    private User user = new User();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userReference = FirebaseDatabase.getInstance().getReference()
                .child("users/data")
                .child(userKey);
        userListener = new UserChangedListener();

        UserFragment userFragment = UserFragment_.builder()
                .userKey(userKey)
                .editable(false)
                .build();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, userFragment)
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();

        userReference.addValueEventListener(userListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        userReference.removeEventListener(userListener);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            appBar.setExpanded(true);
            nestedScroll.setNestedScrollingEnabled(true);
        }
        super.onBackPressed();
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        onBackPressed();
    }

    @OptionsItem(R.id.action_edit)
    void editUser() {
        UserEditActivity_.intent(this).userKey(userKey).start();
    }

    @OptionsItem(R.id.action_delete)
    void deleteUser() {
        new AlertDialog.Builder(this)
                .setTitle(String.format(getString(R.string.action_delete_title), user.name))
                .setMessage(String.format(getString(R.string.action_delete_message), user.name))
                .setPositiveButton(getString(R.string.action_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String myUid = Utils.getMyUid();
                        AuthUI.getInstance()
                                .delete(UserDetailsActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            userReference.removeValue();

                                            // Remove notifications token
                                            String token = FirebaseInstanceId.getInstance().getToken();
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("app/notificationTokens")
                                                    .child("client")
                                                    .child(myUid)
                                                    .child(token).removeValue();

                                            // TODO: Delete user bookings

                                            Intent intent = new Intent(UserDetailsActivity.this, SignInActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(UserDetailsActivity.this, R.string.user_delete_failed, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .create()
                .show();
    }

    @SuppressWarnings("ConstantConditions")
    void setToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void setAppBar(@ViewById(R.id.app_bar) AppBarLayout appBar) {
        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                ratingHolder.setVisibility(verticalOffset < 0 ? View.GONE : View.VISIBLE);
            }
        });
    }

    private class UserChangedListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User newUser = dataSnapshot.getValue(User.class);
            if (newUser == null) return;

            user = newUser;
            user.key = dataSnapshot.getKey();
            toolbarLayout.setTitle(user.name);
            ratingBar.setRating(user.rating);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
