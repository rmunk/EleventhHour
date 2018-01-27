package hr.nas2skupa.eleventhhour.common.ui.provider;


import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.common.R;
import hr.nas2skupa.eleventhhour.common.R2;
import hr.nas2skupa.eleventhhour.common.model.Service;
import hr.nas2skupa.eleventhhour.common.ui.helpers.SimpleDividerItemDecoration;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R2.layout.fragment_services)
public class ServicesFragment extends Fragment {
    @FragmentArg String providerKey;

    @ViewById RecyclerView recyclerView;

    private OnServiceClickListener listener;

    public ServicesFragment() {
        // Required empty public constructor
    }

    @AfterViews
    public void init() {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(0.1f)));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        Query query = FirebaseDatabase.getInstance().getReference()
                .child("providerServices")
                .child(providerKey)
                .child("data")
                .orderByChild("name");
        FirebaseRecyclerOptions<Service> options = new FirebaseRecyclerOptions.Builder<Service>()
                .setQuery(query, Service.class)
                .setLifecycleOwner(this)
                .build();
        FirebaseRecyclerAdapter<Service, ServiceViewHolder> adapter = new ServicesAdapter(options);
        recyclerView.setAdapter(adapter);
    }

    public void setOnServiceClickListener(OnServiceClickListener listener) {
        this.listener = listener;
    }

    private class ServicesAdapter extends FirebaseRecyclerAdapter<Service, ServiceViewHolder> {
        public ServicesAdapter(FirebaseRecyclerOptions<Service> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(ServiceViewHolder viewHolder, int position, Service service) {
            super.onBindViewHolder(viewHolder, position);
            viewHolder.bindToService(service);
            service.key = getRef(position).getKey();
            viewHolder.itemView.setOnClickListener(view -> {
                if (listener != null) listener.onServiceClick(view, providerKey, service.key);
            });
        }

        @Override
        public ServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ServiceViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false));
        }
    }

    public interface OnServiceClickListener {
        void onServiceClick(View view, String providerKey, String serviceKey);
    }
}
