package hr.nas2skupa.eleventhhour.panel.clients;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RatingBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DimensionPixelOffsetRes;

import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.common.model.User;
import hr.nas2skupa.eleventhhour.panel.MainActivity;
import hr.nas2skupa.eleventhhour.panel.R;
import hr.nas2skupa.eleventhhour.panel.databinding.ItemClientBookingBinding;

@EActivity(R.layout.activity_client_details)
@OptionsMenu(R.menu.menu_client_details)
public class ClientDetailsActivity extends AppCompatActivity {
    @Extra String userKey;

    @ViewById ViewGroup layoutMain;
    @ViewById AppBarLayout appBar;
    @ViewById CollapsingToolbarLayout toolbarLayout;
    @ViewById ViewGroup ratingHolder;
    @ViewById RatingBar ratingBar;
    @ViewById RecyclerView recyclerView;
    @ViewById ProgressBar loader;

    @DimensionPixelOffsetRes int appBarHeight;

    private DatabaseReference userReference;
    private ValueEventListener userListener;
    private FirebaseRecyclerAdapter<Booking, ClientBookingViewHolder> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userReference = FirebaseDatabase.getInstance().getReference()
                .child("users/data")
                .child(userKey);
        userListener = new UserChangedListener();

        adapter = new BookingAdapter(FirebaseDatabase.getInstance().getReference()
                .child("providerAppointments")
                .child(MainActivity.providerKey)
                .child("data")
                .orderByChild("userId")
                .equalTo(userKey));
    }

    @Override
    public void onStart() {
        super.onStart();
        userReference.addValueEventListener(userListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        userReference.removeEventListener(userListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.cleanup();
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        onBackPressed();
    }

    @SuppressWarnings("ConstantConditions")
    void setToolbar(@ViewById(R.id.toolbar) Toolbar toolbar) {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void setAppBar(@ViewById(R.id.app_bar) AppBarLayout appBar) {
        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                ratingHolder.setVisibility(verticalOffset < 0 ? View.GONE : View.VISIBLE);
            }
        });
    }

    void setRecyclerView(@ViewById(R.id.recycler_view) RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private class BookingAdapter extends FirebaseRecyclerAdapter<Booking, ClientBookingViewHolder> {

        public BookingAdapter(Query bookingsQuery) {
            super(Booking.class, R.layout.item_client_booking, ClientBookingViewHolder.class, bookingsQuery);
        }

        @Override
        public void onDataChanged() {
            super.onDataChanged();
            loader.setVisibility(View.GONE);
        }

        @Override
        public ClientBookingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ItemClientBookingBinding itemBinding = ItemClientBookingBinding.inflate(layoutInflater, parent, false);
            return new ClientBookingViewHolder(itemBinding);
        }

        @Override
        protected void populateViewHolder(final ClientBookingViewHolder viewHolder, final Booking model, final int position) {
            model.key = getRef(position).getKey();
            viewHolder.bind(model);
        }
    }

    private class UserChangedListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);
            if (user == null) return;

            user.key = dataSnapshot.getKey();
            toolbarLayout.setTitle(user.name);
            ratingBar.setRating(user.rating);
            ratingBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
