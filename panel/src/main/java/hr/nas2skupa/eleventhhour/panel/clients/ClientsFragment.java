package hr.nas2skupa.eleventhhour.panel.clients;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import hr.nas2skupa.eleventhhour.common.model.User;
import hr.nas2skupa.eleventhhour.common.ui.user.UsersFragment;

/**
 * Created by nas2skupa on 12/10/2017.
 */

@EFragment(resName = "fragment_users")
public class ClientsFragment extends UsersFragment {
    @FragmentArg String providerKey;

    @Override
    public Query getKeyRef() {
        return FirebaseDatabase.getInstance().getReference()
                .child("users/providerClients")
                .child(providerKey);
    }

    @Override
    protected void onUserSelected(User user) {
        ClientDetailsActivity_.intent(getContext()).userKey(user.key).start();
    }
}
