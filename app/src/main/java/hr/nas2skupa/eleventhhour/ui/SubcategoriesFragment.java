package hr.nas2skupa.eleventhhour.ui;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.model.Subcategory;
import hr.nas2skupa.eleventhhour.common.ui.helpers.SimpleDividerItemDecoration;
import hr.nas2skupa.eleventhhour.common.utils.Utils;
import hr.nas2skupa.eleventhhour.events.SubcategorySelectedEvent;
import hr.nas2skupa.eleventhhour.ui.viewholders.SubcategoryViewHolder;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_recycler_view)
public class SubcategoriesFragment extends Fragment {
    @FragmentArg
    String categoryKey;

    @ViewById(R.id.recycler_view)
    RecyclerView recyclerView;

    public SubcategoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) setTransitions();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
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
                final DatabaseReference categoryRef = getRef(position);
                final String subcategoryKey = categoryRef.getKey();

                viewHolder.bind(model);
                viewHolder.itemView.setOnClickListener(view -> EventBus.getDefault().post(new SubcategorySelectedEvent(subcategoryKey)));
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setTransitions() {
        setAllowEnterTransitionOverlap(false);
        setAllowReturnTransitionOverlap(false);

        setEnterTransition(new Fade());
        setReenterTransition(new Fade());
        setExitTransition(new Slide(Gravity.TOP));
        setReturnTransition(new Slide(Gravity.TOP));
    }
}
