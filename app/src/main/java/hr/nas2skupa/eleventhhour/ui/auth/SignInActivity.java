package hr.nas2skupa.eleventhhour.ui.auth;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import hr.nas2skupa.eleventhhour.R;
import hr.nas2skupa.eleventhhour.events.AuthSuccessfulEvent;
import hr.nas2skupa.eleventhhour.ui.MainActivity;
import hr.nas2skupa.eleventhhour.ui.MainActivity_;


@EActivity(R.layout.activity_sign_in)
public class SignInActivity extends FragmentActivity {
    private static final String TAG = "SignInActivity";
    public static final String ACTION_SIGN_OUT = "hr.nas2skupa.eleventhhour.SIGN_OUT";

    private static final long ANIMATION_DELAY = 1000;
    private static final long ANIMATION_DURATION = 500;

    @ViewById(R.id.layout_main)
    FrameLayout layoutMain;
    @ViewById(R.id.layout_content)
    RelativeLayout layoutContent;
    @ViewById(R.id.image_splash_background)
    ImageView imageSplashBackground;
    @ViewById(R.id.image_splash_overlay)
    ImageView imageSplashOverlay;
    @ViewById(R.id.text_sign_in_message)
    TextView textSignInMessage;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getAction().equals(ACTION_SIGN_OUT)) {
            FirebaseAuth.getInstance().signOut();
        }
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) EventBus.getDefault().postSticky(new AuthSuccessfulEvent(user));
                else showSignInFragment();
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }

        EventBus.getDefault().unregister(this);
    }

    @AfterViews
    public void afterViews() {
        if (getIntent().getAction().equals(ACTION_SIGN_OUT))
            textSignInMessage.setText(R.string.sign_in_signing_out);
        layoutMain.post(new Runnable() {
            @Override
            public void run() {
                onLayoutMainReady();
            }
        });
    }

    @MainThread
    public void onLayoutMainReady() {
        setImageViewTopCrop(imageSplashBackground);
        setImageViewTopCrop(imageSplashOverlay);

        auth.addAuthStateListener(authListener);
    }

    /**
     * Sets the ImageView scale type to crop top
     *
     * @param view image view to crop
     */
    private void setImageViewTopCrop(ImageView view) {
        float scaleX = ((float) view.getWidth()) / view.getDrawable().getIntrinsicWidth();
        float scaleY = ((float) view.getHeight()) / view.getDrawable().getIntrinsicHeight();
        float scale = Math.max(scaleX, scaleY);
        Matrix matrix = view.getImageMatrix();
        matrix.setScale(scale, scale, 0, 0);
        view.setScaleType(ImageView.ScaleType.MATRIX);
        view.setImageMatrix(matrix);
    }

    private void animateHeader() {
        // Splash overlay image image is 50% longer so it can be animated up
        final float scaleX = ((float) layoutMain.getWidth()) / imageSplashOverlay.getDrawable().getIntrinsicWidth();
        final float scaleY = ((float) layoutMain.getHeight()) / imageSplashOverlay.getDrawable().getIntrinsicHeight();
        final float scale = Math.max(scaleX, scaleY);

        final int imageMainHeight = (int) (imageSplashOverlay.getDrawable().getIntrinsicHeight() * (2f / 3) * scale);
        final float headerHeight = getResources().getDimension(R.dimen.sign_in_header_hight);
        final int initialHeight = imageSplashOverlay.getHeight();

        ValueAnimator valueAnimator = ObjectAnimator.ofInt(0, (int) ((imageMainHeight - headerHeight) / 2));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageSplashOverlay.getLayoutParams();
                layoutParams.height = initialHeight + val;
                imageSplashOverlay.setTranslationY(-val);
                imageSplashOverlay.setLayoutParams(layoutParams);
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setStartDelay(ANIMATION_DELAY);
        valueAnimator.setDuration(ANIMATION_DURATION);
        valueAnimator.start();
    }

    private void showSignInFragment() {
        if (getSupportFragmentManager().findFragmentByTag("SignInFragment_") == null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SignInFragment_(), "SignInFragment_")
                    .commitAllowingStateLoss();

        textSignInMessage.animate()
                .alpha(0)
                .setStartDelay(ANIMATION_DELAY)
                .setDuration(0)
                .start();

        animateHeader();

        layoutContent.animate()
                .alpha(1)
                .setStartDelay(ANIMATION_DELAY + ANIMATION_DURATION)
                .setDuration(ANIMATION_DURATION)
                .start();
    }

    @Subscribe(sticky = true)
    public void onAuthSuccess(final AuthSuccessfulEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        MainActivity_.intent(SignInActivity.this)
                .action(MainActivity.HOME)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                .start();
    }
}
