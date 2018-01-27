package hr.nas2skupa.eleventhhour.ui;

import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.model.City;
import hr.nas2skupa.eleventhhour.common.ui.CityPickerDialog;
import hr.nas2skupa.eleventhhour.common.ui.CityPickerDialog_;

/**
 * Created by nas2skupa on 03/12/2016.
 */
@EFragment(R.layout.fragment_providers)
@OptionsMenu(R.menu.subcategory_providers)
public class SubcategoryProvidersFragment extends ProvidersFragment implements ValueEventListener {

    @OptionsMenuItem
    void menuPickCity(MenuItem item) {
        menuPickCity = item;
        menuPickCity.getIcon().setAlpha(preferences.city().exists() ? 255 : 138);
    }

    MenuItem menuPickCity;

    @Override
    public void onResume() {
        super.onResume();

        if (preferences.city().exists()) {
            FirebaseDatabase.getInstance().getReference()
                    .child("app/cities")
                    .child(preferences.country().get())
                    .child(preferences.city().get())
                    .addValueEventListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (preferences.city().exists()) {
            FirebaseDatabase.getInstance().getReference()
                    .child("app/cities")
                    .child(preferences.country().get())
                    .child(preferences.city().get())
                    .removeEventListener(this);
        }
    }

    @Override
    public Query getKeyRef() {
        if (preferences.city().exists()) {
            return FirebaseDatabase.getInstance().getReference()
                    .child("providers")
                    .child(preferences.country().get())
                    .child("bySubcategoryAndCity")
                    .child(subcategoryKey + preferences.city().get());
        } else {
            return FirebaseDatabase.getInstance().getReference()
                    .child("providers")
                    .child(preferences.country().get())
                    .child("bySubcategory")
                    .child(subcategoryKey);
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        City city = dataSnapshot.getValue(City.class);
        if (city == null) {
            preferences.city().remove();
            menuPickCity.setTitle(R.string.pick_city_all_cities);
            menuPickCity.getIcon().setAlpha(138);
        } else {
            menuPickCity.setTitle(city.getLocalName());
            menuPickCity.getIcon().setAlpha(255);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @OptionsItem(R.id.menu_pick_city)
    public void pickCity() {
        CityPickerDialog cityPickerDialog = CityPickerDialog_.builder()
                .selectedCityKey(preferences.city().get())
                .build();
        cityPickerDialog.setCityPickerDialogListener(new CityPickerDialog.CityPickerDialogListener() {
            @Override
            public void onCityPicked(City city) {
                preferences.city().put(city.key);
                adapter.stopListening();
                adapter = new ProvidersAdapter(filterSale, sortByName);
                recyclerView.swapAdapter(adapter, false);
                menuPickCity.setTitle(city.getLocalName());
                menuPickCity.getIcon().setAlpha(255);
            }

            @Override
            public void onAllSelected() {
                preferences.city().remove();
                adapter.stopListening();
                adapter = new ProvidersAdapter(filterSale, sortByName);
                recyclerView.swapAdapter(adapter, false);
                menuPickCity.setTitle(R.string.pick_city_all_cities);
                menuPickCity.getIcon().setAlpha(138);
            }

            @Override
            public void onCancelled() {

            }
        });
        cityPickerDialog.show(getChildFragmentManager(), "CityPickerDialog");
    }
}
