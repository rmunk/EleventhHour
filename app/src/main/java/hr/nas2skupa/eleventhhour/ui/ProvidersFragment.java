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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Provider;
import hr.nas2skupa.eleventhhour.ui.viewholders.ProviderViewHolder;
import hr.nas2skupa.eleventhhour.utils.Utils;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;


/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_providers)
@OptionsMenu(R.menu.providers)
public abstract class ProvidersFragment extends Fragment {
    @FragmentArg
    String categoryKey;
    @FragmentArg
    String subcategoryKey;

    @ViewById(R.id.layout_main)
    ViewGroup layoutMain;
    @ViewById(R.id.recycler_view)
    RecyclerView recyclerView;

    private DatabaseReference favoriteReference;
    private ChildEventListener myFavoriteChangedListener;
    private HashMap<String, Boolean> favorites = new HashMap<>();

    private Query dataRef;
    private FirebaseRecyclerAdapter<Provider, ProviderViewHolder> adapter;


    public abstract Query getKeyRef();

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) setTransitions();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(0.1f)));
        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);

        dataRef = FirebaseDatabase.getInstance().getReference().child("providers");
        adapter = new ProvidersAdapter(
                Provider.class,
                R.layout.item_provider,
                ProviderViewHolder.class,
                getKeyRef(),
                dataRef);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.action_sale);
        if (item != null) item.getIcon().setAlpha(138);
    }

    @OptionsItem(R.id.action_sale)
    void multipleMenuItems(MenuItem item) {
        item.setChecked(!item.isChecked());
        item.getIcon().setAlpha(item.isChecked() ? 255 : 138);

        if (item.isChecked()) dataRef = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .orderByChild("sale")
                .equalTo(true);
        else dataRef = FirebaseDatabase.getInstance().getReference().child("providers");

        adapter = new ProvidersAdapter(Provider.class,
                R.layout.item_provider,
                ProviderViewHolder.class,
                getKeyRef(),
                dataRef);
        recyclerView.swapAdapter(adapter, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        myFavoriteChangedListener = new MyFavoriteChangedListener();
        favoriteReference.addChildEventListener(myFavoriteChangedListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (myFavoriteChangedListener != null)
            favoriteReference.removeEventListener(myFavoriteChangedListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (adapter != null) {
            adapter.cleanup();
            adapter = null;
        }
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

    public class ProvidersAdapter extends FirebaseIndexRecyclerAdapter<Provider, ProviderViewHolder> {
        public int expandedPosition = -1;

        public ProvidersAdapter(Class<Provider> modelClass, int modelLayout, Class<ProviderViewHolder> viewHolderClass, Query keyRef, Query dataRef) {
            super(modelClass, modelLayout, viewHolderClass, keyRef, dataRef);
        }

        @Override
        protected void populateViewHolder(final ProviderViewHolder viewHolder, final Provider provider, final int position) {
            provider.setKey(getRef(position).getKey());

            provider.setFavorite(favorites.containsKey(provider.getKey())
                    ? favorites.get(provider.getKey())
                    : false);

            viewHolder.bindToProvider(provider);

            final boolean isExpanded = position == expandedPosition;
            viewHolder.showDetails(isExpanded);
            viewHolder.itemView.setActivated(isExpanded);
            View imgExpand = viewHolder.itemView.findViewById(R.id.img_expand);
            imgExpand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                        animateCardExpansion(position);
                    else recyclerView.scrollToPosition(position);

                    expandedPosition = isExpanded ? -1 : position;
                    notifyDataSetChanged();
                }
            });

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProviderActivity_.intent(getContext()).providerKey(provider.getKey()).start();
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void animateCardExpansion(final int position) {
            Transition transition = new AutoTransition();
            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {

                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    Transition transition2 = new AutoTransition();
                    transition2.setDuration(200);
                    TransitionManager.beginDelayedTransition(recyclerView, transition2);
                    recyclerView.scrollToPosition(position);
                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });
            TransitionManager.beginDelayedTransition(recyclerView, transition);
        }
    }

    private class MyFavoriteChangedListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            favorites.put(dataSnapshot.getKey(), true);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Boolean value = dataSnapshot.getValue(Boolean.class);
            favorites.put(dataSnapshot.getKey(), value != null);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            favorites.remove(dataSnapshot.getKey());
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
