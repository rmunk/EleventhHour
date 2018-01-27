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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import hr.nas2skupa.eleventhhour.admin.viewholders.ProviderViewHolder;
import hr.nas2skupa.eleventhhour.common.Preferences_;
import hr.nas2skupa.eleventhhour.common.model.Provider;
import hr.nas2skupa.eleventhhour.common.ui.helpers.SimpleDividerItemDecoration;
import hr.nas2skupa.eleventhhour.common.ui.provider.ProviderDetailsActivity_;
import hr.nas2skupa.eleventhhour.common.ui.provider.ProviderEditActivity_;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;


/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_providers)
public class ProvidersFragment extends Fragment {

    @Pref Preferences_ preferences;

    @ViewById ViewGroup layoutMain;
    @ViewById RecyclerView recyclerView;

    public ProvidersFragment() {
        // Required empty public constructor
    }

    @AfterViews
    public void init() {
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(0.1f)));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        Query query = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("data")
                .orderByChild("name");
        FirebaseRecyclerOptions<Provider> options = new FirebaseRecyclerOptions.Builder<Provider>()
                .setQuery(query, Provider.class)
                .setLifecycleOwner(this)
                .build();
        FirebaseRecyclerAdapter<Provider, ProviderViewHolder> adapter = new FirebaseRecyclerAdapter<Provider, ProviderViewHolder>(options) {
            @Override
            public ProviderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ProviderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_provider, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final ProviderViewHolder viewHolder, int position, @NonNull Provider model) {
                viewHolder.bind(model);
                model.key = getRef(position).getKey();
                viewHolder.itemView.setOnClickListener(view -> ProviderDetailsActivity_.intent(getContext())
                        .providerKey(model.key)
                        .start());
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Click(R.id.fab_add_provider)
    void addProvider() {
        ProviderEditActivity_.intent(getContext()).start();
    }
}
