package hr.nas2skupa.eleventhhour.admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import hr.nas2skupa.eleventhhour.auth.SignInActivity;
import hr.nas2skupa.eleventhhour.model.Provider;
import hr.nas2skupa.eleventhhour.ui.helpers.SimpleDividerItemDecoration;
import hr.nas2skupa.eleventhhour.utils.Utils;


@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.menu_main)
public class MainActivity extends AppCompatActivity {

    @ViewById(R.id.layout_main)
    ViewGroup layoutMain;
    @ViewById(R.id.recycler_view)
    RecyclerView recyclerView;

    private FirebaseRecyclerAdapter<Provider, ProviderViewHolder> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance()
                .getReference("admins")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild(user.getUid())) {
                            Toast.makeText(MainActivity.this, user.getDisplayName() + " is not EleventhHour administrator!", Toast.LENGTH_LONG).show();
                            signOut();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        signOut();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (adapter != null) adapter.cleanup();
    }

    @AfterViews
    public void init() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("providers").orderByChild("name/" + Utils.getLanguageIso());
        adapter = new FirebaseRecyclerAdapter<Provider, ProviderViewHolder>(
                Provider.class,
                R.layout.item_provider,
                ProviderViewHolder.class,
                query) {
            @Override
            protected void populateViewHolder(final ProviderViewHolder viewHolder, Provider model, int position) {
                final DatabaseReference categoryRef = getRef(position);

                viewHolder.bindToProvider(model);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: open provider page
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @OptionsItem(R.id.menu_sign_out)
    void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(MainActivity.this, SignInActivity.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.sign_out_failed, Toast.LENGTH_LONG);
                        }
                    }
                });
    }
}
