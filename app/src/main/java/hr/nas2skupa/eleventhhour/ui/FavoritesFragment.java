package hr.nas2skupa.eleventhhour.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.animation.OvershootInterpolator;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.utils.Utils;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * Fragment with list of favorite providers
 */
@EFragment(R.layout.fragment_favorites)
public class FavoritesFragment extends Fragment {
    @ViewById(R.id.recycler_view)
    RecyclerView recyclerView;

    private HashMap<String, ProviderItem> providerItems = new HashMap<>();
    private DatabaseReference favoriteReference;
    private ChildEventListener myFavoriteChangedListener;
    private FlexibleAdapter adapter;


    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        favoriteReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(Utils.getMyUid())
                .child("favorites");

        myFavoriteChangedListener = new MyFavoriteChangedListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        favoriteReference.addChildEventListener(myFavoriteChangedListener);

        adapter = new ProvidersFlexibleAdapter(new ArrayList<>(providerItems.values()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        favoriteReference.removeEventListener(myFavoriteChangedListener);

        recyclerView.setAdapter(null);
        adapter = null;
    }

    @AfterViews
    void init() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(0.1f)));
        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);
    }

    private class MyFavoriteChangedListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Boolean isFavorite = dataSnapshot.getValue(Boolean.class);
            if (isFavorite) {
                providerItems.put(dataSnapshot.getKey(), new ProviderItem(dataSnapshot.getKey()));
                adapter.updateDataSet(new ArrayList<>(providerItems.values()), true);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Boolean isFavorite = dataSnapshot.getValue(Boolean.class);
            String providerKey = dataSnapshot.getKey();
            if (isFavorite) {
                providerItems.put(providerKey, new ProviderItem(providerKey));
            } else {
                providerItems.remove(providerKey);
            }
            adapter.updateDataSet(new ArrayList<>(providerItems.values()), true);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            providerItems.remove(dataSnapshot.getKey());
            adapter.updateDataSet(new ArrayList<>(providerItems.values()), true);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
