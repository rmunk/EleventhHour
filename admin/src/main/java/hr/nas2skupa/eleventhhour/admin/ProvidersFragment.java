package hr.nas2skupa.eleventhhour.admin;


import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.model.Provider;
import hr.nas2skupa.eleventhhour.ui.helpers.SimpleDividerItemDecoration;


/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_providers)
public class ProvidersFragment extends Fragment {

    @ViewById ViewGroup layoutMain;
    @ViewById RecyclerView recyclerView;

    private FirebaseRecyclerAdapter<Provider, ProviderViewHolder> adapter;

    public ProvidersFragment() {
        // Required empty public constructor
    }

    @AfterViews
    public void init() {
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("providers").orderByChild("name");
        adapter = new FirebaseRecyclerAdapter<Provider, ProviderViewHolder>(
                Provider.class,
                R.layout.item_provider,
                ProviderViewHolder.class,
                query) {
            @Override
            protected void populateViewHolder(final ProviderViewHolder viewHolder, final Provider model, int position) {
                viewHolder.bindToProvider(model);
                model.setKey(getRef(position).getKey());
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProviderDetailsActivity_.intent(getContext())
                                .providerKey(model.getKey())
                                .start();
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (adapter != null) adapter.cleanup();
    }


    @Click(R.id.fab_add_provider)
    void addProvider() {
        ProviderEditActivity_.intent(getContext()).start();
    }
}
