package hr.nas2skupa.eleventhhour.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;
import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.model.Provider;

/**
 * Created by nas2skupa on 22/11/2016.
 */

public class ProviderItem extends AbstractFlexibleItem<ProviderItem.ViewHolder> {
    private String providerKey;

    public ProviderItem(String providerKey) {
        this.providerKey = providerKey;
    }

    public String getProviderKey() {
        return providerKey;
    }

    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof ProviderItem) {
            ProviderItem providerItem = ((ProviderItem) inObject);
            return providerItem.getProviderKey().equals(providerKey);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return providerKey.hashCode();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_provider;
    }

    @Override
    public ViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        ViewHolder holder = new ViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
        return holder;
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ViewHolder holder, int position, List payloads) {
        holder.addProviderListener(providerKey);
    }

    public class ViewHolder extends FlexibleViewHolder {
        private DatabaseReference providerReference;
        private ProviderListener providerListener;
        private TextView txtProviderName;

        public ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);

            txtProviderName = (TextView) itemView.findViewById(R.id.txt_provider_name);
            providerListener = new ProviderListener(this);
        }

        public void addProviderListener(String providerKey) {
            removeProviderListener();
            providerReference = FirebaseDatabase.getInstance().getReference().child(providerKey.replace("_", "/"));
            providerReference.addValueEventListener(providerListener);
        }

        public void removeProviderListener() {
            if (providerReference != null && providerListener != null) {
                providerReference.removeEventListener(providerListener);
                providerListener = null;
                providerReference = null;
            }
        }
    }

    private static class ProviderListener implements ValueEventListener {
        WeakReference<ViewHolder> reference;

        public ProviderListener(ViewHolder reference) {
            this.reference = new WeakReference<>(reference);
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            ViewHolder holder = reference.get();
            if (holder == null) return;

            Provider provider = dataSnapshot.getValue(Provider.class);
            if (provider == null) return;

            holder.txtProviderName.setText(provider.getName());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
