package hr.nas2skupa.eleventhhour.ui.viewholders;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.model.Provider;

/**
 * Created by nas2skupa on 18/09/16.
 */
public class ProviderViewHolder extends RecyclerView.ViewHolder {
    private TextView txtProviderName;
    private ImageView imgFavorite;
    private ImageView imgSale;
    private RatingBar ratingIndicator;
    private TextView txtRatings;
    private TextView txtDistance;
    public ImageView imgExpand;

    private View separator1;
    private ViewGroup viewDetails;

    public TextView txtDescription;
    public TextView txtPhone;
    public TextView txtAddress;
    public TextView txtWeb;
    public TextView txtEmail;
    public TextView txtHours;

    private boolean detailsVisible = false;


    public ProviderViewHolder(final View itemView) {
        super(itemView);

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
        txtHours.setText(provider.hours);
        txtHours.setVisibility(txtHours.getText().length() > 0 ? View.VISIBLE : View.GONE);

        viewDetails.setVisibility(detailsVisible ? View.VISIBLE : View.GONE);
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
