package hr.nas2skupa.eleventhhour.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.Locale;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Subcategory;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_subcategories)
public class SubcategoriesFragment extends Fragment {
    @FragmentArg
    String categoryKey;

    @ViewById(R.id.subcategories_list)
    RecyclerView recyclerView;

    private DatabaseReference database;
    private FirebaseRecyclerAdapter<Subcategory, SubcategoryViewHolder> adapter;
    private LinearLayoutManager manager;

    public SubcategoriesFragment() {
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

        manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);

        Query query = database.child("subcategories").child(categoryKey).orderByChild("name/" + Locale.getDefault().getISO3Language());
        adapter = new FirebaseRecyclerAdapter<Subcategory, SubcategoryViewHolder>(Subcategory.class, R.layout.item_subcategory,
                SubcategoryViewHolder.class, query) {
            @Override
            protected void populateViewHolder(SubcategoryViewHolder viewHolder, Subcategory model, int position) {
                viewHolder.bindToSubcategory(model);
            }
        };
        recyclerView.setAdapter(adapter);
    }
}
