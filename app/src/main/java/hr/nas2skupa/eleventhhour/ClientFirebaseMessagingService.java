package hr.nas2skupa.eleventhhour;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.util.Date;

import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.common.model.BookingStatus;
import hr.nas2skupa.eleventhhour.ui.MainActivity;
import hr.nas2skupa.eleventhhour.ui.MainActivity_;
import timber.log.Timber;

/**
 * Created by nas2skupa on 06/04/2017.
 */

public class ClientFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Timber.d("From: " + remoteMessage.getFrom());

        // User not signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // No data in the message
        if (remoteMessage.getData() == null) return;

        if (remoteMessage.getData().containsKey("bookingUid")
                && remoteMessage.getData().containsKey("userUid")) {
            String bookingKey = remoteMessage.getData().get("bookingUid");
            String userKey = remoteMessage.getData().get("userUid");

            // Not for this user
            if (!userKey.equals(user.getUid())) return;

            DatabaseReference bookingReference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userKey)
                    .child("bookings")
                    .child(bookingKey);
            bookingReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Booking booking = dataSnapshot.getValue(Booking.class);
                        if (booking != null) {
                            booking.key = dataSnapshot.getKey();
                            sendNotification(booking);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void sendNotification(Booking booking) {
        final int id = booking.key.hashCode();
        final String when = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(new Date(booking.from));

        Intent intent = new Intent(this, MainActivity_.class);
        intent.setAction(MainActivity.ACTION_CALENDAR);
        intent.putExtra("bookingKey", booking.key);
        intent.putExtra("providerKey", booking.providerId);
        intent.putExtra("userKey", booking.userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        switch (booking.getStatus()) {
            case BookingStatus.PROVIDER_ACCEPTED:
                builder.setContentTitle(getString(R.string.notification_title_accepted))
                        .setContentText(getString(R.string.notification_text_accepted, booking.providerName, booking.serviceName, when));
                break;
            case BookingStatus.PROVIDER_REJECTED:
                builder.setContentTitle(getString(R.string.notification_title_rejected))
                        .setContentText(getString(R.string.notification_text_rejected, booking.providerName, booking.serviceName, when));
                break;
            case BookingStatus.PROVIDER_CANCELED:
                builder.setContentTitle(getString(R.string.notification_title_cancelled))
                        .setContentText(getString(R.string.notification_text_cancelled, booking.providerName, booking.serviceName, when));
                break;
            default:
                return;
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }
}
