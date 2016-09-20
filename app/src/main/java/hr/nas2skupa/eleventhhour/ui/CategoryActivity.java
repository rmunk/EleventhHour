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

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Category;


@EActivity(R.layout.activity_category)
@OptionsMenu(R.menu.main)
public class CategoryActivity extends AppCompatActivity {
    @Extra
    String categoryKey;

    @ViewById(R.id.main_layout)
    ViewGroup mainLayout;
    @ViewById(R.id.app_bar)
    AppBarLayout appBar;

    @ViewById(R.id.txt_category_name)
    TextView txtCategoryName;
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
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, "SubcategoriesFragment").commit();
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

        // Add value event listener to the category
        categoryListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Category category = dataSnapshot.getValue(Category.class);
                int color;
                try {
                    color = Color.parseColor(category.getColor());
                } catch (Exception e) {
                    color = getResources().getColor(R.color.colorPrimary);
                }
                txtCategoryName.setText(category.getLocaleName());
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

        // Remove category value event listener
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
}
