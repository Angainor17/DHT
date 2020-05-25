package cx.ring.client;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import cx.ring.R;
import cx.ring.application.JamiApplication;
import cx.ring.databinding.ActivityConversationBinding;
import cx.ring.fragments.ConversationFragment;
import cx.ring.interfaces.Colorable;
import cx.ring.utils.ConversationPath;

public class ConversationActivity extends AppCompatActivity implements Colorable {

    private ConversationFragment mConversationFragment;
    private ConversationPath conversationPath = null;

    private Intent mPendingIntent = null;
    private ActivityConversationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JamiApplication.getInstance().startDaemon();

        binding = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.mainToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                decorView.getSystemUiVisibility()
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarLayout, (v, insets) -> {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) binding.toolbarLayout.getLayoutParams();
            params.topMargin = insets.getSystemWindowInsetTop();
            binding.toolbarLayout.setLayoutParams(params);
            insets.consumeSystemWindowInsets();
            return insets;
        });

        Intent intent = getIntent();
        String action = intent == null ? null : intent.getAction();

        if (intent != null) {
            conversationPath = ConversationPath.fromIntent(intent);
        } else if (savedInstanceState != null) {
            conversationPath = ConversationPath.fromBundle(savedInstanceState);
        }
        if (mConversationFragment == null) {
            mConversationFragment = new ConversationFragment();
            mConversationFragment.setArguments(conversationPath.toBundle());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, mConversationFragment, null)
                    .commit();
        }
        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action) || Intent.ACTION_VIEW.equals(action)) {
            mPendingIntent = intent;
        }
    }

    @Override
    public void onContextMenuClosed(@NonNull Menu menu) {
        mConversationFragment.updateAdapterItem();
        super.onContextMenuClosed(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPendingIntent != null) {
            handleShareIntent(mPendingIntent);
            mPendingIntent = null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleShareIntent(intent);
    }

    private void handleShareIntent(Intent intent) {
        if (mConversationFragment != null)
            mConversationFragment.handleShareIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        conversationPath.toBundle(outState);
        super.onSaveInstanceState(outState);
    }

    public void setColor(@ColorInt int color) {
        colouriseToolbar(binding.mainToolbar, color);
        //mToolbar.setBackground(new ColorDrawable(color));
        //getWindow().setStatusBarColor(color);
    }

    public static void colouriseToolbar(Toolbar toolbar, @ColorInt int foreground) {
        final PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(foreground, PorterDuff.Mode.MULTIPLY);

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            final View view = toolbar.getChildAt(i);
            if (view instanceof ImageButton) {
                ((ImageButton) view).getDrawable().setColorFilter(colorFilter);
            } else if (view instanceof ActionMenuView) {
                for (int j = 0; j < ((ActionMenuView) view).getChildCount(); j++) {
                    final View innerView = ((ActionMenuView) view).getChildAt(j);
                    //Any ActionMenuViews - icons that are not back button, text or overflow menu
                    if (innerView instanceof ActionMenuItemView) {
                        final Drawable[] drawables = ((ActionMenuItemView) innerView).getCompoundDrawables();
                        for (int k = 0; k < drawables.length; k++) {
                            final Drawable drawable = drawables[k];
                            if (drawable != null) {
                                final int drawableIndex = k;
                                //Set the color filter in separate thread
                                //by adding it to the message queue - won't work otherwise
                                innerView.post(() -> ((ActionMenuItemView) innerView).getCompoundDrawables()[drawableIndex].setColorFilter(colorFilter));
                            }
                        }
                    }
                }
            }
        }

        Drawable overflowIcon = toolbar.getOverflowIcon();
        if (overflowIcon != null) {
            overflowIcon.setColorFilter(colorFilter);
            toolbar.setOverflowIcon(overflowIcon);
        }
    }
}
