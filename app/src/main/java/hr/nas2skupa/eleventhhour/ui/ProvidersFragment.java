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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.events.FavoriteStatusChangedEvent;
import hr.nas2skupa.eleventhhour.events.UserRatingChangedEvent;
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

    private DatabaseReference favoriteReference;
    private ChildEventListener favoriteChangedListener;
    private HashMap<String, Boolean> favorites = new HashMap<>();

    private DatabaseReference ratingReference;
    private ChildEventListener ratingChangedListener;
    private HashMap<String, Float> ratings = new HashMap<>();

    public ProvidersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        favoriteReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(Utils.getMyUid())
                .child("favorites");

        ratingReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(Utils.getMyUid())
                .child("ratings");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) setTransitions();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("providers").child(subcategoryKey).orderByChild("rating");
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Provider, ProviderViewHolder>(
                Provider.class,
                R.layout.item_provider,
                ProviderViewHolder.class,
                query) {

            public int expandedPosition = -1;

            @Override
            protected void populateViewHolder(final ProviderViewHolder viewHolder, final Provider provider, final int position) {
                provider.setKey(getRef(position).getKey());

                provider.setFavorite(favorites.containsKey(provider.getKey())
                        ? favorites.get(provider.getKey())
                        : false);

                provider.setUserRating(ratings.containsKey(provider.getKey())
                        ? ratings.get(provider.getKey())
                        : 0f);

                viewHolder.bindToProvider(provider);

                final boolean isExpanded = position == expandedPosition;
                viewHolder.showDetails(isExpanded);
                viewHolder.itemView.setActivated(isExpanded);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            Transition transition = new AutoTransition();
                            transition.setDuration(100);
                            TransitionManager.beginDelayedTransition(recyclerView, transition);
                        }

                        notifyItemChanged(expandedPosition);
                        expandedPosition = isExpanded ? -1 : position;
                        notifyItemChanged(position);
                        recyclerView.scrollToPosition(position);
                    }
                });
            }

            @Override
            public void onViewAttachedToWindow(ProviderViewHolder holder) {
                super.onViewAttachedToWindow(holder);

                EventBus.getDefault().register(holder);
            }

            @Override
            public void onViewDetachedFromWindow(ProviderViewHolder holder) {
                super.onViewDetachedFromWindow(holder);

                EventBus.getDefault().unregister(holder);
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        favoriteChangedListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                favorites.put(dataSnapshot.getKey(), dataSnapshot.getValue(boolean.class));
                EventBus.getDefault().post(new FavoriteStatusChangedEvent(dataSnapshot.getKey(), dataSnapshot.getValue(boolean.class)));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                favorites.put(dataSnapshot.getKey(), dataSnapshot.getValue(boolean.class));
                EventBus.getDefault().post(new FavoriteStatusChangedEvent(dataSnapshot.getKey(), dataSnapshot.getValue(boolean.class)));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                favorites.remove(dataSnapshot.getKey());
                EventBus.getDefault().post(new FavoriteStatusChangedEvent(dataSnapshot.getKey(), false));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        favoriteReference.addChildEventListener(favoriteChangedListener);

        ratingChangedListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ratings.put(dataSnapshot.getKey(), dataSnapshot.getValue(float.class));
                EventBus.getDefault().post(new UserRatingChangedEvent(dataSnapshot.getKey(), dataSnapshot.getValue(float.class)));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                ratings.put(dataSnapshot.getKey(), dataSnapshot.getValue(float.class));
                EventBus.getDefault().post(new UserRatingChangedEvent(dataSnapshot.getKey(), dataSnapshot.getValue(float.class)));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                ratings.remove(dataSnapshot.getKey());
                EventBus.getDefault().post(new UserRatingChangedEvent(dataSnapshot.getKey(), 0f));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ratingReference.addChildEventListener(ratingChangedListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (favoriteChangedListener != null)
            favoriteReference.removeEventListener(favoriteChangedListener);
        if (ratingChangedListener != null)
            ratingReference.removeEventListener(ratingChangedListener);
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
