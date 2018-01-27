package hr.nas2skupa.eleventhhour.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.model.Category;
import hr.nas2skupa.eleventhhour.ui.viewholders.CategoryViewHolder;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

@EFragment(R.layout.fragment_home)
public class HomeFragment extends Fragment {
    @ViewById(R.id.categories_list)
    RecyclerView recyclerView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager manager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new LandingAnimator(new OvershootInterpolator(0.1f)));
        recyclerView.getItemAnimator().setAddDuration(300);
        recyclerView.getItemAnimator().setRemoveDuration(300);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("app/categories");
        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(query, Category.class)
                .setLifecycleOwner(this)
                .build();
        FirebaseRecyclerAdapter<Category, CategoryViewHolder> adapter = new FirebaseRecyclerAdapter<Category, CategoryViewHolder>(options) {
            @Override
            public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false));
            }

            @Override
            protected void onBindViewHolder(final CategoryViewHolder viewHolder, int position, final Category model) {
                final DatabaseReference categoryRef = getRef(position);
                final String categoryKey = categoryRef.getKey();

                viewHolder.bind(model);
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
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                Pair.create(card.findViewById(R.id.category_background), "category_header"),
                Pair.create(card.findViewById(R.id.img_category_icon), "category_icon"),
                Pair.create(card.findViewById(R.id.txt_category_name), "category_title")
        );
        CategoryActivity_.intent(getContext())
                .categoryKey(categoryKey)
                .withOptions(options.toBundle())
                .start();
    }
}
