package hr.nas2skupa.eleventhhour.common.ui.provider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.nas2skupa.eleventhhour.common.Preferences_;
import hr.nas2skupa.eleventhhour.common.R;
import hr.nas2skupa.eleventhhour.common.R2;
import hr.nas2skupa.eleventhhour.common.model.Category;
import hr.nas2skupa.eleventhhour.common.model.City;
import hr.nas2skupa.eleventhhour.common.model.Location;
import hr.nas2skupa.eleventhhour.common.model.OpenHours;
import hr.nas2skupa.eleventhhour.common.model.Provider;
import hr.nas2skupa.eleventhhour.common.model.Subcategory;
import hr.nas2skupa.eleventhhour.common.ui.HoursEditDialog;
import hr.nas2skupa.eleventhhour.common.ui.HoursEditDialog_;
import hr.nas2skupa.eleventhhour.common.ui.helpers.DelayedProgressDialog;

import static android.app.Activity.RESULT_OK;

/**
 * Created by nas2skupa on 06/03/2017.
 */

@EFragment(R2.layout.fragment_provider)
public class ProviderFragment extends Fragment implements ValueEventListener {
    private static final int PLACE_PICKER_REQUEST = 1001;
    private static final int GOOGLE_PLAY_SERVICES_REPAIRABLE_REQUEST = 1002;
    private static final int GOOGLE_PLAY_SERVICES_NOT_AVAILABLE_REQUEST = 1003;
    private static final long PROGRESS_DELAY = 500L;

    @Pref Preferences_ preferences;

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
    @ViewById TextInputLayout layoutHours;

    private boolean locationPickerStarted;
    private boolean dialogOpen;
    private ProgressDialog progressDialog;

    private Provider provider = new Provider();
    private DatabaseReference providerReference;
    private String pickedCategory;
    private HashMap<String, Boolean> pickedSubcategories;
    private String pickedCity;
    private OpenHours setHours;

