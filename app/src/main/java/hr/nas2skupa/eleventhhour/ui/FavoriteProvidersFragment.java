package hr.nas2skupa.eleventhhour.ui;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.sharedpreferences.Pref;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.common.Preferences;
import hr.nas2skupa.eleventhhour.common.utils.Utils;

/**
 * Created by nas2skupa on 03/12/2016.
 */
@EFragment(R.layout.fragment_providers)
public class FavoriteProvidersFragment extends ProvidersFragment {
    @Pref Preferences preferences;

    @Override
    public Query getKeyRef() {
        return FirebaseDatabase.getInstance().getReference()
                .child("providers")
                .child(preferences.country())
                .child("userFavorites")
                .child(Utils.getMyUid());
    }
}
