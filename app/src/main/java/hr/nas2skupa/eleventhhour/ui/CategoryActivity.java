package hr.nas2skupa.eleventhhour.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import hr.nas2skupa.eleventhhour.ProvidersFragment;
import hr.nas2skupa.eleventhhour.ProvidersFragment_;
import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.events.SubcategorySelectedEvent;
import hr.nas2skupa.eleventhhour.model.Category;
import hr.nas2skupa.eleventhhour.model.Subcategory;
import hr.nas2skupa.eleventhhour.utils.Utils;


@EActivity(R.layout.activity_category)
@OptionsMenu(R.menu.main)
public class CategoryActivity extends AppCompatActivity {
    @Extra
    String categoryKey;
    @Extra
    String subcategoryKey;

    @ViewById(R.id.main_layout)
    ViewGroup mainLayout;
    @ViewById(R.id.app_bar)
    AppBarLayout appBar;

    @ViewById(R.id.txt_category_name)
    TextView txtCategoryName;
    @ViewById(R.id.txt_subcategory_name)
    TextView txtSubcategoryName;
    @ViewById(R.id.img_category_icon)
    ImageView imgCategoryIcon;

    void setToolbar(@ViewById(R.id.app_bar) AppBarLayout appBar, @ViewById(R.id.toolbar) Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private DatabaseReference categoryReference;
    private ValueEventListener categoryListener;

    @UiThread(delay = 500)
    public void setSubcategoryFragment() {
        SubcategoriesFragment fragment = SubcategoriesFragment_.builder().categoryKey(categoryKey).build();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment, "SubcategoriesFragment").commit();
    }

    @UiThread(delay = 500)
    public void setProvidersFragment() {
        ProvidersFragment fragment = ProvidersFragment_.builder().subcategoryKey(subcategoryKey).build();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragment, "ProvidersFragment")
                .addToBackStack("ProvidersFragment")
                .commit();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categoryReference = FirebaseDatabase.getInstance().getReference().child("categories").child(categoryKey);
        setSubcategoryFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        categoryListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Category category = dataSnapshot.getValue(Category.class);
                if (category == null) return;

                int color;
                try {
                    color = Color.parseColor(category.getColor());
                } catch (Exception e) {
                    color = getResources().getColor(R.color.colorPrimary);
                }
                txtCategoryName.setText(Utils.getLocaleName(category.getName()));
                appBar.setBackgroundColor(color);
                Picasso.with(CategoryActivity.this).load(category.getIcon()).into(imgCategoryIcon);
                imgCategoryIcon.setBackgroundColor(color);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Category failed, show a message
                Snackbar.make(mainLayout, "Failed to load category.",
                        Snackbar.LENGTH_LONG).show();
            }
        };
        categoryReference.addValueEventListener(categoryListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);

        if (categoryListener != null) {
            categoryReference.removeEventListener(categoryListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (getFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            setSubcategoryFragment();
            txtSubcategoryName.setText(R.string.category_pick_a_subcategory);
        }
    }

    @Subscribe
    public void openSubcategory(SubcategorySelectedEvent event) {
        subcategoryKey = event.getSubcategoryKey();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("subcategories")
                .child(categoryKey)
                .child(subcategoryKey);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Subcategory subcategory = dataSnapshot.getValue(Subcategory.class);
                if (subcategory == null) return;
                txtSubcategoryName.setText(Utils.getLocaleName(subcategory.getName()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        getSupportFragmentManager().beginTransaction()
                .remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                .commit();
        setProvidersFragment();
    }
}
