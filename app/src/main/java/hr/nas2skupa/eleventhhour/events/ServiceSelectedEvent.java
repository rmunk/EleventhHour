package hr.nas2skupa.eleventhhour.events;

/**
 * Created by nas2skupa on 19/10/2016.
 */
public class ServiceSelectedEvent {
    private String serviceKey;

    public ServiceSelectedEvent(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getServiceKey() {
        return serviceKey;
    }
}
