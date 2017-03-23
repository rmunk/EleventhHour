package hr.nas2skupa.eleventhhour.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
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
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.model.Category;


@EActivity(R.layout.activity_category)
public class CategoryActivity extends DrawerActivity {
    @Extra String categoryKey;
    @Extra String subcategoryKey;

    @ViewById ViewGroup mainLayout;
    @ViewById AppBarLayout appBar;

    @ViewById TextView txtCategoryName;
    @ViewById TextView txtSubcategoryName;
    @ViewById ImageView categoryBackground;
    @ViewById ImageView imgCategoryIcon;

    void setToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private DatabaseReference categoryReference;
    private ValueEventListener categoryListener;

    @UiThread(delay = 500)
    public void setSubcategoryFragment() {
        if (isFinishing()) return;
        SubcategoriesFragment fragment = SubcategoriesFragment_.builder().categoryKey(categoryKey).build();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment, "SubcategoriesFragment").commit();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categoryReference = FirebaseDatabase.getInstance().getReference().child("categories").child(categoryKey);
        categoryListener = new CategoryListener();

        if (savedInstanceState == null)
            setSubcategoryFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        categoryReference.addValueEventListener(categoryListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        categoryReference.removeEventListener(categoryListener);
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        onBackPressed();
    }

    private class CategoryListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Category category = dataSnapshot.getValue(Category.class);
            if (category == null) return;

            int color;
            try {
                color = Color.parseColor(category.color);
            } catch (Exception e) {
                color = ContextCompat.getColor(CategoryActivity.this, R.color.colorPrimary);
            }

            txtCategoryName.setText(category.getLocalName());
            appBar.setBackgroundColor(color);
            getDrawer().setStatusBarBackgroundColor(color);
            Picasso.with(CategoryActivity.this).load(category.icon).fit().into(imgCategoryIcon);
            categoryBackground.setBackgroundColor(color);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Snackbar.make(mainLayout, "Failed to load category.",
                    Snackbar.LENGTH_LONG).show();
        }
    }
}
