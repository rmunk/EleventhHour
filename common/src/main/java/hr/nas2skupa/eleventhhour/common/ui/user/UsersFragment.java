package hr.nas2skupa.eleventhhour.common.ui.user;


import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.common.R;
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

    protected abstract void onUserSelected(User user);

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
        adapter = new UsersAdapter(
                User.class,
                R.layout.item_user,
                UserViewHolder.class,
                getKeyRef(),
                dataRef);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (adapter != null) adapter.cleanup();
    }

    @ViewById
    protected void recyclerView(RecyclerView recyclerView) {
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new SlideInUpAnimator(new OvershootInterpolator(0.1f)));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        recyclerView.setAdapter(adapter);
    }

    protected class UsersAdapter extends FirebaseIndexRecyclerAdapter<User, UserViewHolder> {

        public UsersAdapter(Class<User> modelClass, @LayoutRes int modelLayout, Class<UserViewHolder> viewHolderClass, Query keyQuery, DatabaseReference dataRef) {
            super(modelClass, modelLayout, viewHolderClass, keyQuery, dataRef);
        }

        @Override
        public void onDataChanged() {
            super.onDataChanged();
            loader.setVisibility(View.GONE);
        }

        @Override
        public void onChildChanged(EventType type, DataSnapshot snapshot, int index, int oldIndex) {
            super.onChildChanged(type, snapshot, index, oldIndex);
        }

        @Override
        protected void populateViewHolder(UserViewHolder holder, final User user, int position) {
            user.key = getRef(position).getKey();
            holder.bindToUser(user);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUserSelected(user);
                }
            });
        }
    }
}
