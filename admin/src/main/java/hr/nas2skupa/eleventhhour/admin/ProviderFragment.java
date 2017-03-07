package hr.nas2skupa.eleventhhour.admin;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.model.Provider;
import hr.nas2skupa.eleventhhour.utils.StringUtils;

import static android.app.Activity.RESULT_OK;

/**
 * Created by nas2skupa on 06/03/2017.
 */

@EFragment(R.layout.fragment_provider)
public class ProviderFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {
    private static final int PLACE_PICKER_REQUEST = 12345;

    @FragmentArg String providerKey;
    @FragmentArg Boolean editable;

    @ViewById(R.id.txt_name) EditText txtName;
    @ViewById(R.id.txt_location) EditText txtLocation;
    @ViewById(R.id.txt_address) EditText txtAddress;
    @ViewById(R.id.txt_description) EditText txtDescription;
    @ViewById(R.id.txt_phone) EditText txtPhone;
    @ViewById(R.id.txt_web) EditText txtWeb;
    @ViewById(R.id.txt_email) EditText txtEmail;
    @ViewById(R.id.txt_hours) EditText txtHours;

    @ViewById(R.id.layout_name) TextInputLayout layoutName;
    @ViewById(R.id.layout_location) TextInputLayout layoutLocation;

    private DatabaseReference providerReference;
    private ValueEventListener providerListener;
    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        providerReference = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(providerKey);
        providerListener = new ProviderChangedListener();

        googleApiClient = new GoogleApiClient
                .Builder(getContext())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();

    }

    @Override
    public void onStart() {
        super.onStart();

        providerReference.addValueEventListener(providerListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        providerReference.removeEventListener(providerListener);
    }

    @Touch(R.id.editing_shroud)
    boolean consumeClick() {
        return !editable;
    }

    @Click(R.id.txt_location)
    void editLocation() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getContext());
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(getContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        }
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class ProviderChangedListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final Provider newProvider = dataSnapshot.getValue(Provider.class);
            if (newProvider == null) return;

            Provider provider = newProvider;
            provider.setKey(dataSnapshot.getKey());
            bindToProvider(provider);

            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("geofire/providers")
                    .child(provider.getCategory())
                    .child(provider.getSubcategory());
            GeoFire geoFire = new GeoFire(ref);
            geoFire.getLocation(providerKey, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation geoLocation) {
                    if (geoLocation != null) {
                        Location location = new Location("");
                        location.setLatitude(geoLocation.latitude);
                        location.setLongitude(geoLocation.longitude);
                        txtLocation.setText(StringUtils.locationToDMS(location));
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
