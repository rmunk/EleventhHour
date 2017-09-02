package hr.nas2skupa.eleventhhour.common.ui.helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

/**
 * Created by nas2skupa on 12/03/2017.
 */

public class DelayedProgressDialog extends ProgressDialog {
    private static Handler dialogHandler;
    private Runnable runner;

    static {
        dialogHandler = new Handler();
    }

    public DelayedProgressDialog(Context context) {
        super(context);
    }

    public static ProgressDialog show(Context c, CharSequence title, CharSequence msg, long afterDelayMilliSec) {
        final DelayedProgressDialog pd = new DelayedProgressDialog(c);
        pd.setTitle(title);
        pd.setMessage(msg);
        pd.setCancelable(true);
        pd.runner = new Runnable() {

            public void run() {
                try {
                    pd.show();
                } catch (Exception e) {
                    /* do nothing */
                }
            }
        };
        dialogHandler.postDelayed(pd.runner, afterDelayMilliSec);
        return pd;
    }

    @Override
    public void cancel() {
        dialogHandler.removeCallbacks(runner);
        super.cancel();
    }
}