package hr.nas2skupa.eleventhhour.admin;


import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.admin.viewholders.SubcategoryViewHolder;
import hr.nas2skupa.eleventhhour.common.model.Subcategory;
import hr.nas2skupa.eleventhhour.common.ui.helpers.SimpleDividerItemDecoration;
import hr.nas2skupa.eleventhhour.common.utils.Utils;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_subcategories)
public class SubcategoriesFragment extends Fragment {
    @FragmentArg String categoryKey;

    @ViewById RecyclerView recyclerView;

    public SubcategoriesFragment() {
        // Required empty public constructor
    }

    @AfterViews
    public void init() {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(0.1f)));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("app/subcategories").child(categoryKey).orderByChild("name/" + Utils.getLanguageIso());
        FirebaseRecyclerOptions<Subcategory> options = new FirebaseRecyclerOptions.Builder<Subcategory>()
                .setQuery(query, Subcategory.class)
                .setLifecycleOwner(this)
                .build();
        FirebaseRecyclerAdapter<Subcategory, SubcategoryViewHolder> adapter = new FirebaseRecyclerAdapter<Subcategory, SubcategoryViewHolder>(options) {
            @Override
            public SubcategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new SubcategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subcategory, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final SubcategoryViewHolder viewHolder, int position, @NonNull Subcategory model) {
                viewHolder.bind(model);
                model.key = getRef(position).getKey();
                viewHolder.itemView.setOnClickListener(view -> SubcategoryDialog_.builder()
                        .categoryKey(categoryKey)
                        .subcategoryKey(model.key)
                        .build()
                        .show(getFragmentManager(), "SubcategoryDialog"));
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Click(R.id.fab_add_subcategory)
    void addSubcategory() {
        SubcategoryDialog_.builder()
                .categoryKey(categoryKey)
                .subcategoryKey(null)
                .build()
                .show(getFragmentManager(), "SubcategoryDialog");
    }
}
