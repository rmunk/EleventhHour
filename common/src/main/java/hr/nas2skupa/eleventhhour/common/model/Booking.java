package hr.nas2skupa.eleventhhour.common.model;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by nas2skupa on 22/10/2016.
 */

public class Booking {
    @Exclude public String key;

    public String userId;
    public String providerId;
    public String serviceId;

    public String userName;
    public String providerName;
    public String serviceName;

    public String price;
    public long from;
    public long to;
    public String note;

    @BookingStatus
    public int status = BookingStatus.PENDING;

    public Booking() {
    }

    public Booking(String userId, String providerId, String serviceId, String userName, String providerName, String serviceName, String price, long from, long to, String note) {
        this.userId = userId;
        this.providerId = providerId;
        this.serviceId = serviceId;
        this.userName = userName;
        this.providerName = providerName;
        this.serviceName = serviceName;
        this.price = price;
        this.from = from;
        this.to = to;
        this.note = note;
    }

    @Exclude
    public String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return String.format(Locale.getDefault(), "%s - %s", format.format(from), format.format(to));
    }

    @Exclude
    @BookingStatus
    public int getStatus() {
        if (to > new Date().getTime()) return status;
        else return BookingStatus.FINISHED;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("providerId", providerId);
        map.put("serviceId", serviceId);

        map.put("userName", userName);
        map.put("providerName", providerName);
        map.put("serviceName", serviceName);

        map.put("price", price);
        map.put("from", from);
        map.put("to", to);
        map.put("note", note);

        map.put("status", status);
        return map;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Booking && key != null && key.equals(((Booking) obj).key);
    }
}
