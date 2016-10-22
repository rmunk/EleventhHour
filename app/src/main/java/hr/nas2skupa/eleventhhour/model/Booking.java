package hr.nas2skupa.eleventhhour.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nas2skupa on 22/10/2016.
 */

public class Booking {
    private String userId;
    private String providerId;
    private String serviceId;

    private long from;
    private long to;
    private String note;

    @BookingStatus
    private int status;

    public Booking() {
    }

    public Booking(String userId, String providerId, String serviceId, long from, long to, String note, int status) {
        this.userId = userId;
        this.providerId = providerId;
        this.serviceId = serviceId;
        this.from = from;
        this.to = to;
        this.note = note;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public String getNote() {
        return note;
    }

    @BookingStatus
    public int getStatus() {
        return status;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("providerId", providerId);
        result.put("serviceId", serviceId);
        result.put("from", from);
        result.put("to", to);
        result.put("note", note);
        result.put("status", status);

        return result;
    }
}
