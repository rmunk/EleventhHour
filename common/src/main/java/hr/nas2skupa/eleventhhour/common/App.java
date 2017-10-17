package hr.nas2skupa.eleventhhour.common;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseException;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.FirebaseDatabase;

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
            Timber.plant(new CrashReportingTree());
        }
    }

    /**
     * A tree which logs important information for crash reporting.
     */
    private static class CrashReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }
            FirebaseCrash.logcat(priority, tag, message);
            if (t != null) FirebaseCrash.report(t);
            else if (priority == Log.ERROR) FirebaseCrash.report(new FirebaseException(message));
        }
    }
}
