package hr.nas2skupa.eleventhhour.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hr.nas2skupa.eleventhhour.R;
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
    @BindView(R.id.rating_bar) RatingBar ratingBar;
    @BindView(R.id.txt_distance) TextView txtDistance;

    @BindView(R.id.provider_details) ViewGroup details;
    @BindView(R.id.txt_description) TextView txtDescription;
    @BindView(R.id.txt_phone) TextView txtPhone;
    @BindView(R.id.txt_address) TextView txtAddress;
    @BindView(R.id.txt_web) TextView txtWeb;
    @BindView(R.id.txt_email) TextView txtEmail;
    @BindView(R.id.txt_hours) TextView txtHours;

    @BindView(R.id.btn_favourite) ImageView btnFavourite;

    private String categoryKey;
    private String subcategoryKey;
    private String providerKey;
    private Provider provider;


    public ProviderViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }

    public void bindToProvider(String categoryKey, String subcategoryKey, String providerKey, Provider provider) {
        this.categoryKey = categoryKey;
        this.subcategoryKey = subcategoryKey;
        this.providerKey = providerKey;
        this.provider = provider;

        txtProviderName.setText(provider.getName());
        imgSale.setVisibility(provider.hasSale() ? View.VISIBLE : View.GONE);
        imgFavorite.setVisibility(provider.isFavorite() ? View.VISIBLE : View.GONE);
        ratingBar.setRating(provider.getRating());

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
    }

    public void setFavorite(boolean favorite) {
        imgFavorite.setVisibility(favorite ? View.VISIBLE : View.GONE);
        btnFavourite.setImageResource(favorite ? R.drawable.ic_heart_broken_black_24dp : R.drawable.ic_favorite_black_36dp);
    }

    public void showDetails(boolean show) {
        details.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.btn_rate)
    public void rateProvider() {

    }

    @OnClick(R.id.btn_favourite)
    public void toggleToFavorite() {
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(Utils.getMyUid())
                .child("favorites")
                .child(providerKey)
                .setValue(!provider.isFavorite());
    }

    @OnClick(R.id.btn_schedule)
    public void scheduleService() {

    }
}