    public ProviderFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        providerReference = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("data")
                .child(providerKey);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!editable && providerKey != null) {
            providerReference.addValueEventListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!editable && providerKey != null) {
            providerReference.removeEventListener(this);
        }
    }

    @AfterViews
    void loadProvider() {
        if (editable) {
            if (providerKey != null) {
                providerReference.addListenerForSingleValueEvent(this);
            }
            setupCityPicker();
        }
    }

    @Touch(R2.id.editing_shroud)
    boolean consumeClick() {
        return !editable;
    }

    @Touch(R2.id.txt_category)
    boolean pickCategory(View v, MotionEvent event) {
        if (dialogOpen || event.getAction() != MotionEvent.ACTION_UP) return true;
        dialogOpen = true;
        progressDialog = DelayedProgressDialog.show(getContext(), null, getString(R.string.msg_provider_loading_categories), PROGRESS_DELAY);

        FirebaseDatabase.getInstance().getReference()
                .child("app/categories")
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
                                        if (pickedCategory != null && pickedCategory.equals(keys[i])) {
                                            selected[0] = i;
                                        }
                                        i++;
                                    }
                                }

                                progressDialog.dismiss();
                                progressDialog.cancel();

                                new AlertDialog.Builder(getContext())
                                        .setTitle(R.string.provider_pick_category_title)
                                        .setSingleChoiceItems(names, selected[0],
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        selected[0] = which;
                                                    }
                                                })
                                        .setPositiveButton(R.string.action_pick, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialogOpen = false;

                                                pickedCategory = keys[selected[0]];
                                                pickedSubcategories = null;
                                                txtCategory.setText(names[selected[0]]);
                                                txtSubcategories.setText(null);
                                            }
                                        })
                                        .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialogOpen = false;
                                            }
                                        })
                                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                dialogOpen = false;
                                            }
                                        })
                                        .create()
                                        .show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                dialogOpen = false;
                            }
                        }
                );
        return true;
    }

    @Touch(R2.id.txt_subcategories)
    boolean pickSubcategory(View view, MotionEvent event) {
        if (dialogOpen || event.getAction() != MotionEvent.ACTION_UP) return true;
        dialogOpen = true;

        if (pickedCategory == null) {
            Snackbar.make(view, R.string.msg_pick_category_first, Snackbar.LENGTH_SHORT).show();
            dialogOpen = false;
            return true;
        }

        progressDialog = DelayedProgressDialog.show(getContext(), null, getString(R.string.msg_provider_loading_subcategories), 500L);
        FirebaseDatabase.getInstance().getReference()
                .child("app/subcategories")
                .child(pickedCategory)
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
                                        checked[i] = pickedSubcategories != null && pickedSubcategories.containsKey(child.getKey());
                                        i++;
                                    }
                                }

                                progressDialog.dismiss();
                                progressDialog.cancel();

                                new AlertDialog.Builder(getContext())
                                        .setTitle(R.string.provider_pick_subcategory_title)
                                        .setMultiChoiceItems(names, checked,
                                                new DialogInterface.OnMultiChoiceClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                        checked[which] = isChecked;
                                                    }
                                                })
                                        .setPositiveButton(R.string.action_pick, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialogOpen = false;
                                                HashMap<String, Boolean> subcategories = new HashMap<>();
                                                StringBuilder builder = new StringBuilder();
                                                for (int j = 0; j < checked.length; j++) {
                                                    if (checked[j]) {
                                                        subcategories.put(keys[j], true);
                                                        builder.append(names[j]).append(", ");
                                                    }
                                                }
                                                pickedSubcategories = subcategories;
                                                String txt = builder.toString();
                                                if (txt.length() > 0) {
                                                    txtSubcategories.setText(txt.substring(0, txt.length() - 2));
                                                }
                                            }
                                        })
                                        .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialogOpen = false;
                                            }
                                        })
                                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                dialogOpen = false;
                                            }
                                        })
                                        .create()
                                        .show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                dialogOpen = false;
                            }
                        }
                );
        return true;
    }

    @Touch(R2.id.txt_location)
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
        progressDialog = DelayedProgressDialog.show(getContext(), null, getString(R.string.msg_provider_loading_cities), 500L);
        FirebaseDatabase.getInstance().getReference()
                .child("app/cities")
                .child(preferences.country().get())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final List<String> cityNames = new ArrayList<>();
                        final ArrayAdapter<City> cityArrayAdapter = new ArrayAdapter<>(
                                getActivity(), android.R.layout.simple_list_item_1,
                                new ArrayList<City>());

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            final City city = child.getValue(City.class);
                            city.key = child.getKey();
                            cityArrayAdapter.add(city);
                            cityNames.add(city.getLocalName());
                        }
                        progressDialog.dismiss();
                        progressDialog.cancel();
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
                pickedCity = cityArrayAdapter.getItem(position).key;
                txtCity.onEditorAction(EditorInfo.IME_ACTION_NEXT);
            }
        });
        txtCity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                txtCity.dismissDropDown();
                if (pickedCity == null) {
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
                if (position < 0 || cityArrayAdapter.getCount() <= position) pickedCity = null;
                else pickedCity = cityArrayAdapter.getItem(position).key;
            }
        });
    }

    @Touch(R2.id.txt_hours)
    boolean editHours(MotionEvent event) {
        if (dialogOpen || event.getAction() != MotionEvent.ACTION_UP) return true;
        dialogOpen = true;

        HoursEditDialog hoursEditDialog = HoursEditDialog_.builder().providerKey(providerKey).build();
        hoursEditDialog.setHoursEditDialogListener(new HoursEditDialog.HoursEditDialogListener() {
            @Override
            public void onHoursSet(OpenHours hours) {
                setHours = hours;
                dialogOpen = false;
            }

            @Override
            public void onCancelled() {
                dialogOpen = false;
            }
        });
        hoursEditDialog.show(getFragmentManager(), "HoursEditDialog");

        return true;
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
        if (pickedCity == null) {
            layoutCity.setError(getString(R.string.provider_error_city));
            valid = false;
        }
        if (setHours == null || !setHours.areValid()) {
            layoutHours.setError(getString(R.string.provider_error_hours));
            valid = false;
        }
        return valid;
    }

    public void saveProvider(final SaveProviderListener listener) {
        if (!validate()) return;

        provider.name = txtName.getText().toString();
        provider.address = txtAddress.getText().toString();
        provider.description = txtDescription.getText().toString();
        provider.phone = txtPhone.getText().toString();
        provider.web = txtWeb.getText().toString();
        provider.email = txtEmail.getText().toString();
        provider.hours = setHours;

        progressDialog = DelayedProgressDialog.show(getContext(), null, getString(R.string.msg_provider_saving), 500L);
        final HashMap<String, Object> childUpdates = new HashMap<>();

        final GeoLocation location = new GeoLocation(provider.location.latitude, provider.location.longitude);
        GeoHash geoHash = new GeoHash(location);
        Map<String, Object> geoUpdates = new HashMap<>();
        geoUpdates.put(".priority", geoHash.getGeoHashString());
        geoUpdates.put("g", geoHash.getGeoHashString());
        geoUpdates.put("l", Arrays.asList(location.latitude, location.longitude));

        if (providerKey == null) {
            providerKey = FirebaseDatabase.getInstance().getReference()
                    .child("providers")
                    .child(preferences.country().get())
                    .child("data")
                    .push().getKey();
        } else {
            // Remove from old locations
            for (String subcategoryKey : provider.subcategories.keySet()) {
                childUpdates.put(String.format("/providers/%s/bySubcategory/%s/%s/",
                        preferences.country().get(),
                        subcategoryKey,
                        providerKey),
                        null);
                childUpdates.put(String.format("/providers/%s/bySubcategoryAndCity/%s/%s/",
                        preferences.country().get(),
                        subcategoryKey + provider.city,
                        providerKey),
                        null);
                childUpdates.put(String.format("/geofire/providers/bySubcategory/%s/%s",
                        subcategoryKey,
                        providerKey),
                        null);
            }
            childUpdates.put(String.format("/geofire/providers/byCategory/%s/%s/",
                    provider.category,
                    providerKey),
                    null);
        }

        provider.city = pickedCity;
        provider.category = pickedCategory;
        provider.subcategories = pickedSubcategories;

        childUpdates.put(String.format("/providers/%s/data/%s/",
                preferences.country().get(),
                providerKey),
                provider.toMap());


        for (String subcategoryKey : provider.subcategories.keySet()) {
            childUpdates.put(String.format("/providers/%s/bySubcategory/%s/%s/",
                    preferences.country().get(),
                    subcategoryKey,
                    providerKey),
                    true);
            childUpdates.put(String.format("/providers/%s/bySubcategoryAndCity/%s/%s/",
                    preferences.country().get(),
                    subcategoryKey + provider.city,
                    providerKey),
                    true);
            childUpdates.put(String.format("/geofire/providers/bySubcategory/%s/%s",
                    subcategoryKey,
                    providerKey),
                    geoUpdates);
        }
        childUpdates.put(String.format("/geofire/providers/byCategory/%s/%s/",
                provider.category,
                providerKey),
                geoUpdates);

        FirebaseDatabase.getInstance().getReference()
                .updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        listener.onProviderSavedListener(providerKey, databaseError == null);

                        progressDialog.dismiss();
                        progressDialog.cancel();
                    }
                });
    }

    private void bindToProvider(final Provider provider) {
        if (!isAdded()) return;

        txtName.setText(provider.name);
        if (provider.location != null) txtLocation.setText(provider.location.toString());
        txtAddress.setText(provider.address);
        txtDescription.setText(provider.description);
        txtPhone.setText(provider.phone);
        txtWeb.setText(provider.web);
        txtEmail.setText(provider.email);
        if (provider.hours != null) txtHours.setText(provider.hours.today());

        layoutName.setVisibility(editable ? View.VISIBLE : View.GONE);
        layoutLocation.setVisibility(editable ? View.VISIBLE : View.GONE);

        if (provider.category != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("app/categories")
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
                    .child("app/subcategories")
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
                    .child("app/cities")
                    .child(preferences.country().get())
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

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        final Provider newProvider = dataSnapshot.getValue(Provider.class);
        if (newProvider == null) return;

        provider = newProvider;
        provider.key = dataSnapshot.getKey();
        bindToProvider(provider);

        pickedCategory = provider.category;
        pickedSubcategories = provider.subcategories;
        pickedCity = provider.city;
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @SuppressWarnings("WeakerAccess")
    public interface SaveProviderListener {
        void onProviderSavedListener(String key, boolean saved);
    }
}
