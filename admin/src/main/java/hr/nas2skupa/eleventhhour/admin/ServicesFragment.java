package hr.nas2skupa.eleventhhour.admin;


import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.model.Service;
import hr.nas2skupa.eleventhhour.ui.helpers.SimpleDividerItemDecoration;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_services)
public class ServicesFragment extends Fragment {
    @FragmentArg
    String providerKey;

    @ViewById(R.id.recycler_view)
    RecyclerView recyclerView;

    private String serviceKey;
    private FirebaseRecyclerAdapter<Service, ServiceViewHolder> adapter;

    public ServicesFragment() {
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
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("services").child(providerKey).orderByChild("name");
        adapter = new FirebaseRecyclerAdapter<Service, ServiceViewHolder>(
                Service.class,
                R.layout.item_service,
                ServiceViewHolder.class,
                query) {
            @Override
            protected void populateViewHolder(final ServiceViewHolder viewHolder, final Service model, final int position) {
                viewHolder.bindToService(model);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final DatabaseReference categoryRef = getRef(position);
                        serviceKey = categoryRef.getKey();
                        ServiceDialog_.builder()
                                .providerKey(providerKey)
                                .serviceKey(serviceKey)
                                .build()
                                .show(getFragmentManager(), "ServiceDialog");

                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Click(R.id.fab_add_service)
    void addService() {
        ServiceDialog_.builder()
                .providerKey(providerKey)
                .serviceKey(null)
                .build()
                .show(getFragmentManager(), "ServiceDialog");
    }
}
