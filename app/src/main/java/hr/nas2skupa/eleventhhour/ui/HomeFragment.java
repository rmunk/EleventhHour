package hr.nas2skupa.eleventhhour.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Locale;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Category;

@EFragment(R.layout.fragment_home)
public class HomeFragment extends Fragment {
    @ViewById(R.id.categories_list)
    RecyclerView recyclerView;

    private DatabaseReference database;
    private FirebaseRecyclerAdapter<Category, CategoryViewHolder> adapter;
    private LinearLayoutManager manager;

    public HomeFragment() {
        // Required empty public constructor
    }

    @AfterViews
    public void afterViews() {
        database = FirebaseDatabase.getInstance().getReference();
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        manager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(manager);

        Query query = database.child("categories").orderByChild("name/" + Locale.getDefault().getISO3Language());
        adapter = new FirebaseRecyclerAdapter<Category, CategoryViewHolder>(Category.class, R.layout.item_category
                , CategoryViewHolder.class, query) {
            @Override
            protected void populateViewHolder(final CategoryViewHolder viewHolder, final Category model, int position) {
                final DatabaseReference categoryRef = getRef(position);
                final String categoryKey = categoryRef.getKey();

                viewHolder.bindToSubcategory(model);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startCategoryActivity(viewHolder.itemView, categoryKey);
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }

    private void startCategoryActivity(View card, String categoryKey) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), Pair.create(card, "category_card"));
        CategoryActivity_.intent(getContext())
                .categoryKey(categoryKey)
                .withOptions(options.toBundle())
                .start();
    }
}
