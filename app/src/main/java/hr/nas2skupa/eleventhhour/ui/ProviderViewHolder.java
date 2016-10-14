package hr.nas2skupa.eleventhhour.ui;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
    @BindView(R.id.layout_main) ViewGroup LayoutMain;

    @BindView(R.id.txt_provider_name) TextView txtProviderName;
    @BindView(R.id.img_favorite) ImageView imgFavorite;
    @BindView(R.id.img_sale) ImageView imgSale;
    @BindView(R.id.rating_indicator) RatingBar ratingIndicator;
    @BindView(R.id.txt_distance) TextView txtDistance;
    @BindView(R.id.img_expand) ImageView imgExpand;

    @BindView(R.id.separator1) View separator1;
    @BindView(R.id.provider_details) ViewGroup viewDetails;
    @BindView(R.id.txt_description) TextView txtDescription;
    @BindView(R.id.txt_phone) TextView txtPhone;
    @BindView(R.id.txt_address) TextView txtAddress;
    @BindView(R.id.txt_web) TextView txtWeb;
    @BindView(R.id.txt_email) TextView txtEmail;
    @BindView(R.id.txt_hours) TextView txtHours;

    @BindView(R.id.separator2) View separator2;
    @BindView(R.id.provider_action) ViewGroup viewAction;
    @BindView(R.id.btn_favourite) ImageView btnFavourite;
    @BindView(R.id.rating_bar) RatingBar ratingBar;

    private Provider provider;


    public ProviderViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);

        ratingBar.setOnRatingBarChangeListener(new UserRatingChangedListener());
    }

    public void bindToProvider(Provider provider) {
        this.provider = provider;

        txtProviderName.setText(provider.getName());
        imgSale.setVisibility(provider.isSale() ? View.VISIBLE : View.GONE);
        imgFavorite.setVisibility(provider.isFavorite() ? View.VISIBLE : View.GONE);
        ratingIndicator.setRating(provider.getRating());

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
        separator1.setVisibility(show ? View.VISIBLE : View.GONE);
        viewDetails.setVisibility(show ? View.VISIBLE : View.GONE);
        separator2.setVisibility(show ? View.VISIBLE : View.GONE);
        viewAction.setVisibility(show ? View.VISIBLE : View.GONE);
        ObjectAnimator anim = show
                ? ObjectAnimator.ofFloat(imgExpand, "rotation", 0, 180)
                : ObjectAnimator.ofFloat(imgExpand, "rotation", 180, 0);
        anim.setDuration(500);
        anim.start();
    }

    @OnClick(R.id.btn_favourite)
    public void toggleToFavorite() {
        provider.setFavorite(!provider.isFavorite());
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(Utils.getMyUid())
                .child("favorites")
                .child(provider.getKey())
                .setValue(provider.isFavorite() ? true : null);
    }

    @OnClick(R.id.btn_schedule)
    public void scheduleService() {
        EventBus.getDefault().post(new ProviderSelectedEvent(itemView, provider.getKey()));
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
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            provider.setUserRating(rating);
            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(Utils.getMyUid())
                    .child("ratings")
                    .child(provider.getKey())
                    .setValue(rating > 0 ? rating : null);
        }
    }
}
