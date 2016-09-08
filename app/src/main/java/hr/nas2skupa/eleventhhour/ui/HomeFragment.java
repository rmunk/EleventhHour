package hr.nas2skupa.eleventhhour.ui;


import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.View;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import hr.nas2skupa.eleventhhour.R;


/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_home)
public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Click
    void cardHealthClicked(View card) {
        startCategoryActivity(card, R.string.category_health, R.drawable.category_health, R.color.category_health);
    }

    @Click
    void cardRepairClicked(View card) {
        startCategoryActivity(card, R.string.category_repair, R.drawable.category_repair, R.color.category_repair);
    }

    @Click
    void cardBeautyClicked(View card) {
        startCategoryActivity(card, R.string.category_beauty, R.drawable.category_beauty, R.color.category_beauty);
    }

    @Click
    void cardIntellectClicked(View card) {
        startCategoryActivity(card, R.string.category_intellect, R.drawable.category_intellect, R.color.category_intellect);
    }

    @Click
    void cardHomeClicked(View card) {
        startCategoryActivity(card, R.string.category_home, R.drawable.category_home, R.color.category_home);
    }

    @Click
    void cardLeisureClicked(View card) {
        startCategoryActivity(card, R.string.category_leisure, R.drawable.category_leisure, R.color.category_leisure);
    }

    @Click
    void cardTravelClicked(View card) {
        startCategoryActivity(card, R.string.category_travel, R.drawable.category_travel, R.color.category_travel);
    }

    @Click
    void cardTransportClicked(View card) {
        startCategoryActivity(card, R.string.category_transport, R.drawable.category_transport, R.color.category_transport);
    }

    @Click
    void cardFunClicked(View card) {
        startCategoryActivity(card, R.string.category_fun, R.drawable.category_fun, R.color.category_fun);
    }

    private void startCategoryActivity(View card, int titleId, int iconId, int colorId) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), Pair.create(card, "category_card"));
        CategoryActivity_.intent(getContext())
                .extra(CategoryActivity.EXTRA_TITLE_ID, titleId)
                .extra(CategoryActivity.EXTRA_ICON_ID, iconId)
                .extra(CategoryActivity.EXTRA_COLOR_ID, colorId)
                .withOptions(options.toBundle())
                .start();
    }

}
