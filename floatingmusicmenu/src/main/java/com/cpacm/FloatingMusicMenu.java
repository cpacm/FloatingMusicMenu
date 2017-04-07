package com.cpacm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.cpacm.floatingmusicbutton.R;

/**
 * <p>
 * 浮动音乐菜单，可以显示歌曲封面和旋转动画并随着音乐显示进度。
 * <ul>
 * <li>
 * {@link #toggle()} 切换菜单的展开收缩状态<br/>
 * </li>
 * <li>
 * {@link #setProgress(float)} 设置进度
 * </li>
 * <li>
 * {@link #setFloatingDirection(int)} 设置展开方向
 * </li>
 * <li>
 * {@link #setMusicCover(Drawable/Bitmap)} 设置封面
 * </li>
 * <li>
 * {@link #start()} 开始旋转动画 <br/>
 * {@link #stop()} 停止旋转动画
 * </li>
 * </ul>
 * </p>
 * <p>
 * 可以通过调用 {@link #addButton(FloatingActionButton)} 和 {@link #removeButton(FloatingActionButton)} 来动态增减按钮数量。
 *
 * @author cpacm
 * @date 2017/03/21
 * </p>
 */
@CoordinatorLayout.DefaultBehavior(FloatingMusicMenu.Behavior.class)
public class FloatingMusicMenu extends ViewGroup {

    public final static int FLOATING_DIRECTION_UP = 0;
    public final static int FLOATING_DIRECTION_LEFT = 1;
    public final static int FLOATING_DIRECTION_DOWN = 2;
    public final static int FLOATING_DIRECTION_RIGHT = 3;

    private static final int SHADOW_OFFSET = 20;

    private FloatingMusicButton floatingMusicButton;
    private AnimatorSet showAnimation;
    private AnimatorSet hideAnimation;

    private int progressWidthPercent;
    private int progressColor;
    private float progress;
    private float buttonInterval;
    private ColorStateList backgroundTint;
    private Drawable cover;
    private boolean isExpanded;
    private boolean isHided;
    private int floatingDirection;

    public FloatingMusicMenu(Context context) {
        this(context, null);
    }

