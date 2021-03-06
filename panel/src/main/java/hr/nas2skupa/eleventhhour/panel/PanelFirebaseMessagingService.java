package hr.nas2skupa.eleventhhour.panel;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
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
import timber.log.Timber;

/**
 * Created by nas2skupa on 06/04/2017.
 */

public class PanelFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Timber.d("From: " + remoteMessage.getFrom());

        // User not signed in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        // No data in the message
        if (remoteMessage.getData() == null) return;

        if (remoteMessage.getData().containsKey("bookingUid")
                && remoteMessage.getData().containsKey("providerUid")) {
            String bookingKey = remoteMessage.getData().get("bookingUid");
            String providerKey = remoteMessage.getData().get("providerUid");

            // Not for this provider
            if (!providerKey.equals(MainActivity.providerKey)) return;

            DatabaseReference bookingReference = FirebaseDatabase.getInstance().getReference()
                    .child("providerAppointments")
                    .child(providerKey)
                    .child("data")
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
        intent.setAction(MainActivity.ACTION_PLANER);
        intent.putExtra("bookingKey", booking.key);
        intent.putExtra("providerKey", booking.providerId);
        intent.putExtra("userKey", booking.userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "eleventh_hour_panel")
                .setSmallIcon(R.drawable.ic_icon_panel)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        switch (booking.getStatus()) {
            case BookingStatus.PENDING:
                Intent serviceIntent = new Intent(this, PanelBookingService.class)
                        .putExtra("bookingKey", booking.key)
                        .putExtra("providerKey", booking.providerId)
                        .putExtra("userKey", booking.userId);

                builder.setContentTitle(getString(R.string.notification_title_new))
                        .setContentText(getString(R.string.notification_text_new, booking.userName, booking.serviceName, when))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .setBigContentTitle(booking.serviceName)
                                .bigText(getString(R.string.notification_text_new, booking.userName, booking.serviceName, when))
                                .setSummaryText(when))
                        .setColor(ContextCompat.getColor(PanelFirebaseMessagingService.this, R.color.primary))
                        .addAction(R.drawable.ic_clear_black_24dp, getString(R.string.notification_action_reject), PendingIntent.getService(
                                this,
                                id,
                                serviceIntent.putExtra("confirm", false),
                                PendingIntent.FLAG_UPDATE_CURRENT
                        ))
                        .addAction(R.drawable.ic_done_black_24dp, getString(R.string.notification_action_confirm), PendingIntent.getService(
                                this,
                                id * 2,
                                serviceIntent.putExtra("confirm", true),
                                PendingIntent.FLAG_UPDATE_CURRENT
                        ));

                break;
            case BookingStatus.USER_CANCELED:
                builder.setContentTitle(getString(R.string.notification_title_cancelled))
                        .setContentText(getString(R.string.notification_text_cancelled, booking.userName, booking.serviceName, when));
                break;
            default:
                return;
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }
}
