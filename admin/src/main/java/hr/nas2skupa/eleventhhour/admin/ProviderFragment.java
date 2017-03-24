package hr.nas2skupa.eleventhhour.admin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.nas2skupa.eleventhhour.model.Category;
import hr.nas2skupa.eleventhhour.model.City;
import hr.nas2skupa.eleventhhour.model.Location;
import hr.nas2skupa.eleventhhour.model.Provider;
import hr.nas2skupa.eleventhhour.model.Subcategory;
import hr.nas2skupa.eleventhhour.ui.helpers.DelayedProgressDialog;

import static android.app.Activity.RESULT_OK;

/**
 * Created by nas2skupa on 06/03/2017.
 */

@EFragment(R.layout.fragment_provider)
public class ProviderFragment extends Fragment {
    private static final int PLACE_PICKER_REQUEST = 1001;
    private static final int GOOGLE_PLAY_SERVICES_REPAIRABLE_REQUEST = 1002;
    private static final int GOOGLE_PLAY_SERVICES_NOT_AVAILABLE_REQUEST = 1003;
    private static final long PROGRESS_DELAY = 500L;

    @FragmentArg String providerKey;
    @FragmentArg Boolean editable;

    @ViewById EditText txtName;
    @ViewById EditText txtCategory;
    @ViewById EditText txtSubcategories;
    @ViewById EditText txtLocation;
    @ViewById AutoCompleteTextView txtCity;
    @ViewById EditText txtAddress;
    @ViewById EditText txtDescription;
    @ViewById EditText txtPhone;
    @ViewById EditText txtWeb;
    @ViewById EditText txtEmail;
    @ViewById EditText txtHours;

    @ViewById TextInputLayout layoutName;
    @ViewById TextInputLayout layoutCategory;
    @ViewById TextInputLayout layoutSubcategories;
    @ViewById TextInputLayout layoutLocation;
    @ViewById TextInputLayout layoutCity;
    @ViewById TextInputLayout layoutAddress;

    private boolean locationPickerStarted;
    private boolean pickingCategory;
    private boolean pickingSubcategory;
    private ProgressDialog progressDialog;

    private Provider provider = new Provider();
    private DatabaseReference providersReference = FirebaseDatabase.getInstance().
            getReference().child("providers");


    @AfterViews
    void loadProvider() {
        if (providerKey != null) {
            if (editable) {
                providersReference.child(providerKey).addListenerForSingleValueEvent(new ProviderChangedListener());
            } else {
                providersReference.child(providerKey).addValueEventListener(new ProviderChangedListener());
            }
        }
        setupCityPicker();
    }

    @Touch(R.id.editing_shroud)
    boolean consumeClick() {
        return !editable;
    }

