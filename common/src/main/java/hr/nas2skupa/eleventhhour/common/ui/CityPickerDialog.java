package hr.nas2skupa.eleventhhour.common.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import hr.nas2skupa.eleventhhour.common.Preferences_;
import hr.nas2skupa.eleventhhour.common.R;
import hr.nas2skupa.eleventhhour.common.model.City;
import hr.nas2skupa.eleventhhour.common.utils.Utils;

/**
 * Created by nas2skupa on 25/09/2017.
 */

@EFragment(resName = "dialog_city_picker")
public class CityPickerDialog extends DialogFragment implements SearchView.OnQueryTextListener {
    @Pref Preferences_ preferences;

    @ViewById ProgressBar progressBar;

    private View view;
    private CitiesAdapter adapter;
    private City pickedCity;
    private CityPickerDialogListener listener;

    public void setCityPickerDialogListener(CityPickerDialogListener listener) {
        this.listener = listener;
    }

    public CityPickerDialog() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_city_picker, null);

        builder.setView(view)
                .setPositiveButton(R.string.action_pick, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (pickedCity == null) return;

                        if (listener != null) listener.onCityPicked(pickedCity);
                        CityPickerDialog.this.dismiss();
                    }
                })
                .setNeutralButton(R.string.pick_city_all_cities, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (listener != null) listener.onAllSelected();
                        CityPickerDialog.this.dismiss();
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) listener.onCancelled();
                        CityPickerDialog.this.dismiss();
                    }
                });

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view;
    }

    @ViewById(resName = "search_view")
    void setSearchView(SearchView searchView) {
        searchView.setQueryHint(getString(R.string.pick_city_search_hint));
        searchView.setOnQueryTextListener(this);
    }

    @ViewById(resName = "recycler_view")
    void setRecyclerView(RecyclerView recyclerView) {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("app/cities")
                .child(preferences.country().get())
                .orderByChild("name/" + Utils.getLanguageIso());

        adapter = new CitiesAdapter(City.class, R.layout.item_city, CitiesAdapter.CityViewHolder.class, query);
        adapter.setCitiesAdapterListener(new CitiesAdapter.CitiesAdapterListener() {
            @Override
            public void onLoadingFinished() {
                progressBar.setVisibility(View.GONE);
                ((AlertDialog) getDialog()).getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
            }

            @Override
            public void onCityPicked(City city) {
                pickedCity = city;
                ((AlertDialog) getDialog()).getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
            }
        });

        final LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onQueryTextSubmit(String text) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        return true;
    }

    public static class CitiesAdapter extends FirebaseRecyclerAdapter<City, CitiesAdapter.CityViewHolder> {
        private CitiesAdapterListener listener;
        private boolean loaded = false;
        private String query = "";
        private int selectedPosition = -1;
        private final SortedList<City> cities = new SortedList<>(City.class, new SortedListAdapterCallback<City>(this) {
            @Override
            public int compare(City o1, City o2) {
                Collator collator = Collator.getInstance(new Locale(Utils.getLanguageIso()));
                collator.setStrength(Collator.PRIMARY);
                return collator.compare(o1.getLocalName(), o2.getLocalName());
            }

            @Override
            public boolean areContentsTheSame(City oldItem, City newItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(City item1, City item2) {
                return Objects.equals(item1.key, item2.key);
            }
        });

        void setCitiesAdapterListener(CitiesAdapterListener listener) {
            this.listener = listener;
        }

        CitiesAdapter(Class<City> modelClass, @LayoutRes int modelLayout, Class<CityViewHolder> viewHolderClass, Query query) {
            super(modelClass, modelLayout, viewHolderClass, query);
        }

        @Override
        protected void populateViewHolder(CityViewHolder viewHolder, City model, final int position) {
            RadioButton text = viewHolder.itemView.findViewById(R.id.text1);
            text.setText(model.getLocalName());
            text.setChecked(position == selectedPosition);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedPosition = position;
                    notifyDataSetChanged();
                    if (listener != null) listener.onCityPicked(getItem(position));
                }
            });
        }

        @Override
        public void onChildChanged(EventType type, DataSnapshot snapshot, int index, int oldIndex) {
            super.onChildChanged(type, snapshot, index, oldIndex);
            super.getItem(index).key = snapshot.getKey();
        }

        @Override
        public void onDataChanged() {
            super.onDataChanged();
            refreshFilter();
            if (listener != null && !loaded) {
                listener.onLoadingFinished();
                loaded = true;
            }
        }

        @Override
        public City getItem(int position) {
            return cities.get(position);
        }

        @Override
        public int getItemCount() {
            return cities.size();
        }

        void replaceAll(List<City> providers) {
            this.cities.beginBatchedUpdates();
            for (int i = this.cities.size() - 1; i >= 0; i--) {
                final City provider = this.cities.get(i);
                if (!providers.contains(provider)) {
                    this.cities.remove(provider);
                }
            }
            this.cities.addAll(providers);
            this.cities.endBatchedUpdates();
        }

        void filter(String query) {
            this.query = query;
            final String lowerCaseQuery = query.toLowerCase();

            final List<City> filteredModelList = new ArrayList<>();
            for (int i = 0; i < super.getItemCount(); i++) {
                City city = super.getItem(i);
                final String text = city.getLocalName().toLowerCase();
                if (text.contains(lowerCaseQuery)) filteredModelList.add(city);
            }
            replaceAll(filteredModelList);
        }

        void refreshFilter() {
            filter(query);
        }

        public static class CityViewHolder extends RecyclerView.ViewHolder {
            private CheckedTextView txtName;

            public CityViewHolder(View itemView) {
                super(itemView);
                txtName = itemView.findViewById(android.R.id.text1);
            }
        }

        interface CitiesAdapterListener {
            void onLoadingFinished();

            void onCityPicked(City city);
        }
    }

    public interface CityPickerDialogListener {
        void onCityPicked(City city);

        void onAllSelected();

        void onCancelled();
    }
}
