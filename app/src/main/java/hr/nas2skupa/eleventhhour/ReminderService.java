package hr.nas2skupa.eleventhhour;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.common.model.BookingStatus;
import hr.nas2skupa.eleventhhour.ui.MainActivity;
import hr.nas2skupa.eleventhhour.ui.MainActivity_;

/**
 * Created by nas2skupa on 02/10/2017.
 */

public class ReminderService extends IntentService {
    public ReminderService() {
        super("ReminderService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null || !intent.hasExtra("bookingKey")
                || !intent.hasExtra("providerKey") || !intent.hasExtra("userKey")) return;

        // User not signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String bookingKey = intent.getStringExtra("bookingKey");
        String userKey = intent.getStringExtra("userKey");

        // Not for this user
        if (!userKey.equals(user.getUid())) return;

        DatabaseReference bookingReference = FirebaseDatabase.getInstance().getReference()
                .child("userAppointments")
                .child(userKey)
                .child("data")
                .child(bookingKey);
        bookingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Booking booking = dataSnapshot.getValue(Booking.class);
                    if (booking != null && booking.status == BookingStatus.PROVIDER_ACCEPTED) {
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

    private void sendNotification(Booking booking) {
        final int id = booking.key.hashCode();
        final CharSequence when = DateUtils.getRelativeTimeSpanString(this, booking.from, true);
        final String title = getString(R.string.alarm_notification_title, booking.providerName);
        final String text = getString(R.string.alarm_notification_text, booking.serviceName, when);
        final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = MainActivity_.intent(this)
                .action(MainActivity.ACTION_CALENDAR)
                .extra("bookingKey", booking.key)
                .extra("providerKey", booking.providerId)
                .extra("userKey", booking.userId)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).get();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "eleventh_hour_client")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(title)
                        .bigText(text));

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }
}
