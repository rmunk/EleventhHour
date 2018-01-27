package hr.nas2skupa.eleventhhour.common.ui.user;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.common.model.User;
import hr.nas2skupa.eleventhhour.common.ui.helpers.SimpleDividerItemDecoration;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;


/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(resName = "fragment_users")
public abstract class UsersFragment extends Fragment {
    protected UsersAdapter adapter;

    protected abstract Query getKeyRef();

    protected abstract Void onUserSelected(User user);

    @ViewById
    protected ProgressBar loader;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference()
                .child("users/data");
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setIndexedQuery(getKeyRef(), dataRef, User.class)
                .setLifecycleOwner(this)
                .build();
        adapter = new UsersAdapter(options,
                () -> {
                    loader.setVisibility(View.GONE);
                    return null;
                },
                this::onUserSelected);
    }

    @ViewById
    protected void recyclerView(RecyclerView recyclerView) {
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(0.1f)));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        recyclerView.setAdapter(adapter);
    }

}
