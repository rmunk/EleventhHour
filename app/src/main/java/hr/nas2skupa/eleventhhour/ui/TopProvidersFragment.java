package hr.nas2skupa.eleventhhour.ui;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.EFragment;

import hr.nas2skupa.eleventhhour.R;

/**
 * Created by nas2skupa on 03/12/2016.
 */
@EFragment(R.layout.fragment_providers)
public class TopProvidersFragment extends ProvidersFragment {
    @Override
    public Query getKeyRef() {
        return FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country().get())
                .child("promoted");
    }
}
