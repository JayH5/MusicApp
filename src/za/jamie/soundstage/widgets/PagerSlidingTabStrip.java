/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.jamie.soundstage.widgets;

import za.jamie.soundstage.R;
import za.jay.IcsViewPager.ViewPager;
import za.jay.IcsViewPager.ViewPager.OnPageChangeListener;
import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PagerSlidingTabStrip extends HorizontalScrollView {

	private static final int[] ATTRS = new int[] {
		android.R.attr.textSize,
		android.R.attr.textColor,
		android.R.attr.fontFamily,
		android.R.attr.textStyle,
		android.R.attr.typeface
    };
	
	private static final int SANS = 1;
    private static final int SERIF = 2;
    private static final int MONOSPACE = 3;

	private LinearLayout.LayoutParams defaultTabLayoutParams;
	private LinearLayout.LayoutParams expandedTabLayoutParams;

	private final PageListener pageListener = new PageListener();
	public OnPageChangeListener delegatePageListener;

	private LinearLayout tabsContainer;
	private ViewPager pager;

	private int tabCount;

	private int currentPosition = 0;
	private float currentPositionOffset = 0f;

	private Paint indicatorPaint;
	private Paint underlinePaint;
	private Paint dividerPaint;

	private boolean checkedTabWidths = false;

	private boolean shouldExpand = false;
	private boolean textAllCaps = true;

	private int scrollOffset = 52;
	private int indicatorHeight = 8;
	private int underlineHeight = 2;
	private int dividerPadding = 12;
	private int tabPadding = 24;
	private int dividerWidth = 1;

	private int tabTextSize = 12;
	private int tabTextColor = 0xFF666666;
	private int tabSelectedTextColor = 0xFF333333;
	private Typeface tabTypeface = null;
	private int tabStyleIndex = Typeface.NORMAL;

	private int lastScrollX = 0;
	
	private final ArgbEvaluator tabTextColorEvaluator = new ArgbEvaluator();

	private int tabBackgroundResId = R.drawable.background_tab;

	public PagerSlidingTabStrip(Context context) {
		this(context, null);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setFillViewport(true);
		setWillNotDraw(false);

		tabsContainer = new LinearLayout(context);
		tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
		tabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addView(tabsContainer);

		DisplayMetrics dm = getResources().getDisplayMetrics();

		scrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset, dm);
		indicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indicatorHeight, dm);
		underlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, underlineHeight, dm);
		dividerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerPadding, dm);
		tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
		dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dividerWidth, dm);
		tabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);

		// get system attrs (android:textSize and android:textColor)

		int typefaceIndex = SANS;
		String tabFontFamily = null;
		TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

		tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
		tabTextColor = a.getColor(1, tabTextColor);
		tabFontFamily = a.getString(2);
		tabStyleIndex = a.getInt(3, typefaceIndex);
		typefaceIndex = a.getInt(4, typefaceIndex);

		a.recycle();
		
		setTypefaceFromAttrs(tabFontFamily, typefaceIndex, tabStyleIndex);

		// get custom attrs

		int indicatorColor = 0xFF666666;
		int underlineColor = 0x1A000000;
		int dividerColor = 0x1A000000;
		
		a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip);

		indicatorColor = a.getColor(R.styleable.PagerSlidingTabStrip_indicatorColor, indicatorColor);
		underlineColor = a.getColor(R.styleable.PagerSlidingTabStrip_underlineColor, underlineColor);
		dividerColor = a.getColor(R.styleable.PagerSlidingTabStrip_dividerColor, dividerColor);
		indicatorHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_indicatorHeight, indicatorHeight);
		underlineHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_underlineHeight, underlineHeight);
		dividerPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_dividerPadding, dividerPadding);
		tabPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_tabPaddingLeftRight, tabPadding);
		tabBackgroundResId = a.getResourceId(R.styleable.PagerSlidingTabStrip_tabBackground, tabBackgroundResId);
		shouldExpand = a.getBoolean(R.styleable.PagerSlidingTabStrip_shouldExpand, shouldExpand);
		scrollOffset = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_scrollOffset, scrollOffset);
		textAllCaps = a.getBoolean(R.styleable.PagerSlidingTabStrip_textAllCaps, textAllCaps);
		tabSelectedTextColor = a.getColor(R.styleable.PagerSlidingTabStrip_selectedTextColor, tabSelectedTextColor);

		a.recycle();

		indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		indicatorPaint.setStyle(Style.FILL);
		indicatorPaint.setColor(indicatorColor);
		
		underlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		underlinePaint.setStyle(Style.FILL);
		underlinePaint.setColor(underlineColor);
		
		dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dividerPaint.setStrokeWidth(dividerWidth);
		dividerPaint.setColor(dividerColor);

		defaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
	}
	
	private void setTypefaceFromAttrs(String familyName, int typefaceIndex, int styleIndex) {
        Typeface tf = null;
        if (familyName != null) {
            tf = Typeface.create(familyName, styleIndex);
            if (tf != null) {
                setTypeface(tf);
                return;
            }
        }
        switch (typefaceIndex) {
            case SANS:
                tf = Typeface.SANS_SERIF;
                break;

            case SERIF:
                tf = Typeface.SERIF;
                break;

            case MONOSPACE:
                tf = Typeface.MONOSPACE;
                break;
        }
        setTypeface(tf, styleIndex);
    }

	public void setViewPager(ViewPager pager) {
		this.pager = pager;

		if (pager.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		}

		pager.setOnPageChangeListener(pageListener);

		notifyDataSetChanged();
	}

	public void setOnPageChangeListener(OnPageChangeListener listener) {
		this.delegatePageListener = listener;
	}

	public void notifyDataSetChanged() {
		tabsContainer.removeAllViews();

		tabCount = pager.getAdapter().getCount();

		for (int i = 0; i < tabCount; i++) {
			addTab(i, pager.getAdapter().getPageTitle(i).toString());
		}

		updateTabStyles();

		checkedTabWidths = false;

		getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeOnGlobalLayoutListener(this);

				currentPosition = pager.getCurrentItem();
				scrollToChild(currentPosition, 0);
			}
		});
	}

	private void addTab(final int position, String title) {
		TextView tab = new TextView(getContext());
		tab.setText(title);
		tab.setFocusable(true);
		tab.setGravity(Gravity.CENTER);
		tab.setSingleLine();

		tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pager.setCurrentItem(position);
			}
		});

		tabsContainer.addView(tab);
	}

	private void updateTabStyles() {
		for (int i = 0; i < tabCount; i++) {
			TextView tab = (TextView) tabsContainer.getChildAt(i);

			tab.setLayoutParams(defaultTabLayoutParams);
			tab.setBackgroundResource(tabBackgroundResId);
			if (shouldExpand) {
				tab.setPadding(0, 0, 0, 0);
			} else {
				tab.setPadding(tabPadding, 0, tabPadding, 0);
			}

			tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
			tab.setTypeface(tabTypeface, tabStyleIndex);
			tab.setTextColor(tabTextColor);

			if (textAllCaps) {
				tab.setAllCaps(true);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (!shouldExpand || MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
			return;
		}

		int myWidth = getMeasuredWidth();
		int childWidth = 0;
		for (int i = 0; i < tabCount; i++) {
			childWidth += tabsContainer.getChildAt(i).getMeasuredWidth();
		}

		if (!checkedTabWidths && childWidth > 0 && myWidth > 0) {

			if (childWidth <= myWidth) {
				for (int i = 0; i < tabCount; i++) {
					tabsContainer.getChildAt(i).setLayoutParams(expandedTabLayoutParams);
				}
			}

			checkedTabWidths = true;
		}
	}

	private void scrollToChild(int position, int offset) {
		if (tabCount == 0) {
			return;
		}

		int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;

		if (position > 0 || offset > 0) {
			newScrollX -= scrollOffset;
		}

		if (newScrollX != lastScrollX) {
			lastScrollX = newScrollX;
			scrollTo(newScrollX, 0);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (isInEditMode() || tabCount == 0) {
			return;
		}

		final int height = getHeight();

		// default: line below current tab
		TextView currentTab = (TextView) tabsContainer.getChildAt(currentPosition);
		float lineLeft = currentTab.getLeft();
		float lineRight = currentTab.getRight();

		// if there is an offset, start interpolating left and right coordinates between current and next tab
		if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {
			TextView nextTab = (TextView) tabsContainer.getChildAt(currentPosition + 1);
			final float nextTabLeft = nextTab.getLeft();
			final float nextTabRight = nextTab.getRight();

			lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft);
			lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);

			int currentTabColor = (Integer) tabTextColorEvaluator.evaluate(currentPositionOffset, 
					tabSelectedTextColor, tabTextColor);
			currentTab.setTextColor(currentTabColor);

			int nextTabColor = (Integer) tabTextColorEvaluator.evaluate(currentPositionOffset, tabTextColor, 
					tabSelectedTextColor);
			nextTab.setTextColor(nextTabColor);
		}

		canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, indicatorPaint);

		// draw underline
		canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, underlinePaint);

		// draw divider
		for (int i = 0; i < tabCount - 1; i++) {
			final int tabRight = tabsContainer.getChildAt(i).getRight();
			canvas.drawLine(tabRight, dividerPadding, tabRight, height - dividerPadding, dividerPaint);
		}
	}

	private class PageListener implements OnPageChangeListener {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			currentPosition = position;
			currentPositionOffset = positionOffset;

			scrollToChild(position, (int) (positionOffset * tabsContainer.getChildAt(position).getWidth()));

			invalidate();

			if (delegatePageListener != null) {
				delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == ViewPager.SCROLL_STATE_IDLE) {
				scrollToChild(pager.getCurrentItem(), 0);
			}

			if (delegatePageListener != null) {
				delegatePageListener.onPageScrollStateChanged(state);
			}
		}

		@Override
		public void onPageSelected(int position) {
			updateSelectedPage(position);
			if (delegatePageListener != null) {
				delegatePageListener.onPageSelected(position);
			}
		}
	}
	
	private void updateSelectedPage(int position) {
		TextView currentTab = (TextView) tabsContainer.getChildAt(currentPosition);
		currentTab.setTextColor(tabTextColor);

		TextView nextTab = (TextView) tabsContainer.getChildAt(position);
		nextTab.setTextColor(tabSelectedTextColor);
	}

	public void setIndicatorColor(int indicatorColor) {
		indicatorPaint.setColor(indicatorColor);
		invalidate();
	}

	public void setIndicatorColorResource(int resId) {
		setIndicatorColor(getResources().getColor(resId));
	}

	public int getIndicatorColor() {
		return indicatorPaint.getColor();
	}

	public void setIndicatorHeight(int indicatorLineHeightPx) {
		this.indicatorHeight = indicatorLineHeightPx;
		invalidate();
	}

	public int getIndicatorHeight() {
		return indicatorHeight;
	}

	public void setUnderlineColor(int underlineColor) {
		underlinePaint.setColor(underlineColor);
		invalidate();
	}

	public void setUnderlineColorResource(int resId) {
		setUnderlineColor(getResources().getColor(resId));
	}

	public int getUnderlineColor() {
		return underlinePaint.getColor();
	}

	public void setDividerColor(int dividerColor) {
		dividerPaint.setColor(dividerColor);
		invalidate();
	}

	public void setDividerColorResource(int resId) {
		setDividerColor(getResources().getColor(resId));
	}

	public int getDividerColor() {
		return dividerPaint.getColor();
	}

	public void setUnderlineHeight(int underlineHeightPx) {
		this.underlineHeight = underlineHeightPx;
		invalidate();
	}

	public int getUnderlineHeight() {
		return underlineHeight;
	}

	public void setDividerPadding(int dividerPaddingPx) {
		this.dividerPadding = dividerPaddingPx;
		invalidate();
	}

	public int getDividerPadding() {
		return dividerPadding;
	}

	public void setScrollOffset(int scrollOffsetPx) {
		this.scrollOffset = scrollOffsetPx;
		invalidate();
	}

	public int getScrollOffset() {
		return scrollOffset;
	}

	public void setShouldExpand(boolean shouldExpand) {
		this.shouldExpand = shouldExpand;
		requestLayout();
	}

	public boolean getShouldExpand() {
		return shouldExpand;
	}

	public boolean isTextAllCaps() {
		return textAllCaps;
	}

	public void setAllCaps(boolean textAllCaps) {
		this.textAllCaps = textAllCaps;
	}

	public void setTextSize(int textSizePx) {
		this.tabTextSize = textSizePx;
		updateTabStyles();
	}

	public int getTextSize() {
		return tabTextSize;
	}

	public void setTextColor(int textColor) {
		this.tabTextColor = textColor;
		updateTabStyles();
	}

	public void setTextColorResource(int resId) {
		this.tabTextColor = getResources().getColor(resId);
		updateTabStyles();
	}

	public int getTextColor() {
		return tabTextColor;
	}
	
	public void setTypeface(Typeface typeface) {
		this.tabTypeface = typeface;
		updateTabStyles();
	}

	public void setTypeface(Typeface typeface, int style) {
		this.tabTypeface = typeface;
		this.tabStyleIndex = style;
		updateTabStyles();
	}

	public void setTabBackground(int resId) {
		this.tabBackgroundResId = resId;
	}

	public int getTabBackground() {
		return tabBackgroundResId;
	}

	public void setTabPaddingLeftRight(int paddingPx) {
		this.tabPadding = paddingPx;
		updateTabStyles();
	}

	public int getTabPaddingLeftRight() {
		return tabPadding;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		currentPosition = savedState.currentPosition;
		requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.currentPosition = currentPosition;
		return savedState;
	}

	static class SavedState extends BaseSavedState {
		int currentPosition;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentPosition = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(currentPosition);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

}
