package hr.nas2skupa.eleventhhour.common.ui.user;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hr.nas2skupa.eleventhhour.common.Preferences;
import hr.nas2skupa.eleventhhour.common.R;
import hr.nas2skupa.eleventhhour.common.model.City;
import hr.nas2skupa.eleventhhour.common.model.User;
import hr.nas2skupa.eleventhhour.common.ui.helpers.DelayedProgressDialog;

/**
 * Created by nas2skupa on 06/03/2017.
 */

@EFragment(resName = "fragment_user")
public class UserFragment extends Fragment {
    private static final long PROGRESS_DELAY = 500L;
    private static final String[] sexes = new String[]{"female", "male"};

    @Pref Preferences preferences;

    @FragmentArg String userKey;
    @FragmentArg Boolean editable;

    @ViewById EditText txtName;
    @ViewById EditText txtEmail;
    @ViewById AutoCompleteTextView txtCity;
    @ViewById EditText txtAge;
    @ViewById EditText txtSex;

    @ViewById TextInputLayout layoutName;
    @ViewById TextInputLayout layoutEmail;
    @ViewById TextInputLayout layoutCity;
    @ViewById TextInputLayout layoutAge;
    @ViewById TextInputLayout layoutSex;

    private boolean pickingAge;
    private boolean pickingSex;
    private ProgressDialog progressDialog;

    private User user = new User();
    private DatabaseReference usersReference = FirebaseDatabase.getInstance().
            getReference().child("users/data");

    public UserFragment() {
    }

    @AfterViews
    void loadUser() {
        if (userKey != null) {
            if (editable) {
                usersReference.child(userKey).addListenerForSingleValueEvent(new UserChangedListener());
            } else {
                usersReference.child(userKey).addValueEventListener(new UserChangedListener());
            }
        }

        layoutName.setVisibility(editable ? View.VISIBLE : View.GONE);
        layoutEmail.setVisibility(editable ? View.GONE : View.VISIBLE);

        setupCityPicker();
    }

    @Touch(resName = "editing_shroud")
    boolean consumeClick() {
        return !editable;
    }

