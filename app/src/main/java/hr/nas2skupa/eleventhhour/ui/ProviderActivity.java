package hr.nas2skupa.eleventhhour.ui;

import android.Manifest;
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
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
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
import hr.nas2skupa.eleventhhour.ui.viewholders.ProviderViewHolder;
import hr.nas2skupa.eleventhhour.utils.Utils;

@EActivity(R.layout.activity_provider)
@OptionsMenu(R.menu.main)
public class ProviderActivity extends DrawerActivity implements RatingBar.OnRatingBarChangeListener {
    private static final int REQUEST_PHONE_PERMISSION = 1;

    @Extra
    String providerKey;

    @ViewById(R.id.layout_main)
    ViewGroup layoutMain;
    @ViewById(R.id.app_bar)
    AppBarLayout appBar;

    @ViewById(R.id.provider_action)
    ViewGroup providerAction;
    @ViewById(R.id.btn_favorite)
    ImageView btnFavorite;
    @ViewById(R.id.rating_bar)
    RatingBar ratingBar;

    @AfterViews
    void initViews() {
        ratingBar.setOnRatingBarChangeListener(this);
    }

    private ProviderViewHolder viewHolder;
    private DatabaseReference providerReference;
    private ValueEventListener providerListener;
    private DatabaseReference favoriteReference;
    private ValueEventListener favoriteListener;
    private DatabaseReference ratingReference;
    private ValueEventListener ratingListener;
    private boolean showDetails = false;
    private Provider provider;

    @SuppressWarnings("ConstantConditions")
    void setToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getDrawer().setStatusBarBackgroundColor(Color.LTGRAY);
    }

    void setProviderView(@ViewById(R.id.item_provider) View providerInfo) {
        viewHolder = new ProviderViewHolder(providerInfo);

        viewHolder.imgExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Transition transition = new AutoTransition();
                    transition.setDuration(200);
                    TransitionManager.beginDelayedTransition(layoutMain, transition);
                }

                showDetails = !showDetails;
                viewHolder.showDetails(showDetails);
                providerAction.setVisibility(showDetails ? View.VISIBLE : View.GONE);
            }
        });

        viewHolder.txtPhone.setOnClickListener(new View.OnClickListener() {
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
        viewHolder.txtWeb.setOnClickListener(new View.OnClickListener() {
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
        viewHolder.txtEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + provider.getEmail()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(intent, "Email"));
                }
            }
        });
        viewHolder.txtAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("geofire/providers")
                        .child(provider.getCategory())
                        .child(provider.getSubcategory());
                GeoFire geoFire = new GeoFire(ref);
                geoFire.getLocation(providerKey, new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        String uriString;
                        if (location != null) {
                            uriString = String.format(Locale.getDefault(), "geo:%f,%f?q=%f,%f(%s)",
                                    location.latitude,
                                    location.longitude,
                                    location.latitude,
                                    location.longitude,
                                    Uri.encode(provider.getName()));
                        } else {
                            uriString = String.format(Locale.getDefault(), "geo:%f,%f?q=%s",
                                    0,
                                    0,
                                    Uri.encode(provider.getAddress()));
                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
                        intent.setPackage("com.google.android.apps.maps");
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
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
            viewHolder.bindToProvider(provider);
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
            viewHolder.bindToProvider(provider);
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
