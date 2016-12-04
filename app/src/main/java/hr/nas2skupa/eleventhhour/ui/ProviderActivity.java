package hr.nas2skupa.eleventhhour.ui;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

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

    private ProviderViewHolder providerViewHolder;
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
        providerViewHolder = new ProviderViewHolder(providerInfo);
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

    @Click(R.id.item_provider)
    public void showProviderDetails(CardView providerCard) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Transition transition = new AutoTransition();
            transition.setDuration(200);
            TransitionManager.beginDelayedTransition(layoutMain, transition);
        }

        showDetails = !showDetails;
        providerViewHolder.showDetails(showDetails);
        providerAction.setVisibility(showDetails ? View.VISIBLE : View.GONE);
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
            providerViewHolder.bindToProvider(provider);
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
            providerViewHolder.bindToProvider(provider);
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
