package hr.nas2skupa.eleventhhour.ui;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Locale;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.Preferences_;
import hr.nas2skupa.eleventhhour.common.model.Provider;
import hr.nas2skupa.eleventhhour.common.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_provider_info)
public class ProviderInfoFragment extends Fragment {
    private static final int REQUEST_PHONE_PERMISSION = 1;

    @Pref Preferences_ preferences;

    @FragmentArg
    String providerKey;
    @FragmentArg
    String distance;

    @ViewById(R.id.layout_main)
    ViewGroup layoutMain;
    @ViewById(R.id.layout_list)
    ViewGroup layoutList;

    @ViewById(R.id.txt_provider_name)
    TextView txtProviderName;
    @ViewById(R.id.img_favorite)
    ImageView imgFavorite;
    @ViewById(R.id.img_sale)
    ImageView imgSale;
    @ViewById(R.id.rating_indicator)
    RatingBar ratingIndicator;
    @ViewById(R.id.txt_ratings)
    TextView txtRatings;
    @ViewById(R.id.txt_distance)
    TextView txtDistance;

    @ViewById(R.id.provider_details)
    ViewGroup providerDetails;
    @ViewById(R.id.txt_description)
    TextView txtDescription;
    @ViewById(R.id.txt_phone)
    TextView txtPhone;
    @ViewById(R.id.txt_address)
    TextView txtAddress;
    @ViewById(R.id.txt_web)
    TextView txtWeb;
    @ViewById(R.id.txt_email)
    TextView txtEmail;
    @ViewById(R.id.txt_hours)
    TextView txtHours;

    private DatabaseReference providerReference;
    private ValueEventListener providerListener;
    private DatabaseReference favoriteReference;
    private ValueEventListener favoriteListener;
    private Provider provider;

    public ProviderInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        providerReference = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("data")
                .child(providerKey);

        favoriteReference = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("userFavorites")
                .child(Utils.getMyUid())
                .child(providerKey);
    }

    @Override
    public void onStart() {
        super.onStart();

        providerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                provider = dataSnapshot.getValue(Provider.class);
                if (provider == null) return;

                provider.key = dataSnapshot.getKey();
                updateView(provider);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        providerReference.addValueEventListener(providerListener);

        favoriteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean favorite = dataSnapshot.getValue(Boolean.class);
                imgFavorite.setVisibility(favorite != null && favorite ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        favoriteReference.addValueEventListener(favoriteListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (providerListener != null) providerReference.removeEventListener(providerListener);
        if (favoriteListener != null) favoriteReference.removeEventListener(favoriteListener);
    }

    @AfterViews
    public void afterViews() {
        txtPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_PERMISSION);
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + provider.phone));
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        txtWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = provider.web;
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        txtEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + provider.email));
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(Intent.createChooser(intent, "Email"));
                }
            }
        });
        txtAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uriString;
                if (provider.location != null) {
                    uriString = String.format(Locale.getDefault(), "geo:%f,%f?q=%f,%f(%s)",
                            provider.location.latitude,
                            provider.location.longitude,
                            provider.location.latitude,
                            provider.location.longitude,
                            Uri.encode(provider.name));
                } else {
                    uriString = String.format(Locale.getDefault(), "geo:%f,%f?q=%s",
                            0f,
                            0f,
                            Uri.encode(provider.address));
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
                intent.setPackage("com.google.android.apps.maps");
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PHONE_PERMISSION:
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + provider.phone));
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
                break;
        }
    }

    private void updateView(Provider provider) {
        txtProviderName.setText(provider.name);
        imgSale.setVisibility(provider.hasSale ? View.VISIBLE : View.GONE);
        imgFavorite.setVisibility(provider.favorite ? View.VISIBLE : View.GONE);
        ratingIndicator.setRating(provider.rating);
        txtRatings.setText(String.valueOf(provider.ratings));
        txtDistance.setText(distance);

        txtDescription.setText(provider.description);
        txtDescription.setVisibility(txtDescription.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtPhone.setText(provider.phone);
        txtPhone.setVisibility(txtPhone.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtAddress.setText(provider.address);
        txtAddress.setVisibility(txtAddress.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtAddress.setSelected(true);
        txtWeb.setText(provider.web);
        txtWeb.setVisibility(txtWeb.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtEmail.setText(provider.email);
        txtEmail.setVisibility(txtEmail.getText().length() > 0 ? View.VISIBLE : View.GONE);
        if (provider.hours != null) txtHours.setText(provider.hours.today());
        txtHours.setVisibility(txtHours.getText().length() > 0 ? View.VISIBLE : View.GONE);
    }
}
