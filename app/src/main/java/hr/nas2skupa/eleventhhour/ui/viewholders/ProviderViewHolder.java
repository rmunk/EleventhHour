package hr.nas2skupa.eleventhhour.ui.viewholders;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Provider;

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
        txtWeb.setText(provider.getWeb());
        txtWeb.setVisibility(txtWeb.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtEmail.setText(provider.getEmail());
        txtEmail.setVisibility(txtEmail.getText().length() > 0 ? View.VISIBLE : View.GONE);
        txtHours.setText(provider.getHours());
        txtHours.setVisibility(txtHours.getText().length() > 0 ? View.VISIBLE : View.GONE);
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
}
