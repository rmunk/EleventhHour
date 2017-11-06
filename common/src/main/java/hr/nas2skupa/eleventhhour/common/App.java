package hr.nas2skupa.eleventhhour.common;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.perf.FirebasePerformance;

import net.danlew.android.joda.JodaTimeAndroid;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Created by nas2skupa on 06/04/2017.
 */

public class App extends Application {

    private static Context appContext;

    public static Context getAppContext() {
        return appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        appContext = getApplicationContext();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Fabric.with(this, new Crashlytics());
            FirebasePerformance.getInstance().setPerformanceCollectionEnabled(true);
            Timber.plant(new CrashReportingTree());
        }

        JodaTimeAndroid.init(this);
    }

    /**
     * A tree which logs important information for crash reporting.
     */
    private static class CrashReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (t != null) Crashlytics.logException(t);
            else if (priority == Log.ERROR) Crashlytics.logException(new RuntimeException(message));
            else if (priority >= Log.INFO) Crashlytics.log(priority, tag, message);
        }
    }
}
