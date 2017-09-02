package hr.nas2skupa.eleventhhour.common.ui.provider;


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

import hr.nas2skupa.eleventhhour.common.R;
import hr.nas2skupa.eleventhhour.common.model.Service;
import hr.nas2skupa.eleventhhour.common.ui.helpers.SimpleDividerItemDecoration;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(resName = "fragment_services")
public class ServicesFragment extends Fragment {
    @FragmentArg String providerKey;

    @ViewById RecyclerView recyclerView;

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
        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(0.1f)));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("services").child(providerKey).orderByChild("name");
        adapter = new ServicesAdapter(Service.class, R.layout.item_service, ServiceViewHolder.class, query);
        recyclerView.setAdapter(adapter);
    }

    @Click(resName = "fab_add_service")
    void addService() {
        ServiceDialog_.builder()
                .providerKey(providerKey)
                .serviceKey(null)
                .build()
                .show(getFragmentManager(), "ServiceDialog");
    }

    private class ServicesAdapter extends FirebaseRecyclerAdapter<Service, ServiceViewHolder> {
        public ServicesAdapter(Class<Service> modelClass, int modelLayout, Class<ServiceViewHolder> viewHolderClass, Query ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        @Override
        protected void populateViewHolder(ServiceViewHolder viewHolder, final Service model, int position) {
            viewHolder.bindToService(model);
            model.key = getRef(position).getKey();
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ServiceDialog_.builder()
                            .providerKey(providerKey)
                            .serviceKey(model.key)
                            .build()
                            .show(getFragmentManager(), "ServiceDialog");
                }
            });
        }
    }
}
