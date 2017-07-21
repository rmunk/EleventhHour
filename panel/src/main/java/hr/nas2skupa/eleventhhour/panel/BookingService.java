package hr.nas2skupa.eleventhhour.panel;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import hr.nas2skupa.eleventhhour.common.model.BookingStatus;
import timber.log.Timber;

/**
 * Created by nas2skupa on 11/04/2017.
 */

public class BookingService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!intent.hasExtra("bookingKey")
                || !intent.hasExtra("providerKey")
                || !intent.hasExtra("userKey")
                || !intent.hasExtra("confirm")) {
            return START_NOT_STICKY;
        }

        String bookingKey = intent.getStringExtra("bookingKey");
        String providerKey = intent.getStringExtra("providerKey");
        String userKey = intent.getStringExtra("userKey");
        final boolean confirm = intent.getBooleanExtra("confirm", false);
        int status = confirm ? BookingStatus.PROVIDER_ACCEPTED : BookingStatus.PROVIDER_REJECTED;

        Timber.d("bookingKey: %s, providerKey: %s, userKey: %s, confirm: %s", bookingKey, providerKey, userKey, confirm);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/bookings/" + providerKey + "/" + bookingKey + "/status", status);
        childUpdates.put("/users/" + userKey + "/bookings/" + bookingKey + "/status", status);
        reference.updateChildren(childUpdates).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(BookingService.this, String.format("Appointment could not be %s.", confirm ? "confirmed" : "rejected"), Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(bookingKey.hashCode());

        return START_NOT_STICKY;
    }
}
