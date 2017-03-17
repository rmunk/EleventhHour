package hr.nas2skupa.eleventhhour.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by nas2skupa on 12/03/2017.
 */
public class Location {
    public double latitude;
    public double longitude;

    public Location() {
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(LatLng latLng) {
        latitude = latLng.latitude;
        longitude = latLng.longitude;
    }

    @Override
    public String toString() {
        String output, degrees, minutes, seconds, direction;

        double decimal = latitude;

        direction = "N";
        if (decimal < 0) {
            decimal *= -1;
            direction = "S";
        }

        double mod = decimal % 1;
        degrees = String.valueOf((int) decimal);

        decimal = mod * 60;
        mod = decimal % 1;
        minutes = String.valueOf((int) decimal);

        decimal = mod * 60;
        seconds = String.valueOf((int) decimal);

        output = degrees + "°" + minutes + "'" + seconds + "\"" + direction;

        decimal = longitude;

        direction = "E";
        if (decimal < 0) {
            decimal *= -1;
            direction = "W";
        }

        mod = decimal % 1;
        degrees = String.valueOf((int) decimal);

        decimal = mod * 60;
        mod = decimal % 1;
        minutes = String.valueOf((int) decimal);

        decimal = mod * 60;
        seconds = String.valueOf((int) decimal);

        output += " " + degrees + "°" + minutes + "'" + seconds + "\"" + direction;

        return output;
    }
}
