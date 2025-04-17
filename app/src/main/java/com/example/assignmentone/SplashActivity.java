package com.example.assignmentone;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 4000; // 4 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make fullscreen and hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        // Initialize views
        ImageView logo = findViewById(R.id.splash_logo);
        TextView appName = findViewById(R.id.splash_app_name);
        TextView slogan = findViewById(R.id.splash_slogan);

        // Set custom elegant font with fallback
        try {
            // Option 1: Use system serif font (built-in)
            Typeface typeface = Typeface.create("serif", Typeface.BOLD);

            // Option 2: If you add a custom font later, use this instead:
            // Typeface typeface = ResourcesCompat.getFont(this, R.font.your_custom_font);

            if (typeface != null) {
                appName.setTypeface(typeface);
                slogan.setTypeface(typeface);
            } else {
                // Fallback to system default
                appName.setTypeface(Typeface.DEFAULT_BOLD);
                slogan.setTypeface(Typeface.DEFAULT);
            }
        } catch (Exception e) {
            // Use default typeface if any error occurs
            appName.setTypeface(Typeface.DEFAULT_BOLD);
            slogan.setTypeface(Typeface.DEFAULT);
        }

        // Load animations with null checks
        Animation fadeIn = safeLoadAnimation(R.anim.fade_in);
        Animation slideUp = safeLoadAnimation(R.anim.slide_up);
        Animation pulse = safeLoadAnimation(R.anim.pulse);

        // Set animations with different start offsets
        if (fadeIn != null) logo.startAnimation(fadeIn);
        if (slideUp != null) {
            appName.startAnimation(slideUp);
            slogan.startAnimation(slideUp);
        }
        if (pulse != null) logo.startAnimation(pulse);

        // Delayed transition with elegant fade out
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);

            // Use default transition if custom animations fail
            try {
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            } catch (Exception e) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

            finish();
        }, SPLASH_DURATION);
    }

    /**
     * Safely loads animation with null check
     */
    private Animation safeLoadAnimation(int animResId) {
        try {
            return AnimationUtils.loadAnimation(this, animResId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        // Clean up animations to prevent memory leaks
        ImageView logo = findViewById(R.id.splash_logo);
        TextView appName = findViewById(R.id.splash_app_name);
        TextView slogan = findViewById(R.id.splash_slogan);

        if (logo != null) logo.clearAnimation();
        if (appName != null) appName.clearAnimation();
        if (slogan != null) slogan.clearAnimation();

        super.onDestroy();
    }
}