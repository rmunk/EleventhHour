package hr.nas2skupa.eleventhhour.ui;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.util.SortedListAdapterCallback;
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
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.Preferences_;
import hr.nas2skupa.eleventhhour.common.model.Provider;
import hr.nas2skupa.eleventhhour.common.ui.helpers.VerticalSpaceItemDecoration;
import hr.nas2skupa.eleventhhour.common.utils.Utils;
import hr.nas2skupa.eleventhhour.ui.viewholders.ProviderViewHolder;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;


/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_providers)
@OptionsMenu(R.menu.providers)
public abstract class ProvidersFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static final int REQUEST_PHONE_PERMISSION = 1;

    @Pref Preferences_ preferences;

    @FragmentArg String categoryKey;
    @FragmentArg String subcategoryKey;

    @ViewById ViewGroup layoutMain;
    @ViewById RecyclerView recyclerView;

    private DatabaseReference favoriteReference;
    private ChildEventListener myFavoriteChangedListener;
    private HashMap<String, Boolean> favorites = new HashMap<>();

    ProvidersAdapter adapter;
    private String providerPhone;
    boolean filterSale;
    boolean sortByName;


    public abstract Query getKeyRef();

    public ProvidersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        favoriteReference = FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("userFavorites")
                .child(Utils.getMyUid());

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
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(getContext(), 8));

        adapter = new ProvidersAdapter(filterSale, sortByName);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.action_sale);
        if (item != null) item.getIcon().setAlpha(138);
        item = menu.findItem(R.id.action_sort_by_name);
        if (item != null) item.getIcon().setAlpha(138);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.providers_search_by_name));
        searchView.setOnQueryTextListener(this);
    }

    @OptionsItem(R.id.action_sale)
    void filterSale(MenuItem item) {
        item.setChecked(!item.isChecked());
        item.getIcon().setAlpha(item.isChecked() ? 255 : 138);

        filterSale = item.isChecked();
        adapter.cleanup();
        adapter = new ProvidersAdapter(filterSale, sortByName);
        recyclerView.swapAdapter(adapter, false);
    }

    @OptionsItem(R.id.action_sort_by_name)
    void sortByName(MenuItem item) {
        item.setChecked(!item.isChecked());
        item.getIcon().setAlpha(item.isChecked() ? 255 : 138);

        sortByName = item.isChecked();
        adapter.cleanup();
        adapter = new ProvidersAdapter(filterSale, sortByName);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PHONE_PERMISSION:
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + providerPhone));
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        return true;
    }

    class ProvidersAdapter extends FirebaseIndexRecyclerAdapter<Provider, ProviderViewHolder> {
        private int expandedPosition = -1;
        private final boolean filterSale;
        private final boolean sortByName;
        private String query;
        private final SortedList<Provider> providers = new SortedList<>(Provider.class, new SortedListAdapterCallback<Provider>(this) {
            @Override
            public int compare(Provider o1, Provider o2) {
                if (sortByName) {
                    Collator collator = Collator.getInstance(new Locale(Utils.getLanguageIso()));
                    collator.setStrength(Collator.PRIMARY);
                    return collator.compare(o1.name, o2.name);
                }
                else return Float.compare(o2.rating, o1.rating);
            }

            @Override
            public boolean areContentsTheSame(Provider oldItem, Provider newItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(Provider item1, Provider item2) {
                return Objects.equals(item1.key, item2.key);
            }
        });


        ProvidersAdapter(boolean filterSale, boolean sortByName) {
            super(Provider.class,
                    R.layout.item_provider,
                    ProviderViewHolder.class,
                    ProvidersFragment.this.getKeyRef(),
                    FirebaseDatabase.getInstance().getReference()
                            .child("providers")
                            .child(preferences.country().get())
                            .child("data"));
            this.filterSale = filterSale;
            this.sortByName = sortByName;
        }

        @Override
        public Provider getItem(int position) {
            return providers.get(position);
        }

        @Override
        public int getItemCount() {
            return providers.size();
        }

        @Override
        public void onChildChanged(EventType type, DataSnapshot snapshot, int index, int oldIndex) {
            Provider oldProvider;
            Provider provider = super.getItem(index);
            provider.key = snapshot.getKey();

            switch (type) {
                case ADDED:
                    if (!filterSale || provider.hasSale) {
                        providers.add(provider);
                        refreshFilter();
                    }
                    break;
                case CHANGED:
                    oldProvider = null;
                    for (int i = 0; i < providers.size(); i++) {
                        if (Objects.equals(provider.key, providers.get(i).key)) {
                            oldProvider = providers.get(i);
                            break;
                        }
                    }
                    if (oldProvider != null) {
                        if (oldProvider.hasSale != provider.hasSale) {
                            if (provider.hasSale) providers.add(provider);
                            else providers.remove(oldProvider);
                        }
                    } else if (provider.hasSale) providers.add(provider);
                    refreshFilter();
                    break;
                case REMOVED:
                    oldProvider = null;
                    for (int j = 0; j < providers.size(); j++) {
                        if (Objects.equals(provider.key, providers.get(j).key)) {
                            oldProvider = providers.get(j);
                            break;
                        }
                    }
                    if (oldProvider != null) {
                        providers.remove(oldProvider);
                        refreshFilter();
                    }
                    break;
                case MOVED:
                    break;
            }
        }

        @Override
        protected void populateViewHolder(final ProviderViewHolder viewHolder, final Provider provider, final int position) {
            provider.favorite = favorites.containsKey(provider.key)
                    ? favorites.get(provider.key)
                    : false;

            viewHolder.bindToProvider(provider);

            final boolean isExpanded = position == expandedPosition;
            viewHolder.showDetails(isExpanded);
            viewHolder.itemView.setActivated(isExpanded);
            viewHolder.imgExpand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                        animateCardExpansion(viewHolder.getAdapterPosition());
                    else recyclerView.scrollToPosition(viewHolder.getAdapterPosition());

                    expandedPosition = isExpanded ? -1 : viewHolder.getAdapterPosition();
                    notifyDataSetChanged();
                }
            });
            viewHolder.txtPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        providerPhone = provider.phone;
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_PERMISSION);
                        return;
                    }
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + provider.phone));
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
            viewHolder.txtWeb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = provider.web;
                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        url = "http://" + url;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
            viewHolder.txtEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + provider.email));
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(Intent.createChooser(intent, "Email"));
                    }
                }
            });

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProviderActivity_.intent(getContext()).providerKey(provider.key).start();
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

        void replaceAll(List<Provider> providers) {
            this.providers.beginBatchedUpdates();
            for (int i = this.providers.size() - 1; i >= 0; i--) {
                final Provider provider = this.providers.get(i);
                if (!providers.contains(provider)) {
                    this.providers.remove(provider);
                }
            }
            this.providers.addAll(providers);
            this.providers.endBatchedUpdates();
        }

        void filter(String query) {
            this.query = query;
            final String lowerCaseQuery = query.toLowerCase();

            final List<Provider> filteredModelList = new ArrayList<>();
            for (int i = 0; i < super.getItemCount(); i++) {
                Provider provider = super.getItem(i);
                final String text = provider.name.toLowerCase();
                if (!filterSale || provider.hasSale) {
                    if (text.contains(lowerCaseQuery)) {
                        filteredModelList.add(provider);
                    }
                }
            }
            replaceAll(filteredModelList);
        }

        void refreshFilter() {
            if (query != null && !query.isEmpty()) filter(query);
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