    @FocusChange(resName = "txt_age")
    void pickAge(View v, boolean hasFocus) {
        if (pickingAge || !hasFocus) return;
        pickingAge = true;
        progressDialog = DelayedProgressDialog.show(getContext(), null, getString(R.string.msg_user_loading_age), PROGRESS_DELAY);

        FirebaseDatabase.getInstance().getReference()
                .child("app/ageGroups")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                final int cnt = (int) dataSnapshot.getChildrenCount();
                                final String[] keys = new String[cnt];
                                final int[] selected = {0};

                                int i = 0;
                                for (DataSnapshot child : children) {
                                    keys[i] = child.getKey();
                                    if (user.age != null && user.age.equals(keys[i])) {
                                        selected[0] = i;
                                    }
                                    i++;
                                }

                                progressDialog.dismiss();
                                progressDialog.cancel();

                                new AlertDialog.Builder(

                                        getContext())
                                        .setTitle(R.string.user_pick_age)
                                        .setSingleChoiceItems(keys, selected[0],
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        selected[0] = which;
                                                    }
                                                })
                                        .setPositiveButton(R.string.action_pick, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                pickingAge = false;
                                                user.age = keys[selected[0]];
                                                txtAge.setText(keys[selected[0]]);
                                            }
                                        })
                                        .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                pickingAge = false;
                                            }
                                        })
                                        .create()
                                        .show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                pickingAge = false;
                            }
                        }
                );
    }

    @FocusChange(resName = "txt_sex")
    void pickSex(View view, boolean hasFocus) {
        if (pickingSex || !hasFocus) return;
        pickingSex = true;

        final int[] selected = {0};
        final String[] sexes = getResources().getStringArray(R.array.user_sexes);
        selected[0] = (user.sex != null && user.sex.equals("female")) ? 0 : 1;

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.user_pick_sex)
                .setSingleChoiceItems(sexes, selected[0],
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selected[0] = which;
                            }
                        })
                .setPositiveButton(R.string.action_pick, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pickingSex = false;
                        user.sex = selected[0] == 0 ? "female" : "male";
                        txtSex.setText(sexes[selected[0]]);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pickingSex = false;
                    }
                })
                .create()
                .show();
    }

    private void setupCityPicker() {
        final List<String> cityNames = new ArrayList<>();
        final ArrayAdapter<City> cityArrayAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_list_item_1,
                new ArrayList<City>());

        FirebaseDatabase.getInstance().getReference()
                .child("app/cities")
                .child(preferences.country())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        final City city = dataSnapshot.getValue(City.class);
                        city.key = dataSnapshot.getKey();
                        cityArrayAdapter.add(city);
                        cityNames.add(city.getLocalName());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        FirebaseDatabase.getInstance().getReference()
                .child("app/cities")
                .child(preferences.country())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        setCityListeners(cityNames, cityArrayAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void setCityListeners(final List<String> cityNames, final ArrayAdapter<City> cityArrayAdapter) {
        txtCity.setAdapter(cityArrayAdapter);
        txtCity.setThreshold(0);
        txtCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                txtCity.showDropDown();
            }
        });
        txtCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                user.city = cityArrayAdapter.getItem(position).key;
                txtCity.onEditorAction(EditorInfo.IME_ACTION_NEXT);
            }
        });
        txtCity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                txtCity.dismissDropDown();
                if (user.city == null) {
                    layoutCity.setError(getString(R.string.user_error_city));
                    layoutCity.setErrorEnabled(true);
                    return true;
                } else {
                    layoutCity.setError(null);
                    layoutCity.setErrorEnabled(false);
                    return false;
                }
            }
        });
        txtCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int position = cityNames.indexOf(s.toString());
                if (position < 0 || cityArrayAdapter.getCount() <= position) user.city = null;
                else user.city = cityArrayAdapter.getItem(position).key;
            }
        });
    }

    private boolean validate() {
        boolean valid = true;
        if (txtName.getText().toString().isEmpty()) {
            layoutName.setError(getString(R.string.user_error_name));
            valid = false;
        }
        if (txtEmail.getText().toString().isEmpty() || !txtEmail.getText().toString().matches("^[\\p{L}\\p{N}\\._%+-]+@[\\p{L}\\p{N}\\.\\-]+\\.[\\p{L}]{2,}$")) {
            layoutEmail.setError(getString(R.string.user_error_email));
            valid = false;
        }
        if (user.city == null) {
            layoutCity.setError(getString(R.string.user_error_city));
            valid = false;
        }
        return valid;
    }

    public void saveUser(final SaveUserListener listener) {
        if (!validate()) return;

        progressDialog = DelayedProgressDialog.show(getContext(), null, getString(R.string.msg_user_saving), 500L);

        if (!user.name.equals(txtName.getText().toString())) {
            user.name = txtName.getText().toString();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(txtName.getText().toString())
                    .build();

            currentUser.updateProfile(profileUpdates);
        }
        if (!user.email.equals(txtEmail.getText().toString())) {
            user.email = txtEmail.getText().toString();

            final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            currentUser.updateEmail(txtEmail.getText().toString()).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                        // TODO: reauthenticate
                    }
                }
            });
        }

        usersReference.child(userKey).updateChildren(user.toMap())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.onUserSavedListener(userKey, task.isSuccessful());

                        progressDialog.dismiss();
                        progressDialog.cancel();
                    }
                });
    }

    private void bindToUser(final User user) {
        if (!isAdded()) return;

        txtName.setText(user.name);
        txtEmail.setText(user.email);
        txtAge.setText(user.age);

        if (user.sex != null && Arrays.binarySearch(sexes, user.sex) >= 0) {
            txtSex.setText(getResources().getStringArray(R.array.user_sexes)[Arrays.binarySearch(sexes, user.sex)]);
        }

        if (user.city != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("app/cities")
                    .child(preferences.country())
                    .child(user.city)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            City city = dataSnapshot.getValue(City.class);
                            if (isAdded() && city != null) {
                                txtCity.setText(city.getLocalName());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    private class UserChangedListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final User newUser = dataSnapshot.getValue(User.class);
            if (newUser == null) return;

            user = newUser;
            user.key = dataSnapshot.getKey();
            bindToUser(user);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    @SuppressWarnings("WeakerAccess")
    public interface SaveUserListener {
        void onUserSavedListener(String key, boolean saved);
    }
}
