package hr.nas2skupa.eleventhhour.ui;


import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Provider;
import hr.nas2skupa.eleventhhour.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_provider_info)
public class ProviderInfoFragment extends Fragment {
    @FragmentArg
    String categoryKey;
    @FragmentArg
    String subcategoryKey;
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

    @ViewById(R.id.separator1)
    View separator1;
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

    @ViewById(R.id.separator2)
    View separator2;
    @ViewById(R.id.rating_bar)
    RatingBar ratingBar;

    private boolean showDetails = false;

    private DatabaseReference providerReference;
    private ValueEventListener providerListener;
    private DatabaseReference favoriteReference;
    private ValueEventListener favoriteListener;

    public ProviderInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        providerReference = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(categoryKey)
                .child(subcategoryKey)
                .child(providerKey);

        favoriteReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(Utils.getMyUid())
                .child("favorites")
                .child(providerKey);
    }

    @Override
    public void onStart() {
        super.onStart();

        providerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Provider provider = dataSnapshot.getValue(Provider.class);
                if (provider == null) return;

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
                if (favorite == null) return;

                imgFavorite.setVisibility(favorite ? View.VISIBLE : View.GONE);
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

    private void updateView(Provider provider) {
        txtProviderName.setText(provider.getName());
        imgSale.setVisibility(provider.isSale() ? View.VISIBLE : View.GONE);
        imgFavorite.setVisibility(provider.isFavorite() ? View.VISIBLE : View.GONE);
        ratingIndicator.setRating(provider.getRating());
        txtRatings.setText(String.valueOf(provider.getRatingsCnt()));
        txtDistance.setText(distance);

        txtDescription.setText(provider.getDescription());
        txtDescription.setVisibility(txtDescription.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtPhone.setText(provider.getPhone());
        txtPhone.setVisibility(txtPhone.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtAddress.setText(provider.getAddress());
        txtAddress.setVisibility(txtAddress.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtWeb.setText(provider.getWeb());
        txtWeb.setVisibility(txtWeb.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtEmail.setText(provider.getEmail());
        txtEmail.setVisibility(txtEmail.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtHours.setText(provider.getHours());
        txtHours.setVisibility(txtHours.getText().length() > 0 ? View.VISIBLE : View.GONE);

        ratingBar.setRating(provider.getUserRating());
    }

    @Click(R.id.img_expand)
    public void toggleDetails(ImageView imgExpand) {
        showDetails = !showDetails;

        separator1.setVisibility(showDetails ? View.VISIBLE : View.GONE);
        providerDetails.setVisibility(showDetails ? View.VISIBLE : View.GONE);

        ObjectAnimator anim = showDetails
                ? ObjectAnimator.ofFloat(imgExpand, "rotation", 0, 180)
                : ObjectAnimator.ofFloat(imgExpand, "rotation", 180, 0);
        anim.setDuration(500);
        anim.start();

    }
}
