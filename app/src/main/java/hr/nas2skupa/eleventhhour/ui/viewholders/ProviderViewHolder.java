package hr.nas2skupa.eleventhhour.ui.viewholders;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.events.FavoriteStatusChangedEvent;
import hr.nas2skupa.eleventhhour.events.ProviderSelectedEvent;
import hr.nas2skupa.eleventhhour.events.UserRatingChangedEvent;
import hr.nas2skupa.eleventhhour.model.Provider;
import hr.nas2skupa.eleventhhour.utils.Utils;

/**
 * Created by nas2skupa on 18/09/16.
 */
public class ProviderViewHolder extends RecyclerView.ViewHolder {
    ViewGroup layoutMain;
    ViewGroup layoutList;

    TextView txtProviderName;
    ImageView imgFavorite;
    ImageView imgSale;
    RatingBar ratingIndicator;
    TextView txtRatings;
    TextView txtDistance;
    ImageView imgExpand;

    View separator1;
    ViewGroup viewDetails;
    TextView txtDescription;
    TextView txtPhone;
    TextView txtAddress;
    TextView txtWeb;
    TextView txtEmail;
    TextView txtHours;

    View separator2;
    ViewGroup viewAction;
    ImageView btnFavourite;
    ImageView btnSchedule;
    RatingBar ratingBar;

    private Provider provider;
    private boolean detailsVisible = false;


    public ProviderViewHolder(final View itemView) {
        super(itemView);

        layoutMain = (ViewGroup) itemView.findViewById(R.id.layout_main);
        layoutList = (ViewGroup) itemView.findViewById(R.id.layout_list);

        txtProviderName = (TextView) itemView.findViewById(R.id.txt_provider_name);
        imgFavorite = (ImageView) itemView.findViewById(R.id.img_favorite);
        imgSale = (ImageView) itemView.findViewById(R.id.img_sale);
        ratingIndicator = (RatingBar) itemView.findViewById(R.id.rating_indicator);
        txtRatings = (TextView) itemView.findViewById(R.id.txt_ratings);
        txtDistance = (TextView) itemView.findViewById(R.id.txt_distance);
        imgExpand = (ImageView) itemView.findViewById(R.id.img_expand);

        separator1 = itemView.findViewById(R.id.separator1);
        viewDetails = (ViewGroup) itemView.findViewById(R.id.provider_details);
        txtDescription = (TextView) itemView.findViewById(R.id.txt_description);
        txtPhone = (TextView) itemView.findViewById(R.id.txt_phone);
        txtAddress = (TextView) itemView.findViewById(R.id.txt_address);
        txtWeb = (TextView) itemView.findViewById(R.id.txt_web);
        txtEmail = (TextView) itemView.findViewById(R.id.txt_email);
        txtHours = (TextView) itemView.findViewById(R.id.txt_hours);

        separator2 = itemView.findViewById(R.id.separator2);
        viewAction = (ViewGroup) itemView.findViewById(R.id.provider_action);
        btnFavourite = (ImageView) itemView.findViewById(R.id.btn_favourite);
        btnSchedule = (ImageView) itemView.findViewById(R.id.btn_schedule);
        ratingBar = (RatingBar) itemView.findViewById(R.id.rating_bar);

        ratingBar.setOnRatingBarChangeListener(new UserRatingChangedListener());
        btnFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                provider.setFavorite(!provider.isFavorite());
                FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(Utils.getMyUid())
                        .child("favorites")
                        .child(provider.getKey())
                        .setValue(provider.isFavorite() ? true : null);
            }
        });
        btnSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ProviderSelectedEvent(itemView, provider.getKey()));
            }
        });
    }

    public void bindToProvider(Provider provider) {
        this.provider = provider;

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
        txtWeb.setText(provider.getWeb());
        txtWeb.setVisibility(txtWeb.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtEmail.setText(provider.getEmail());
        txtEmail.setVisibility(txtEmail.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtHours.setText(provider.getHours());
        txtHours.setVisibility(txtHours.getText().length() > 0 ? View.VISIBLE : View.GONE);

        btnFavourite.setImageResource(provider.isFavorite() ? R.drawable.ic_heart_broken_black_24dp : R.drawable.ic_favorite_black_36dp);
        ratingBar.setRating(provider.getUserRating());
    }

    public void showDetails(boolean show) {
        if (detailsVisible == show) return;
        detailsVisible = show;

        separator1.setVisibility(show ? View.VISIBLE : View.GONE);
        viewDetails.setVisibility(show ? View.VISIBLE : View.GONE);

        ObjectAnimator anim = show
                ? ObjectAnimator.ofFloat(imgExpand, "rotation", 0, 180)
                : ObjectAnimator.ofFloat(imgExpand, "rotation", 180, 0);
        anim.setDuration(500);
        anim.start();
    }

    public void showActionBar(boolean show) {
        separator2.setVisibility(show ? View.VISIBLE : View.GONE);
        viewAction.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Subscribe
    public void updateFavorite(FavoriteStatusChangedEvent event) {
        if (!event.getProviderKey().equals(provider.getKey())) return;

        provider.setFavorite(event.isFavorite());
        imgFavorite.setVisibility(provider.isFavorite() ? View.VISIBLE : View.GONE);
        btnFavourite.setImageResource(provider.isFavorite() ? R.drawable.ic_heart_broken_black_24dp : R.drawable.ic_favorite_black_36dp);
    }

    @Subscribe
    public void updateUserRating(UserRatingChangedEvent event) {
        if (!event.getProviderKey().equals(provider.getKey())) return;

        provider.setUserRating(event.getUserRating());
        ratingBar.setRating(event.getUserRating());
    }

    private class UserRatingChangedListener implements RatingBar.OnRatingBarChangeListener {

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
                    .child(provider.getKey());
            providers.updateChildren(ratingUpdate);

            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(Utils.getMyUid())
                    .child("ratings")
                    .child(provider.getKey())
                    .setValue(newUserRating > 0 ? newUserRating : null);
        }
    }
}
