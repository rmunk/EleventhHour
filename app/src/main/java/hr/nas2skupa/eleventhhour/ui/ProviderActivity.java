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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
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
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.Locale;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Provider;
import hr.nas2skupa.eleventhhour.ui.helpers.DrawerActivity;
import hr.nas2skupa.eleventhhour.utils.Utils;

@EActivity(R.layout.activity_provider)
@OptionsMenu(R.menu.main)
public class ProviderActivity extends DrawerActivity implements RatingBar.OnRatingBarChangeListener, OnMapReadyCallback {
    private static final int REQUEST_PHONE_PERMISSION = 1;

    @Extra
    String providerKey;

    @ViewById(R.id.layout_main)
    ViewGroup layoutMain;
    @ViewById(R.id.app_bar)
    AppBarLayout appBar;

    @ViewById(R.id.btn_favorite)
    ImageView btnFavorite;
    @ViewById(R.id.rating_bar)
    RatingBar ratingBar;

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
    @ViewById(R.id.img_expand)
    ImageView imgExpand;

    @ViewById(R.id.provider_more)
    ViewGroup viewDetails;
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
    private DatabaseReference ratingReference;
    private ValueEventListener ratingListener;
    private boolean showDetails = false;
    private Provider provider;

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
                viewDetails.setVisibility(showDetails ? View.VISIBLE : View.GONE);

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
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + provider.getPhone()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        txtWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = provider.getWeb();
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
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + provider.getEmail()));
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
        txtProviderName.setText(provider.getName());
        imgSale.setVisibility(provider.isSale() ? View.VISIBLE : View.GONE);
        imgFavorite.setVisibility(provider.isFavorite() ? View.VISIBLE : View.GONE);
        ratingIndicator.setRating(provider.getRating());
        txtRatings.setText(String.valueOf(provider.getRatingsCnt()));

        txtDescription.setText(provider.getDescription());
        txtDescription.setVisibility(txtDescription.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtPhone.setText(provider.getPhone());
        txtPhone.setVisibility(txtPhone.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtAddress.setText(provider.getAddress());
        txtAddress.setVisibility(txtAddress.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtAddress.setSelected(true);
        txtWeb.setText(provider.getWeb());
        txtWeb.setVisibility(txtWeb.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtEmail.setText(provider.getEmail());
        txtEmail.setVisibility(txtEmail.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtHours.setText(provider.getHours());
        txtHours.setVisibility(txtHours.getText().length() > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        providerReference = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(providerKey);
        providerListener = new ProviderChangedListener();

        favoriteReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(Utils.getMyUid())
                .child("favorites")
                .child(providerKey);
        favoriteListener = new FavoriteChangedListener();

        ratingReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(Utils.getMyUid())
                .child("ratings")
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
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + provider.getPhone()));
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
        provider.setFavorite(!provider.isFavorite());
        imageView.setImageResource(provider.isFavorite() ? R.drawable.ic_heart_broken_black_24dp : R.drawable.ic_favorite_black_36dp);
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(Utils.getMyUid())
                .child("favorites")
                .child(providerKey)
                .setValue(provider.isFavorite() ? true : null);
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float newUserRating, boolean fromUser) {
        if (!fromUser) return;

        HashMap<String, Object> ratingUpdate = new HashMap<>();
        float oldUserRating = provider.getUserRating();
        int oldRatingsCnt = provider.getRatingsCnt();

        boolean alreadyRated = oldUserRating > 0;
        int newRatingsCnt = !alreadyRated ? oldRatingsCnt + 1
                : newUserRating > 0 ? oldRatingsCnt
                : oldRatingsCnt - 1;
        float newRating = (oldRatingsCnt * provider.getRating() - oldUserRating + newUserRating) / newRatingsCnt;
        ratingUpdate.put("rating", newRating);
        ratingUpdate.put("ratingsCnt", newRatingsCnt);
        ratingUpdate.put(".priority", 5 - newRating);
        DatabaseReference providers = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(providerKey);
        providers.updateChildren(ratingUpdate);

        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(Utils.getMyUid())
                .child("ratings")
                .child(providerKey)
                .setValue(newUserRating > 0 ? newUserRating : null);
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
                        String.format(Locale.getDefault(), getString(R.string.provider_removed), provider.getName()),
                        Toast.LENGTH_SHORT).show();
                onBackPressed();
                return;
            }

            newProvider.setFavorite(provider != null && provider.isFavorite());
            provider = newProvider;
            provider.setKey(dataSnapshot.getKey());
            bindToProvider(provider);


            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("geofire/providers")
                    .child(provider.getCategory())
                    .child(provider.getSubcategory());
            GeoFire geoFire = new GeoFire(ref);
            geoFire.getLocation(providerKey, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    if (map != null && location != null) {
                        LatLng target = new LatLng(location.latitude, location.longitude);
                        map.addMarker(new MarkerOptions()
                                .position(target)
                                .title(provider.getName()));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 16));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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
            provider.setFavorite(value != null);
            bindToProvider(provider);
            btnFavorite.setImageResource(provider.isFavorite() ? R.drawable.ic_heart_broken_black_24dp : R.drawable.ic_favorite_black_36dp);
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
            float userRating = value != null ? value : 0;
            provider.setUserRating(userRating);
            ratingBar.setRating(userRating);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Snackbar.make(layoutMain, "Failed to load provider.", Snackbar.LENGTH_LONG).show();
        }
    }
}
