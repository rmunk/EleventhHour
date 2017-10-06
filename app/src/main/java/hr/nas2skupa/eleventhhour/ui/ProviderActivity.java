package hr.nas2skupa.eleventhhour.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Locale;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.Preferences_;
import hr.nas2skupa.eleventhhour.common.model.Provider;
import hr.nas2skupa.eleventhhour.common.utils.Utils;

@EActivity(R.layout.activity_provider)
@OptionsMenu(R.menu.main)
public class ProviderActivity extends DrawerActivity implements RatingBar.OnRatingBarChangeListener, OnMapReadyCallback {
    private static final int REQUEST_PHONE_PERMISSION = 1;
    @Pref Preferences_ preferences;

    @Extra String providerKey;

    @ViewById ViewGroup layoutMain;
    @ViewById AppBarLayout appBar;

    @ViewById ImageView btnFavorite;
    @ViewById RatingBar ratingBar;

    @ViewById TextView txtProviderName;
    @ViewById ImageView imgFavorite;
    @ViewById ImageView imgSale;
    @ViewById RatingBar ratingIndicator;
    @ViewById TextView txtRatings;
    @ViewById TextView txtDistance;
    @ViewById ImageView imgExpand;

    @ViewById ViewGroup providerMore;
    @ViewById TextView txtDescription;
    @ViewById TextView txtPhone;
    @ViewById TextView txtAddress;
    @ViewById TextView txtWeb;
    @ViewById TextView txtEmail;
    @ViewById TextView txtHours;

    private DatabaseReference providerReference;
    private ValueEventListener providerListener;
    private DatabaseReference favoriteReference;
    private ValueEventListener favoriteListener;
    private DatabaseReference ratingReference;
    private ValueEventListener ratingListener;
    private boolean showDetails = false;
    private Provider provider;
    private float userRating;

    private GoogleMap map;

