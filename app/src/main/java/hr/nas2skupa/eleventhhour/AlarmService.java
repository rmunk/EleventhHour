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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import hr.nas2skupa.eleventhhour.common.model.Booking;
import hr.nas2skupa.eleventhhour.common.model.BookingStatus;
import hr.nas2skupa.eleventhhour.ui.MainActivity;
import hr.nas2skupa.eleventhhour.ui.MainActivity_;

/**
 * Created by nas2skupa on 02/10/2017.
 */

public class AlarmService extends IntentService {
    public AlarmService() {
        super("AlarmService");
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
        final String when = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(booking.from));

        Intent intent = MainActivity_.intent(this)
                .action(MainActivity.ACTION_CALENDAR)
                .extra("bookingKey", booking.key)
                .extra("providerKey", booking.providerId)
                .extra("userKey", booking.userId)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).get();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "eleventh_hour_client")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentTitle(booking.providerName)
                .setContentText(getString(R.string.alarm_notification_text, booking.serviceName, when))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle("Appointment reminder")
                        .bigText(getString(R.string.alarm_notification_details, booking.serviceName, when, booking.providerName)));

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }
}