    @Touch(R.id.txt_category)
    boolean pickCategory(View v, MotionEvent event) {
        if (pickingCategory || event.getAction() != MotionEvent.ACTION_UP) return true;
        pickingCategory = true;
        progressDialog = DelayedProgressDialog.show(getContext(), null, getString(R.string.msg_provider_loading_categories), PROGRESS_DELAY);

        FirebaseDatabase.getInstance().getReference()
                .child("categories")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                final int cnt = (int) dataSnapshot.getChildrenCount();
                                final String[] keys = new String[cnt];
                                final String[] names = new String[cnt];
                                final int[] selected = {0};

                                int i = 0;
                                for (DataSnapshot child : children) {
                                    Category category = child.getValue(Category.class);
                                    if (category != null) {
                                        keys[i] = child.getKey();
                                        names[i] = category.getLocalName();
                                        if (provider.category != null && provider.category.equals(keys[i])) {
                                            selected[0] = i;
                                        }
                                        i++;
                                    }
                                }

                                progressDialog.dismiss();
                                progressDialog.cancel();

                                new AlertDialog.Builder(getContext())
                                        .setTitle("Pick category")
                                        .setSingleChoiceItems(names, selected[0],
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        selected[0] = which;
                                                    }
                                                })
                                        .setPositiveButton("Pick", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                pickingCategory = false;

                                                provider.category = keys[selected[0]];
                                                provider.subcategories = null;
                                                txtCategory.setText(names[selected[0]]);
                                                txtSubcategories.setText(null);
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                pickingCategory = false;
                                            }
                                        })
                                        .create()
                                        .show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                pickingCategory = false;
                            }
                        }
                );
        return true;
    }

    @Touch(R.id.txt_subcategories)
    boolean pickSubcategory(View view, MotionEvent event) {
        if (pickingSubcategory || event.getAction() != MotionEvent.ACTION_UP) return true;
        pickingSubcategory = true;

        if (provider.category == null) {
            Snackbar.make(view, R.string.msg_pick_category_first, Snackbar.LENGTH_SHORT).show();
            pickingSubcategory = false;
            return true;
        }

        progressDialog = DelayedProgressDialog.show(getContext(), null, getString(R.string.msg_provider_loading_subcategories), 500L);
        FirebaseDatabase.getInstance().getReference()
                .child("subcategories")
                .child(provider.category)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                final int cnt = (int) dataSnapshot.getChildrenCount();
                                final String[] keys = new String[cnt];
                                final String[] names = new String[cnt];
                                final boolean[] checked = new boolean[cnt];

                                int i = 0;
                                for (DataSnapshot child : children) {
                                    Subcategory subcategory = child.getValue(Subcategory.class);
                                    if (subcategory != null) {
                                        keys[i] = child.getKey();
                                        names[i] = subcategory.getLocalName();
                                        checked[i] = provider.subcategories != null && provider.subcategories.containsKey(child.getKey());
                                        i++;
                                    }
                                }

                                progressDialog.dismiss();
                                progressDialog.cancel();

                                new AlertDialog.Builder(getContext())
                                        .setTitle("Pick subcategory")
                                        .setMultiChoiceItems(names, checked,
                                                new DialogInterface.OnMultiChoiceClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                        checked[which] = isChecked;
                                                    }
                                                })
                                        .setPositiveButton("Pick", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                pickingSubcategory = false;
                                                HashMap<String, Boolean> subcategories = new HashMap<>();
                                                StringBuilder builder = new StringBuilder();
                                                for (int j = 0; j < checked.length; j++) {
                                                    if (checked[j]) {
                                                        subcategories.put(keys[j], true);
                                                        builder.append(names[j]).append(", ");
                                                    }
                                                }
                                                provider.subcategories = subcategories;
                                                String txt = builder.toString();
                                                if (txt.length() > 0) {
                                                    txtSubcategories.setText(txt.substring(0, txt.length() - 2));
                                                }
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                pickingSubcategory = false;
                                            }
                                        })
                                        .create()
                                        .show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                pickingSubcategory = false;
                            }
                        }
                );
        return true;
    }

    @Touch(R.id.txt_location)
    boolean editLocation(View v, MotionEvent event) {
        if (locationPickerStarted || event.getAction() != MotionEvent.ACTION_UP) return true;
        startLocationPicker();
        return true;
    }

    private void startLocationPicker() {
        progressDialog = DelayedProgressDialog.show(getContext(), null, getString(R.string.msg_provider_starting_location_picker), 500L);
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
            locationPickerStarted = true;
            progressDialog.dismiss();
            progressDialog.cancel();
        } catch (GooglePlayServicesRepairableException e) {
            if (isAdded()) {
                GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), e.getConnectionStatusCode(), GOOGLE_PLAY_SERVICES_REPAIRABLE_REQUEST).show();
                progressDialog.dismiss();
                progressDialog.cancel();
            }
        } catch (GooglePlayServicesNotAvailableException e) {
            if (isAdded()) {
                GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), e.errorCode, GOOGLE_PLAY_SERVICES_NOT_AVAILABLE_REQUEST).show();
                progressDialog.dismiss();
                progressDialog.cancel();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case PLACE_PICKER_REQUEST:
                locationPickerStarted = false;
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(getContext(), data);
                    String toastMsg = String.format(getString(R.string.msg_provider_location_selected), place.getName());
                    Snackbar.make(getView(), toastMsg, Snackbar.LENGTH_SHORT).show();
                    provider.location = new Location(place.getLatLng());
                    txtLocation.setText(provider.location.toString());
                }
                break;
            case GOOGLE_PLAY_SERVICES_REPAIRABLE_REQUEST:
                if (resultCode == RESULT_OK) startLocationPicker();
                break;
            case GOOGLE_PLAY_SERVICES_NOT_AVAILABLE_REQUEST:
                break;
        }
    }

    private void setupCityPicker() {
        final List<String> cityNames = new ArrayList<>();
        final ArrayAdapter<City> arrayAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_list_item_1,
                new ArrayList<City>());

        FirebaseDatabase.getInstance().getReference()
                .child("cities")
                .child("hrv")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        final City city = dataSnapshot.getValue(City.class);
                        city.key = dataSnapshot.getKey();
                        arrayAdapter.add(city);
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
        txtCity.setAdapter(arrayAdapter);
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
                provider.city = arrayAdapter.getItem(position).key;
                txtCity.onEditorAction(EditorInfo.IME_ACTION_NEXT);
            }
        });
        txtCity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                txtCity.dismissDropDown();
                if (provider.city == null) {
                    layoutCity.setError(getString(R.string.provider_error_city));
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
                if (position < 0) provider.city = null;
                else provider.city = arrayAdapter.getItem(0).key;
            }
        });
    }

    private boolean validate() {
        boolean valid = true;
        if (txtName.getText().toString().isEmpty()) {
            layoutName.setError(getString(R.string.provider_error_name));
            valid = false;
        }
        if (txtCategory.getText().toString().isEmpty()) {
            layoutCategory.setError(getString(R.string.provider_error_category));
            valid = false;
        }
        if (txtSubcategories.getText().toString().isEmpty()) {
            layoutSubcategories.setError(getString(R.string.provider_error_subcategory));
            valid = false;
        }
        if (txtLocation.getText().toString().isEmpty()) {
            layoutLocation.setError(getString(R.string.provider_error_location));
            valid = false;
        }
        if (txtAddress.getText().toString().isEmpty()) {
            layoutAddress.setError(getString(R.string.provider_error_address));
            valid = false;
        }
        if (txtCity.getText().toString().isEmpty()) {
            layoutCity.setError(getString(R.string.provider_error_city));
            valid = false;
        }
        return valid;
    }

    public void saveProvider(final SaveProviderListener listener) {
        if (!validate()) {
            listener.onProviderSavedListener(providerKey, false);
            return;
        }

        provider.name = txtName.getText().toString();
        provider.address = txtAddress.getText().toString();
        provider.description = txtDescription.getText().toString();
        provider.phone = txtPhone.getText().toString();
        provider.web = txtWeb.getText().toString();
        provider.email = txtEmail.getText().toString();
        provider.hours = txtHours.getText().toString();

        progressDialog = DelayedProgressDialog.show(getContext(), null, getString(R.string.msg_provider_saving), 500L);
        FirebaseDatabase.getInstance().getReference()
                .child("subcategories")
                .child(provider.category)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        final HashMap<String, Object> childUpdates = new HashMap<>();

                        final GeoLocation location = new GeoLocation(provider.location.latitude, provider.location.longitude);
                        GeoHash geoHash = new GeoHash(location);
                        Map<String, Object> geoUpdates = new HashMap<>();
                        geoUpdates.put(".priority", geoHash.getGeoHashString());
                        geoUpdates.put("g", geoHash.getGeoHashString());
                        geoUpdates.put("l", Arrays.asList(location.latitude, location.longitude));

                        if (providerKey == null) {
                            providerKey = providersReference.push().getKey();
                        }

                        childUpdates.put("/providers/" + providerKey, provider.toMap());

                        for (DataSnapshot child : children) {
                            String subcategory = child.getKey();
                            boolean isInSubcategory = provider.subcategories.containsKey(subcategory);
                            childUpdates.put(
                                    String.format("/subcategoryProviders/%s/%s",
                                            subcategory,
                                            providerKey),
                                    isInSubcategory ? true : null);
                            childUpdates.put(
                                    String.format("/geofire/providers/%s/%s/%s",
                                            provider.category,
                                            subcategory,
                                            providerKey)
                                    , isInSubcategory ? geoUpdates : null);
                        }

                        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                listener.onProviderSavedListener(providerKey, databaseError == null);

                                progressDialog.dismiss();
                                progressDialog.cancel();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onProviderSavedListener(providerKey, false);

                        progressDialog.dismiss();
                        progressDialog.cancel();
                    }
                });
    }

    private void bindToProvider(final Provider provider) {
        txtName.setText(provider.name);
        if (provider.location != null) txtLocation.setText(provider.location.toString());
        txtAddress.setText(provider.address);
        txtDescription.setText(provider.description);
        txtPhone.setText(provider.phone);
        txtWeb.setText(provider.web);
        txtEmail.setText(provider.email);
        txtHours.setText(provider.hours);

        layoutName.setVisibility(editable ? View.VISIBLE : View.GONE);
        layoutLocation.setVisibility(editable ? View.VISIBLE : View.GONE);

        if (provider.category != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("categories")
                    .child(provider.category)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Category category = dataSnapshot.getValue(Category.class);
                            if (isAdded() && category != null) {
                                txtCategory.setText(category.getLocalName());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        if (provider.subcategories != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("subcategories")
                    .child(provider.category)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String subcategories = "";
                            int cnt = provider.subcategories.size();
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            for (DataSnapshot child : children) {
                                Subcategory subcategory = child.getValue(Subcategory.class);
                                if (subcategory != null && provider.subcategories.containsKey(child.getKey())) {
                                    subcategories += subcategory.getLocalName();
                                    if (--cnt > 0) subcategories += ", ";
                                }
                            }
                            if (isAdded()) txtSubcategories.setText(subcategories);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        if (provider.city != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("cities")
                    .child("hrv")
                    .child(provider.city)
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

    private class ProviderChangedListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final Provider newProvider = dataSnapshot.getValue(Provider.class);
            if (newProvider == null) return;

            provider = newProvider;
            provider.key = dataSnapshot.getKey();
            bindToProvider(provider);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    @SuppressWarnings("WeakerAccess")
    public interface SaveProviderListener {
        void onProviderSavedListener(String key, boolean saved);
    }
}