    public FloatingMusicMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMenu(context, attrs);
    }

    public FloatingMusicMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initMenu(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FloatingMusicMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initMenu(context, attrs);
    }

    private void initMenu(Context context, AttributeSet attrs) {
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.FloatingMusicMenu, 0, 0);
        progressWidthPercent = attr.getInteger(R.styleable.FloatingMusicMenu_fmm_progress_percent, 3);
        progressColor = attr.getColor(R.styleable.FloatingMusicMenu_fmm_progress_color, getResources().getColor(android.R.color.holo_blue_dark));
        progress = attr.getFloat(R.styleable.FloatingMusicMenu_fmm_progress, 0);
        buttonInterval = attr.getDimension(R.styleable.FloatingMusicMenu_fmm_button_interval, 4);
        buttonInterval = dp2px(buttonInterval);
 /*       if (Build.VERSION.SDK_INT < 21) {
            // 版本兼容
            buttonInterval = -BitmapUtils.dp2px(16);
        }*/
        cover = attr.getDrawable(R.styleable.FloatingMusicMenu_fmm_cover);
        backgroundTint = attr.getColorStateList(R.styleable.FloatingMusicMenu_fmm_backgroundTint);
        floatingDirection = attr.getInteger(R.styleable.FloatingMusicMenu_fmm_floating_direction, 0);
        attr.recycle();
        createRootButton(context);
        addScrollAnimation();
    }

    private void addScrollAnimation() {
        showAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
        showAnimation.play(ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f));
        showAnimation.setInterpolator(alphaExpandInterpolator);
        showAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                setVisibility(VISIBLE);
            }
        });

        hideAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
        hideAnimation.play(ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0f));
        hideAnimation.setInterpolator(alphaExpandInterpolator);
        hideAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setVisibility(GONE);
            }
        });
    }

    private void createRootButton(Context context) {
        floatingMusicButton = new FloatingMusicButton(context);
        floatingMusicButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
        floatingMusicButton.config(progressWidthPercent, progressColor, backgroundTint);
        floatingMusicButton.setProgress(progress);
        if (cover != null) {
            floatingMusicButton.setCoverDrawable(cover);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addView(floatingMusicButton, super.generateDefaultLayoutParams());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        switch (floatingDirection) {
            case FLOATING_DIRECTION_UP:
            case FLOATING_DIRECTION_DOWN:
                onMeasureVerticalDirection();
                break;
            case FLOATING_DIRECTION_LEFT:
            case FLOATING_DIRECTION_RIGHT:
                onMeasureHorizontalDirection();
                break;
        }
    }

    /**
     * 计算竖向排列时需要的大小
     */
    private void onMeasureVerticalDirection() {
        int width = 0;
        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;
            width = Math.max(child.getMeasuredWidth(), width);
            height += child.getMeasuredHeight();
        }
        width += SHADOW_OFFSET * 2;
        height += SHADOW_OFFSET * 2;
        height += buttonInterval * (getChildCount() - 1);
        height = adjustShootLength(height);
        setMeasuredDimension(width, height);
    }

    /**
     * 计算横向排列时需要的大小
     */
    private void onMeasureHorizontalDirection() {
        int width = 0;
        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;
            height = Math.max(child.getMeasuredHeight(), height);
            width += child.getMeasuredWidth();
        }
        width += SHADOW_OFFSET * 2;
        height += SHADOW_OFFSET * 2;
        width += buttonInterval * (getChildCount() - 1);
        width = adjustShootLength(width);
        setMeasuredDimension(width, height);
    }

    private int adjustShootLength(int length) {
        return length * 12 / 10;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        switch (floatingDirection) {
            case FLOATING_DIRECTION_UP:
                onUpDirectionLayout(l, t, r, b);
                break;
            case FLOATING_DIRECTION_DOWN:
                onDownDirectionLayout(l, t, r, b);
                break;
            case FLOATING_DIRECTION_LEFT:
                onLeftDirectionLayout(l, t, r, b);
                break;
            case FLOATING_DIRECTION_RIGHT:
                onRightDirectionLayout(l, t, r, b);
                break;
        }
    }

    /**
     * 摆放朝上展开方向的子控件位置
     */
    private void onUpDirectionLayout(int l, int t, int r, int b) {
        int centerX = (r - l) / 2;
        int offsetY = b - t - SHADOW_OFFSET;

        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            child.layout(centerX - width / 2, offsetY - height, centerX + width / 2, offsetY);

            //排除根按钮，添加动画
            if (i != getChildCount() - 1) {
                float collapsedTranslation = b - t - SHADOW_OFFSET - offsetY;
                float expandedTranslation = 0f;
                child.setTranslationY(isExpanded ? expandedTranslation : collapsedTranslation);
                child.setAlpha(isExpanded ? 1f : 0f);

                MenuLayoutParams params = (MenuLayoutParams) child.getLayoutParams();
                params.collapseDirAnim.setFloatValues(expandedTranslation, collapsedTranslation);
                params.expandDirAnim.setFloatValues(collapsedTranslation, expandedTranslation);
                params.collapseDirAnim.setProperty(View.TRANSLATION_Y);
                params.expandDirAnim.setProperty(View.TRANSLATION_Y);
                params.setAnimationsTarget(child);
            }
            offsetY -= height + buttonInterval;
        }
    }

    /**
     * 摆放朝下展开方向的子控件位置
     */
    private void onDownDirectionLayout(int l, int t, int r, int b) {
        int centerX = (r - l) / 2;
        int offsetY = SHADOW_OFFSET;
        View rootView = getChildAt(getChildCount() - 1);
        rootView.layout(centerX - rootView.getMeasuredWidth() / 2, offsetY, centerX + rootView.getMeasuredWidth() / 2, offsetY + rootView.getMeasuredHeight());
        offsetY += rootView.getMeasuredHeight() + buttonInterval;

        for (int i = 0; i < getChildCount() - 1; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            child.layout(centerX - width / 2, offsetY, centerX + width / 2, offsetY + height);

            float collapsedTranslation = -offsetY;
            float expandedTranslation = 0f;
            child.setTranslationY(isExpanded ? expandedTranslation : collapsedTranslation);
            child.setAlpha(isExpanded ? 1f : 0f);

            MenuLayoutParams params = (MenuLayoutParams) child.getLayoutParams();
            params.collapseDirAnim.setFloatValues(expandedTranslation, collapsedTranslation);
            params.expandDirAnim.setFloatValues(collapsedTranslation, expandedTranslation);
            params.collapseDirAnim.setProperty(View.TRANSLATION_Y);
            params.expandDirAnim.setProperty(View.TRANSLATION_Y);
            params.setAnimationsTarget(child);

            offsetY += height + buttonInterval;
        }
    }

    /**
     * 摆放朝左展开方向的子控件位置
     */
    private void onLeftDirectionLayout(int l, int t, int r, int b) {
        int centerY = (b - t) / 2;
        int offsetX = r - l - SHADOW_OFFSET;

        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            child.layout(offsetX - width, centerY - height / 2, offsetX, centerY + height / 2);

            //排除根按钮，添加动画
            if (i != getChildCount() - 1) {
                float collapsedTranslation = r - l - SHADOW_OFFSET - offsetX;
                float expandedTranslation = 0f;
                child.setTranslationX(isExpanded ? expandedTranslation : collapsedTranslation);
                child.setAlpha(isExpanded ? 1f : 0f);

                MenuLayoutParams params = (MenuLayoutParams) child.getLayoutParams();
                params.collapseDirAnim.setFloatValues(expandedTranslation, collapsedTranslation);
                params.expandDirAnim.setFloatValues(collapsedTranslation, expandedTranslation);
                params.collapseDirAnim.setProperty(View.TRANSLATION_X);
                params.expandDirAnim.setProperty(View.TRANSLATION_X);
                params.setAnimationsTarget(child);
            }
            offsetX -= width + buttonInterval;
        }
    }

    /**
     * 摆放朝右展开方向的子控件位置
     */
    private void onRightDirectionLayout(int l, int t, int r, int b) {
        int centerY = (b - t) / 2;
        int offsetX = SHADOW_OFFSET;
        View rootView = getChildAt(getChildCount() - 1);
        rootView.layout(offsetX, centerY - rootView.getMeasuredHeight() / 2, offsetX + rootView.getMeasuredWidth(), centerY + rootView.getMeasuredHeight() / 2);
        offsetX += rootView.getMeasuredWidth() + buttonInterval;

        for (int i = 0; i < getChildCount() - 1; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                continue;
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            child.layout(offsetX, centerY - height / 2, offsetX + width, centerY + height / 2);

            float collapsedTranslation = -offsetX;
            float expandedTranslation = 0f;
            child.setTranslationX(isExpanded ? expandedTranslation : collapsedTranslation);
            child.setAlpha(isExpanded ? 1f : 0f);

            MenuLayoutParams params = (MenuLayoutParams) child.getLayoutParams();
            params.collapseDirAnim.setFloatValues(expandedTranslation, collapsedTranslation);
            params.expandDirAnim.setFloatValues(collapsedTranslation, expandedTranslation);
            params.collapseDirAnim.setProperty(View.TRANSLATION_X);
            params.expandDirAnim.setProperty(View.TRANSLATION_X);
            params.setAnimationsTarget(child);

            offsetX += width + buttonInterval;
        }
    }

    public void setButtonInterval(float buttonInterval) {
        this.buttonInterval = buttonInterval;
        requestLayout();
    }

    public void addButton(FloatingActionButton button) {
        addView(button, 0);
        requestLayout();
    }

    public void addButtonAtLast(FloatingActionButton button) {
        addView(button, getChildCount() - 1);
        requestLayout();
    }

    public void removeButton(FloatingActionButton button) {
        removeView(button);
        requestLayout();
    }

    public void setMusicCover(Drawable drawable) {
        floatingMusicButton.setCoverDrawable(drawable);
    }

    public void setMusicCover(Bitmap bitmap) {
        floatingMusicButton.setCover(bitmap);
    }

    public void setProgress(float progress) {
        if (floatingMusicButton != null) {
            floatingMusicButton.setProgress(progress);
        }
    }

    public void start() {
        floatingMusicButton.rotate(true);
    }

    public void stop() {
        floatingMusicButton.rotate(false);
    }

    public void setFloatingDirection(int floatingDirection) {
        this.floatingDirection = floatingDirection;
        postInvalidate();
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MenuLayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MenuLayoutParams(super.generateLayoutParams(attrs));
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MenuLayoutParams(super.generateLayoutParams(p));
    }

    private static final int ANIMATION_DURATION = 300;
    private static final float COLLAPSED_PLUS_ROTATION = 0f;
    private static final float EXPANDED_PLUS_ROTATION = 90f + 45f;

    private AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    private AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);

    private static Interpolator expandInterpolator = new OvershootInterpolator();
    private static Interpolator collapseInterpolator = new DecelerateInterpolator(3f);
    private static Interpolator alphaExpandInterpolator = new DecelerateInterpolator();

    private class MenuLayoutParams extends LayoutParams {

        private ObjectAnimator expandDirAnim = new ObjectAnimator();
        private ObjectAnimator expandAlphaAnim = new ObjectAnimator();
        private ObjectAnimator collapseDirAnim = new ObjectAnimator();
        private ObjectAnimator collapseAlphaAnim = new ObjectAnimator();

        private boolean animationsSetToPlay;

        public MenuLayoutParams(LayoutParams source) {
            super(source);

            expandDirAnim.setInterpolator(expandInterpolator);
            expandAlphaAnim.setInterpolator(alphaExpandInterpolator);
            collapseDirAnim.setInterpolator(collapseInterpolator);
            collapseAlphaAnim.setInterpolator(collapseInterpolator);

            collapseAlphaAnim.setProperty(View.ALPHA);
            collapseAlphaAnim.setFloatValues(1f, 0f);

            expandAlphaAnim.setProperty(View.ALPHA);
            expandAlphaAnim.setFloatValues(0f, 1f);
        }

        public void setAnimationsTarget(View view) {
            collapseAlphaAnim.setTarget(view);
            collapseDirAnim.setTarget(view);
            expandDirAnim.setTarget(view);
            expandAlphaAnim.setTarget(view);

            // Now that the animations have targets, set them to be played
            if (!animationsSetToPlay) {
                addLayerTypeListener(expandDirAnim, view);
                addLayerTypeListener(collapseDirAnim, view);

                mCollapseAnimation.play(collapseAlphaAnim);
                mCollapseAnimation.play(collapseDirAnim);
                mExpandAnimation.play(expandAlphaAnim);
                mExpandAnimation.play(expandDirAnim);
                animationsSetToPlay = true;
            }
        }

        private void addLayerTypeListener(Animator animator, final View view) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setLayerType(LAYER_TYPE_NONE, null);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    view.setLayerType(LAYER_TYPE_HARDWARE, null);
                }
            });
        }
    }

    public void collapse() {
        collapse(false);
    }

    public void collapseImmediately() {
        collapse(true);
    }

    private void collapse(boolean immediately) {
        if (isExpanded) {
            isExpanded = false;
            mCollapseAnimation.setDuration(immediately ? 0 : ANIMATION_DURATION);
            mCollapseAnimation.start();
            mExpandAnimation.cancel();
        }
    }

    public void toggle() {
        if (isExpanded) {
            collapse();
        } else {
            expand();
        }
    }

    public void expand() {
        if (!isExpanded) {
            isExpanded = true;
            mCollapseAnimation.cancel();
            mExpandAnimation.start();
        }
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void hide() {
        if (!isHided) {
            isHided = true;
            hideAnimation.start();
            showAnimation.cancel();
        }
    }

    public void show() {
        if (isHided) {
            isHided = false;
            showAnimation.start();
            hideAnimation.cancel();
        }
    }

    public float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    /**
     * <p>
     * 上拉隐藏，下拉显示的动作行为，配合 {@link FloatingMusicMenu} 使用更佳
     * </P>
     */
    public static class Behavior extends CoordinatorLayout.Behavior<FloatingMusicMenu> {

        public Behavior() {
            super();
        }

        public Behavior(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingMusicMenu child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
            if (dyConsumed > 30 && child.getVisibility() == VISIBLE) {
                child.hide();
            } else if (dyConsumed < -30 && child.getVisibility() == GONE) {
                child.show();
            }
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingMusicMenu child, View directTargetChild, View target, int nestedScrollAxes) {
            return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
        }
    }
}
