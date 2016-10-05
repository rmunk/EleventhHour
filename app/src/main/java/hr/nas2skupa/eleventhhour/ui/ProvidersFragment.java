package hr.nas2skupa.eleventhhour.ui;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.AutoTransition;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Provider;
import hr.nas2skupa.eleventhhour.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_recycler_view)
public class ProvidersFragment extends Fragment {
    @FragmentArg
    String categoryKey;
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
        Query query = database.child("providers").child(subcategoryKey).orderByChild("txtCategoryName/" + Utils.getLanguageIso());
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Provider, ProviderViewHolder>(
                Provider.class,
                R.layout.item_provider,
                ProviderViewHolder.class,
                query) {

            public int expandedPosition = -1;

            @Override
            protected void populateViewHolder(final ProviderViewHolder viewHolder, final Provider model, final int position) {
                final DatabaseReference categoryRef = getRef(position);
                final String providerKey = categoryRef.getKey();

                FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(Utils.getMyUid())
                        .child("favorites")
                        .child(providerKey)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                boolean favorite = dataSnapshot.exists() ? dataSnapshot.getValue(boolean.class) : false;
                                model.setFavorite(favorite);
                                viewHolder.setFavorite(favorite);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                viewHolder.bindToProvider(categoryKey, subcategoryKey, providerKey, model);

                final boolean isExpanded = position == expandedPosition;
                viewHolder.showDetails(isExpanded);
                viewHolder.itemView.setActivated(isExpanded);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        expandedPosition = isExpanded ? -1 : position;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            Transition transition = new AutoTransition();
                            transition.setDuration(200);
                            TransitionManager.beginDelayedTransition(recyclerView, transition);
                        }
                        notifyDataSetChanged();
                    }
                });
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
