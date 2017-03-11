package hr.nas2skupa.eleventhhour.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.EditText;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.List;

import hr.nas2skupa.eleventhhour.model.Category;
import hr.nas2skupa.eleventhhour.model.Provider;
import hr.nas2skupa.eleventhhour.model.Subcategory;
import hr.nas2skupa.eleventhhour.utils.StringUtils;

import static android.app.Activity.RESULT_OK;

/**
 * Created by nas2skupa on 06/03/2017.
 */

@EFragment(R.layout.fragment_provider)
public class ProviderFragment extends Fragment {
    private static final int PLACE_PICKER_REQUEST = 1001;
    private static final int GOOGLE_PLAY_SERVICES_REPAIRABLE_REQUEST = 1002;
    private static final int GOOGLE_PLAY_SERVICES_NOT_AVAILABLE_REQUEST = 1003;

    @FragmentArg String providerKey;
    @FragmentArg Boolean editable;

    @ViewById(R.id.txt_name) EditText txtName;
    @ViewById(R.id.txt_category) EditText txtCategory;
    @ViewById(R.id.txt_subcategory) EditText txtSubcategory;
    @ViewById(R.id.txt_location) EditText txtLocation;
    @ViewById(R.id.txt_address) EditText txtAddress;
    @ViewById(R.id.txt_description) EditText txtDescription;
    @ViewById(R.id.txt_phone) EditText txtPhone;
    @ViewById(R.id.txt_web) EditText txtWeb;
    @ViewById(R.id.txt_email) EditText txtEmail;
    @ViewById(R.id.txt_hours) EditText txtHours;

    @ViewById(R.id.layout_name) TextInputLayout layoutName;
    @ViewById(R.id.layout_location) TextInputLayout layoutLocation;

    private boolean locationPickerStarted;
    private boolean pickingCategory;
    private boolean pickingSubcategory;
    private LatLng providerLocation;
    private Provider provider;
    private DatabaseReference providersReference = FirebaseDatabase.getInstance().
            getReference().child("providers");


    @AfterViews
    void loadProvider() {
        if (providerKey != null) {
            providersReference.child(providerKey)
                    .addListenerForSingleValueEvent(new ProviderChangedListener());
        }
    }

    @Touch(R.id.editing_shroud)
    boolean consumeClick() {
        return !editable;
    }