    @SuppressWarnings("ConstantConditions")
    void setToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getDrawer().setStatusBarBackgroundColor(Color.LTGRAY);
    }

    @AfterViews
    void initViews() {
        ratingBar.setOnRatingBarChangeListener(this);
    }

    @AfterViews
    void setProviderView() {
        imgExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Transition transition = new Slide(Gravity.TOP);
                    TransitionManager.beginDelayedTransition(layoutMain, transition);
                }

                showDetails = !showDetails;
                providerMore.setVisibility(showDetails ? View.VISIBLE : View.GONE);

                ObjectAnimator anim = showDetails
                        ? ObjectAnimator.ofFloat(imgExpand, "rotation", 0, 180)
                        : ObjectAnimator.ofFloat(imgExpand, "rotation", 180, 0);
                anim.setDuration(500);
                anim.start();
            }
        });

        txtPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(ProviderActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ProviderActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_PERMISSION);
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + provider.phone));
                if (intent.resolveActivity(getPackageManager()) != null) {
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
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        txtEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + provider.email));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(intent, "Email"));
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void bindToProvider(Provider provider) {
        txtProviderName.setText(provider.name);
        imgSale.setVisibility(provider.hasSale ? View.VISIBLE : View.GONE);
        imgFavorite.setVisibility(provider.favorite ? View.VISIBLE : View.GONE);
        ratingIndicator.setRating(provider.rating);
        txtRatings.setText(String.valueOf(provider.ratings));

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        providerReference = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("data")
                .child(providerKey);
        providerListener = new ProviderChangedListener();

        favoriteReference = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("userFavorites")
                .child(Utils.getMyUid())
                .child(providerKey);
        favoriteListener = new FavoriteChangedListener();

        ratingReference = FirebaseDatabase.getInstance().getReference()
                .child("userRatingsOfProviders")
                .child(Utils.getMyUid())
                .child(providerKey);
        ratingListener = new RatingChangedListener();

        if (savedInstanceState == null) {
            ServicesFragment fragment = ServicesFragment_.builder()
                    .providerKey(providerKey)
                    .build();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, "ServicesFragment")
                    .commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PHONE_PERMISSION:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + provider.phone));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        providerReference.addValueEventListener(providerListener);
        favoriteReference.addValueEventListener(favoriteListener);
        ratingReference.addValueEventListener(ratingListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        providerReference.removeEventListener(providerListener);
        favoriteReference.removeEventListener(favoriteListener);
        ratingReference.removeEventListener(ratingListener);
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        onBackPressed();
    }

    @Click(R.id.provider_more)
    public void consumeClick() {
    }

    @Click(R.id.btn_favorite)
    public void toggleFavorite(ImageView imageView) {
        provider.favorite = !provider.favorite;
        imageView.setImageResource(provider.favorite ? R.drawable.ic_heart_broken_black_24dp : R.drawable.ic_favorite_black_36dp);
        FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("userFavorites")
                .child(Utils.getMyUid())
                .child(providerKey)
                .setValue(provider.favorite ? true : null);
    }

    @Override
    public void onRatingChanged(final RatingBar ratingBar, final float newUserRating, boolean fromUser) {
        if (!fromUser) return;

        ratingBar.setEnabled(false);
        DatabaseReference providers = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("data")
                .child(providerKey);
        providers.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Provider provider = mutableData.getValue(Provider.class);

                if (provider == null) {
                    return Transaction.success(mutableData);
                }

                int oldRatingsCnt = provider.ratings;
                float oldRating = oldRatingsCnt > 0 ? provider.rating : 0;
                float oldUserRating = oldRatingsCnt > 0 ? userRating : 0;

                boolean alreadyRated = oldUserRating > 0;
                int newRatingsCnt = !alreadyRated ? oldRatingsCnt + 1
                        : newUserRating > 0 ? oldRatingsCnt
                        : oldRatingsCnt - 1;
                float newRating = newRatingsCnt > 0
                        ? (oldRatingsCnt * oldRating - oldUserRating + newUserRating) / newRatingsCnt
                        : 0;

                provider.rating = newRating;
                provider.ratings = newRatingsCnt;

                mutableData.setValue(provider);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                userRating = newUserRating;
                FirebaseDatabase.getInstance().getReference()
                        .child("userRatingsOfProviders")
                        .child(Utils.getMyUid())
                        .child(providerKey)
                        .setValue(newUserRating > 0 ? newUserRating : null);
                ratingBar.setEnabled(true);
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setBuildingsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            map.setMyLocationEnabled(true);
    }

    private class ProviderChangedListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Provider newProvider = dataSnapshot.getValue(Provider.class);
            if (newProvider == null) {
                Toast.makeText(ProviderActivity.this,
                        String.format(Locale.getDefault(), getString(R.string.provider_removed), provider.name),
                        Toast.LENGTH_SHORT).show();
                onBackPressed();
                return;
            }

            newProvider.favorite = provider != null && provider.favorite;
            provider = newProvider;
            provider.key = dataSnapshot.getKey();
            bindToProvider(provider);

            if (map != null && provider.location != null) {
                LatLng target = provider.location.toLatLng();
                map.addMarker(new MarkerOptions()
                        .position(target)
                        .title(provider.name));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 16));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Snackbar.make(layoutMain, "Failed to load provider.", Snackbar.LENGTH_LONG).show();
        }
    }

    private class FavoriteChangedListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Boolean value = dataSnapshot.getValue(Boolean.class);
            provider.favorite = value != null ? value : false;
            bindToProvider(provider);
            btnFavorite.setImageResource(provider.favorite ? R.drawable.ic_heart_broken_black_24dp : R.drawable.ic_favorite_black_36dp);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Snackbar.make(layoutMain, "Failed to load provider.", Snackbar.LENGTH_LONG).show();
        }
    }

    private class RatingChangedListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Float value = dataSnapshot.getValue(Float.class);
            userRating = provider.ratings > 0 && value != null ? value : 0;
            ratingBar.setRating(userRating);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Snackbar.make(layoutMain, "Failed to load provider.", Snackbar.LENGTH_LONG).show();
        }
    }
}
