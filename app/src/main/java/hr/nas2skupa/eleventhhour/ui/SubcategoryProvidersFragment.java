package hr.nas2skupa.eleventhhour.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.EFragment;

import java.util.Objects;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.model.City;
import hr.nas2skupa.eleventhhour.common.ui.CityPickerDialog;
import hr.nas2skupa.eleventhhour.common.ui.CityPickerDialog_;

/**
 * Created by nas2skupa on 03/12/2016.
 */
@EFragment(R.layout.fragment_providers)
public class SubcategoryProvidersFragment extends ProvidersFragment implements View.OnClickListener, CityPickerDialog.CityPickerDialogListener, ValueEventListener {

    private ActionBar actionBar;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.pick_city_all_cities);
            FirebaseDatabase.getInstance().getReference()
                    .child("app/cities")
                    .child(preferences.country().get())
                    .child(preferences.city().get())
                    .addListenerForSingleValueEvent(this);
        }

        getActivity().findViewById(R.id.toolbar).setOnClickListener(this);
    }

    @Override
    public Query getKeyRef() {
        if (Objects.equals(preferences.city().get(), "all")) {
            return FirebaseDatabase.getInstance().getReference()
                    .child("providers")
                    .child(preferences.country().get())
                    .child("bySubcategory")
                    .child(subcategoryKey);
        }

        else {
            return FirebaseDatabase.getInstance().getReference()
                    .child("providers")
                    .child(preferences.country().get())
                    .child("bySubcategoryAndCity")
                    .child(subcategoryKey + preferences.city().get());
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        actionBar.setDisplayShowTitleEnabled(true);
        City city = dataSnapshot.getValue(City.class);
        if (city != null) actionBar.setTitle(city.getLocalName());
        else preferences.city().put("all");
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
    
    @Override
    public void onClick(View view) {
        CityPickerDialog cityPickerDialog = CityPickerDialog_.builder().build();
        cityPickerDialog.setCityPickerDialogListener(this);
        cityPickerDialog.show(getChildFragmentManager(), "CityPickerDialog");
    }

    @Override
    public void onCityPicked(City city) {
        if (actionBar != null) actionBar.setTitle(city.getLocalName());
        preferences.city().put(city.key);
        adapter.cleanup();
        adapter = new ProvidersAdapter(filterSale, sortByName);
        recyclerView.swapAdapter(adapter, false);
    }

    @Override
    public void onAllSelected() {
        if (actionBar != null) actionBar.setTitle(R.string.pick_city_all_cities);
        preferences.city().put("all");
        adapter.cleanup();
        adapter = new ProvidersAdapter(filterSale, sortByName);
        recyclerView.swapAdapter(adapter, false);
    }

    @Override
    public void onCancelled() {

    }
}
