package hr.nas2skupa.eleventhhour.admin;


import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

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

    private FirebaseRecyclerAdapter<Subcategory, SubcategoryViewHolder> adapter;

    public SubcategoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (adapter != null) adapter.cleanup();
    }

    @AfterViews
    public void init() {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(0.1f)));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("app/subcategories").child(categoryKey).orderByChild("name/" + Utils.getLanguageIso());
        adapter = new SubcategoriesAdapter(Subcategory.class, R.layout.item_subcategory, SubcategoryViewHolder.class, query);
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


    private class SubcategoriesAdapter extends FirebaseRecyclerAdapter<Subcategory, SubcategoryViewHolder> {

        public SubcategoriesAdapter(Class<Subcategory> modelClass, int modelLayout, Class<SubcategoryViewHolder> viewHolderClass, Query ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        @Override
        protected void populateViewHolder(SubcategoryViewHolder viewHolder, final Subcategory model, int position) {
            viewHolder.bindToSubcategory(model);
            model.key = getRef(position).getKey();
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SubcategoryDialog_.builder()
                            .categoryKey(categoryKey)
                            .subcategoryKey(model.key)
                            .build()
                            .show(getFragmentManager(), "SubcategoryDialog");
                }
            });
        }
    }
}
