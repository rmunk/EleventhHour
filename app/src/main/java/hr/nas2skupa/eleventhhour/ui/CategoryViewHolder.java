package hr.nas2skupa.eleventhhour.ui;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Category;

/**
 * Created by nas2skupa on 18/09/16.
 */
public class CategoryViewHolder extends RecyclerView.ViewHolder {
    public TextView titleView;
    private ImageView iconView;


    public CategoryViewHolder(View itemView) {
        super(itemView);

        titleView = (TextView) itemView.findViewById(R.id.txt_category_name);
        iconView = (ImageView) itemView.findViewById(R.id.img_category_icon);
    }

    public void bindToSubcategory(final Category category) {
        titleView.setText(category.getLocaleName());
        try {
            iconView.setBackgroundColor(Color.parseColor(category.getColor()));
        } catch (Exception ignored) {

        }
        Picasso.with(iconView.getContext()).load(category.getIcon())
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(iconView, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(iconView.getContext())
                                .load(category.getIcon())
                                .into(iconView);
                    }
                });
    }
}
