package hr.nas2skupa.eleventhhour.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nas2skupa on 22/10/2016.
 */

public class Booking {
    private String key;

    private String userId;
    private String providerId;
    private String serviceId;

    private String userName;
    private String providerName;
    private String serviceName;
    private String price;
    private long from;
    private long to;
    private String note;

    @BookingStatus
    private int status = BookingStatus.PENDING;

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUserId() {
        return userId;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getUserName() {
        return userName;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPrice() {
        return price;
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
        result.put("userName", userName);
        result.put("providerName", providerName);
        result.put("serviceName", serviceName);
        result.put("price", price);
        result.put("to", to);
        result.put("note", note);
        result.put("status", status);

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Booking) return ((Booking) obj).getKey().equals(key);
        return false;
    }
}
