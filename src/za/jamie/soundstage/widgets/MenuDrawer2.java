package za.jamie.soundstage.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import za.jamie.soundstage.R;

/**
 * Created by jamie on 2013/12/01.
 */
public class MenuDrawer2 extends SlidingPaneLayout {

    private View mMenuView;
    private int mMenuWidth = LayoutParams.MATCH_PARENT;

    public MenuDrawer2(Context context) {
        super(context);
    }

    public MenuDrawer2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuDrawer2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public static MenuDrawer2 attach(Activity activity) {
        MenuDrawer2 drawer = createMenuDrawer(activity);
        attachToDecor(activity, drawer);
        return drawer;
    }

    private static MenuDrawer2 createMenuDrawer(Activity activity) {
        MenuDrawer2 drawer = new MenuDrawer2(activity);
        drawer.setId(R.id.md2__drawer);
        return drawer;
    }

    private static void attachToDecor(Activity activity, MenuDrawer2 drawer) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decorView.getChildAt(0);

        decorView.removeAllViews();
        decorView.addView(drawer, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        drawer.addView(decorChild, decorChild.getLayoutParams());
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        if (mMenuView != null) {
            mMenuView.setPadding(0, insets.top, 0, 0);
        }
        return super.fitSystemWindows(insets);
    }

    /**
     * Set the menu view from a layout resource.
     *
     * @param layoutResId Resource ID to be inflated.
     */
    public void setMenuView(int layoutResId) {
        mMenuView = LayoutInflater.from(getContext()).inflate(layoutResId, this, false);
        addView(mMenuView, 0, new LayoutParams(mMenuWidth, LayoutParams.MATCH_PARENT));
    }

    public void setMenuWidth(int width) {
        if (width != mMenuWidth) {
            mMenuWidth = width;
            if (mMenuView != null) {
                mMenuView.setLayoutParams(new LayoutParams(width, LayoutParams.MATCH_PARENT));
            }
        }
    }

}
