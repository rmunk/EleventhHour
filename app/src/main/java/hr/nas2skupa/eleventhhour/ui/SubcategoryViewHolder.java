package hr.nas2skupa.eleventhhour.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Subcategory;

/**
 * Created by nas2skupa on 18/09/16.
 */
public class SubcategoryViewHolder extends RecyclerView.ViewHolder {
    private TextView titleView;


    public SubcategoryViewHolder(View itemView) {
        super(itemView);

        titleView = (TextView) itemView.findViewById(R.id.txt_subcategory_name);
    }

    public void bindToSubcategory(Subcategory subcategory) {
        titleView.setText(subcategory.getName());
    }
}