    @Touch(R.id.txt_category)
    boolean pickCategory() {
        if (pickingCategory) return true;
        pickingCategory = true;

        FirebaseDatabase.getInstance().getReference()
                .child("categories")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                pickingCategory = false;

                                final List<String> categories = new ArrayList<>();
                                final List<String> categoryNames = new ArrayList<>();
                                final Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                for (DataSnapshot child : children) {
                                    Category category = child.getValue(Category.class);
                                    if (category != null) {
                                        categories.add(child.getKey());
                                        categoryNames.add(category.getName());
                                    }
                                }
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Pick category")
                                        .setSingleChoiceItems(categoryNames.toArray(new String[]{}), 0,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        provider.setCategory(categories.get(which));
                                                        provider.setSubcategory(null);
                                                        txtCategory.setText(categoryNames.get(which));
                                                        txtSubcategory.setText(null);
                                                    }
                                                })
                                        .setPositiveButton("Pick", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .setNegativeButton("Cancel", null)
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

    @Touch(R.id.txt_subcategory)
    boolean pickSubcategory() {
        if (pickingSubcategory) return true;
        pickingSubcategory = true;

        FirebaseDatabase.getInstance().getReference()
                .child("subcategories")
                .child(provider.getCategory())
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                pickingSubcategory = false;

                                final List<String> subcategories = new ArrayList<>();
                                final List<String> subcategoryNames = new ArrayList<>();
                                final Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                for (DataSnapshot child : children) {
                                    Subcategory subcategory = child.getValue(Subcategory.class);
                                    if (subcategory != null) {
                                        subcategories.add(child.getKey());
                                        subcategoryNames.add(subcategory.getName());
                                    }
                                }
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Pick subcategory")
                                        .setSingleChoiceItems(subcategoryNames.toArray(new String[]{}), 0,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        provider.setSubcategory(subcategories.get(which));
                                                        txtSubcategory.setText(subcategoryNames.get(which));
                                                    }
                                                })
                                        .setPositiveButton("Pick", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .setNegativeButton("Cancel", null)
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
    boolean editLocation() {
        if (locationPickerStarted) return true;

        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
            locationPickerStarted = true;
        } catch (GooglePlayServicesRepairableException e) {
            if (isAdded()) {
                GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), e.getConnectionStatusCode(), GOOGLE_PLAY_SERVICES_REPAIRABLE_REQUEST).show();
            }
        } catch (GooglePlayServicesNotAvailableException e) {
            if (isAdded()) {
                GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), e.errorCode, GOOGLE_PLAY_SERVICES_NOT_AVAILABLE_REQUEST).show();
            }
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationPickerStarted = false;

        switch (requestCode) {
            case PLACE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(data, getContext());
                    String toastMsg = String.format(getString(R.string.msg_provider_location_selected), place.getName());
                    Snackbar.make(getView(), toastMsg, Snackbar.LENGTH_SHORT).show();
                    providerLocation = place.getLatLng();
                    txtLocation.setText(StringUtils.locationToDMS(providerLocation));
                }
                break;
            case GOOGLE_PLAY_SERVICES_REPAIRABLE_REQUEST:
                if (resultCode == RESULT_OK) editLocation();
                break;
            case GOOGLE_PLAY_SERVICES_NOT_AVAILABLE_REQUEST:
                break;
        }
    }

    public void saveProvider() {
        if (provider == null) {
            if (providerKey == null) provider = new Provider();
            else return;
        }

        provider.setName(txtName.getText().toString());
        provider.setCategory(txtCategory.getText().toString());
        provider.setSubcategory(txtSubcategory.getText().toString());
        provider.setAddress(txtAddress.getText().toString());
        provider.setAddress(txtAddress.getText().toString());
        provider.setDescription(txtDescription.getText().toString());
        provider.setPhone(txtPhone.getText().toString());
        provider.setWeb(txtWeb.getText().toString());
        provider.setEmail(txtEmail.getText().toString());
        provider.setHours(txtHours.getText().toString());

        if (providerKey != null) {
            providersReference.updateChildren(provider.toMap());
        } else {
            providerKey = providersReference.push().getKey();
            providersReference.child(providerKey).setValue(provider);
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("geofire/providers")
                .child(provider.getCategory())
                .child(provider.getSubcategory());
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(providerKey, new GeoLocation(providerLocation.latitude, providerLocation.longitude));
    }

    private void bindToProvider(Provider provider) {
        txtName.setText(provider.getName());
        txtAddress.setText(provider.getAddress());
        txtDescription.setText(provider.getDescription());
        txtPhone.setText(provider.getPhone());
        txtWeb.setText(provider.getWeb());
        txtEmail.setText(provider.getEmail());
        txtHours.setText(provider.getHours());

        layoutName.setVisibility(editable ? View.VISIBLE : View.GONE);
        layoutLocation.setVisibility(editable ? View.VISIBLE : View.GONE);
    }

    private class ProviderChangedListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final Provider newProvider = dataSnapshot.getValue(Provider.class);
            if (newProvider == null) return;

            provider = newProvider;
            provider.setKey(dataSnapshot.getKey());
            bindToProvider(provider);

            FirebaseDatabase.getInstance().getReference()
                    .child("categories")
                    .child(provider.getCategory())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Category category = dataSnapshot.getValue(Category.class);
                            if (category != null) {
                                txtCategory.setText(category.getName());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            FirebaseDatabase.getInstance().getReference()
                    .child("subcategories")
                    .child(provider.getCategory())
                    .child(provider.getSubcategory())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Subcategory subcategory = dataSnapshot.getValue(Subcategory.class);
                            if (subcategory != null) {
                                txtSubcategory.setText(subcategory.getName());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance()
                    .getReference("geofire/providers")
                    .child(provider.getCategory())
                    .child(provider.getSubcategory()));
            geoFire.getLocation(providerKey, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation geoLocation) {
                    if (geoLocation != null) {
                        providerLocation = new LatLng(geoLocation.latitude, geoLocation.longitude);
                        txtLocation.setText(StringUtils.locationToDMS(providerLocation));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Snackbar.make(getView(), "Failed to load provider.", Snackbar.LENGTH_LONG).show();
        }
    }
}
