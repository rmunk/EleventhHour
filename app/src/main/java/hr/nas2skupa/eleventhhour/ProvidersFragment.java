package hr.nas2skupa.eleventhhour;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.transition.Fade;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.model.Provider;
import hr.nas2skupa.eleventhhour.ui.ProviderViewHolder;
import hr.nas2skupa.eleventhhour.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_recycler_view)
public class ProvidersFragment extends Fragment {
    @FragmentArg
    String subcategoryKey;

    @ViewById(R.id.recycler_view)
    RecyclerView recyclerView;

    public ProvidersFragment() {
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

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("subcategory_providers").child(subcategoryKey).orderByChild("name/" + Utils.getLanguageIso());
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Provider, ProviderViewHolder>(
                Provider.class,
                R.layout.item_provider,
                ProviderViewHolder.class,
                query) {
            @Override
            protected void populateViewHolder(ProviderViewHolder viewHolder, Provider model, int position) {
                viewHolder.bindToProvider(model);
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setTransitions() {
        setAllowEnterTransitionOverlap(false);
        setAllowReturnTransitionOverlap(false);

        setEnterTransition(new Fade().setStartDelay(500).setDuration(500));
        setReenterTransition(new Fade().setStartDelay(500).setDuration(500));
        setExitTransition(new Explode().setDuration(500));
        setReturnTransition(new Explode().setDuration(500));
    }
}
