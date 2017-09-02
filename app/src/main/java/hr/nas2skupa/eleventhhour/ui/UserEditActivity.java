package hr.nas2skupa.eleventhhour.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.ui.user.UserFragment;
import hr.nas2skupa.eleventhhour.common.ui.user.UserFragment_;

@EActivity(R.layout.activity_user_edit)
public class UserEditActivity extends AppCompatActivity {
    @Extra String userKey;

    private UserFragment userFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userFragment = UserFragment_.builder()
                .userKey(userKey)
                .editable(true)
                .build();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, userFragment)
                .commit();
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        onBackPressed();
    }

    @SuppressWarnings("ConstantConditions")
    void setToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
    }

    public void cancel(View view) {
        onBackPressed();
    }

    public void save(final View view) {
        userFragment.saveUser(new UserFragment.SaveUserListener() {
            @Override
            public void onUserSavedListener(String key, boolean saved) {
                if (saved) {
                    UserDetailsActivity_.intent(UserEditActivity.this).userKey(key).start();
                } else {
                    Snackbar.make(view, R.string.msg_user_save_failed, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}
