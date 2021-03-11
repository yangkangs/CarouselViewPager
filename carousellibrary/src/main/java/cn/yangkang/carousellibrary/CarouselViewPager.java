package cn.yangkang.carousellibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;


/**
 * @Author: created by YangKang
 * @CreateDate: 2021/3/3 15:32
 * @Description: 轮播图
 */
public class CarouselViewPager extends FrameLayout
        implements ViewPager.OnPageChangeListener {

    private static final String TAG = "CycleViewPager";
    /**
     * none不显示，dot圆点，number数字
     */
    private String NONE_INDICATOR = "none";
    private String DOT_INDICATOR = "dot";
    private String NUMBER_INDICATOR = "number";
    private String indicatorType = DOT_INDICATOR;


    private Context mContext;

    public ViewPager mViewPager;//实现轮播图的ViewPager

    private TextView mTitle;//标题

    private LinearLayout mIndicatorLayout; // 指示器

    private Handler handler;//每几秒后执行下一张的切换

    private int WHEEL = 100; // 转动

    private int WHEEL_WAIT = 101; // 等待

    private List<View> mViews = new ArrayList<>(); //需要轮播的View，数量为轮播图数量+2

    private ImageView[] mIndicators;    //指示器小圆点

    private int fillet = 0;//item圆角

    private int pageMargin = 0;//item间间隔

    private boolean isScrolling = false; // 滚动框是否滚动着

    private boolean isCycle = true; // 是否循环，默认为true

    private boolean isWheel = true; // 是否轮播，默认为true(是否自动滚动)

    private int delay = 3000; // 默认轮播时间

    private int mCurrentPosition = 0; // 轮播当前位置

    private long releaseTime = 0; // 手指松开、页面不滚动时间，防止手机松开后短时间进行切换

    private ViewPagerAdapter mAdapter;

    private ImageCycleViewListener mImageCycleViewListener;
    private MyViewInitListener myViewInitListener;

    private List<String> infos;//数据集合

    private int mIndicatorSelected;//指示器图片，被选择状态

    private int mIndicatorUnselected;//指示器图片，未被选择状态


    final Runnable runnable = new Runnable() {

        @Override
        public void run() {
            if (mContext != null && isWheel) {
                long now = System.currentTimeMillis();
                // 检测上一次滑动时间与本次之间是否有触击(手滑动)操作，有的话等待下次轮播
                if (now - releaseTime > delay - 500) {
                    handler.sendEmptyMessage(WHEEL);
                } else {
                    handler.sendEmptyMessage(WHEEL_WAIT);
                }
            }
        }
    };
    private int myLayoutId = -1;
    private TextView lable_TextView;


    public CarouselViewPager(Context context) {
        this(context, null);
    }

    public CarouselViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CarouselViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CarouselViewPager);
        fillet = (int) typedArray.getDimension(R.styleable.CarouselViewPager_fillet, 0);
        pageMargin = (int) typedArray.getDimension(R.styleable.CarouselViewPager_pageMargin, 0);
        mIndicatorSelected = typedArray.getResourceId(R.styleable.CarouselViewPager_indicatorFocus, R.drawable.radius_bg1);
        mIndicatorUnselected = typedArray.getResourceId(R.styleable.CarouselViewPager_indicatorNormal, R.drawable.radius_bg2);
        isWheel = typedArray.getBoolean(R.styleable.CarouselViewPager_isWheel, true);
        indicatorType = typedArray.getString(R.styleable.CarouselViewPager_indicatorType);
        if (TextUtils.isEmpty(indicatorType)) {
            indicatorType = DOT_INDICATOR;
        }
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        if (fillet > 0) {
            //父布局设置圆角
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), fillet);
                }
            });
            setClipToOutline(true);
        }
        LayoutInflater.from(mContext).inflate(R.layout.layout_cycle_view, this, true);
        mViewPager = (ViewPager) findViewById(R.id.cycle_view_pager);
        mViewPager.setPageMargin(pageMargin);
//        mTitle = (TextView) findViewById(R.id.cycle_title);
        mIndicatorLayout = (LinearLayout) findViewById(R.id.cycle_indicator);
        lable_TextView = (TextView) findViewById(R.id.lable_TextView);
        if (NONE_INDICATOR.equals(indicatorType)) {
            mIndicatorLayout.setVisibility(GONE);
            lable_TextView.setVisibility(GONE);
        } else if (DOT_INDICATOR.equals(indicatorType)) {
            mIndicatorLayout.setVisibility(VISIBLE);
            lable_TextView.setVisibility(GONE);
        } else if (NUMBER_INDICATOR.equals(indicatorType)) {
            mIndicatorLayout.setVisibility(GONE);
            lable_TextView.setVisibility(VISIBLE);
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == WHEEL && mViews.size() > 0) {
                    if (!isScrolling) {
                        //当前为非滚动状态，切换到下一页
                        int posttion = (mCurrentPosition + 1) % mViews.size();
                        mViewPager.setCurrentItem(posttion, true);
                    }
                    releaseTime = System.currentTimeMillis();
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, delay);
                    return;

                }
                if (msg.what == WHEEL_WAIT && mViews.size() > 0) {
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, delay);
                }
            }
        };
    }

    public LinearLayout getdotIndicator() {
        return mIndicatorLayout;
    }


    public TextView getNumIndicator() {
        return lable_TextView;
    }

    /**
     * 设置指示器图片，在setData之前调用
     *
     * @param select   选中时的图片
     * @param unselect 未选中时的图片
     */
    public void setIndicators(int select, int unselect) {
        mIndicatorSelected = select;
        mIndicatorUnselected = unselect;
    }

    @Override
    public void setBackgroundColor(int color) {
        if (mViewPager != null) {
            mViewPager.setBackgroundColor(color);
        }
    }


    /**
     * 自定义itemView,一定要设置在setData()之前
     */
    public void setMyView(int layoutId, MyViewInitListener listener) {
        this.myLayoutId = layoutId;
        myViewInitListener = listener;
    }

    public void setData(List<String> list, ImageCycleViewListener listener) {
        setData(list, listener, 0);
    }


    /**
     * 初始化viewpager
     *
     * @param list         要显示的数据
     * @param showPosition 默认显示位置
     */
    public void setData(List<String> list, ImageCycleViewListener listener, int showPosition) {
        if (list == null || list.size() == 0) {
            //没有数据时隐藏整个布局
            this.setVisibility(View.GONE);
            return;
        }
        mViews.clear();
        infos = list;
        if (infos.size() < 2) {
            setWheel(false);
            setCycle(false);
        }
        if (isCycle) {
            //添加轮播图View，数量为集合数+2
            // 将最后一个View添加进来
            mViews.add(getImageView(mContext, infos.get(infos.size() - 1)));
            for (int i = 0; i < infos.size(); i++) {
                mViews.add(getImageView(mContext, infos.get(i)));
            }
            // 将第一个View添加进来
            mViews.add(getImageView(mContext, infos.get(0)));
        } else {
            //只添加对应数量的View
            for (int i = 0; i < infos.size(); i++) {
                mViews.add(getImageView(mContext, infos.get(i)));
            }
        }
        if (mViews == null || mViews.size() == 0) {
            //没有View时隐藏整个布局
            this.setVisibility(View.GONE);
            return;
        }
        mImageCycleViewListener = listener;
        if (infos.size() > 1) {
            if (DOT_INDICATOR.equals(indicatorType)) {
                int ivSize = mViews.size();
                // 设置指示器
                mIndicators = new ImageView[ivSize];
                if (isCycle) {
                    mIndicators = new ImageView[ivSize - 2];
                }
                mIndicatorLayout.removeAllViews();
                for (int i = 0; i < mIndicators.length; i++) {
                    mIndicators[i] = new ImageView(mContext);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            20, 20);
                    lp.setMargins(10, 0, 10, 0);
                    mIndicators[i].setLayoutParams(lp);
                    mIndicatorLayout.addView(mIndicators[i]);
                }
            }
        } else {
            mIndicatorLayout.setVisibility(GONE);
            lable_TextView.setVisibility(GONE);
        }
        mAdapter = new ViewPagerAdapter();
        // 默认指向第一项，下方viewPager.setCurrentItem将触发重新计算指示器指向
        setIndicator(0);
        mViewPager.setOffscreenPageLimit(mViews.size());
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setAdapter(mAdapter);
        if (showPosition < 0 || showPosition >= mViews.size()) {
            showPosition = 0;
        }
        if (isCycle) {
            showPosition = showPosition + 1;
        }
        mViewPager.setCurrentItem(showPosition);
        if (isWheel && infos.size() > 1) {
            setWheel(true);
        }
    }

    /**
     * 获取轮播图View
     *
     * @param context
     * @param imgurl
     */
    private View getImageView(Context context, String imgurl) {
        if (myLayoutId >= 0) {
            View view = LayoutInflater.from(context).inflate(myLayoutId, null);
            if (myViewInitListener != null) {
                myViewInitListener.onMyViewInitListener(imgurl, view);
            }
            return view;
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner_image, null);
        if (view != null) {
            final ImageView imageView = view.findViewById(R.id.image);
            RequestOptions options = new RequestOptions()
                    .transform(new CenterCrop(), new RoundedCornersTransform(fillet, fillet, fillet, fillet));
            Glide.with(context).load(imgurl)
                    .apply(options)
//                    .placeholder(placeholder > 0 ? placeholder : R.drawable.photo_empty)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(imageView);
        }
        return view;
    }


    public void setFillet(int fillet) {
        this.fillet = fillet;
    }

    /**
     * 设置指示器，和文字内容
     *
     * @param selectedPosition 默认指示器位置
     */
    private void setIndicator(int selectedPosition) {
//        setText(mTitle, infos.get(selectedPosition).getTitle());
        if (infos.size() < 2) {
            return;
        }
        try {
            if (DOT_INDICATOR.equals(indicatorType)) {
                for (int i = 0; i < mIndicators.length; i++) {
                    mIndicators[i]
                            .setBackgroundResource(mIndicatorUnselected);
                }
                if (mIndicators.length > selectedPosition) {
                    mIndicators[selectedPosition].setBackgroundResource(mIndicatorSelected);
                }
            } else if (NUMBER_INDICATOR.equals(indicatorType)) {
                lable_TextView.setText((selectedPosition + 1) + "/" + infos.size());
            }
        } catch (Exception e) {
            Log.i(TAG, "指示器路径不正确");
        }
    }


    /**
     * 页面适配器 返回对应的view
     *
     * @author Yuedong Li
     */
    private class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            View v = mViews.get(position);
            if (mImageCycleViewListener != null) {
                v.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int posi = isCycle ? mCurrentPosition - 1 : mCurrentPosition;
                        mImageCycleViewListener.onImageClick(infos.get(posi), posi, v);
                    }
                });
            }
            container.addView(v);
            return v;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int arg0) {
        int max = mViews.size() - 1;
        int position = arg0;
        mCurrentPosition = arg0;
        if (isCycle) {
            if (arg0 == 0) {

                //滚动到mView的1个（界面上的最后一个），将mCurrentPosition设置为max - 1
                mCurrentPosition = max - 1;
            } else if (arg0 == max) {
                //滚动到mView的最后一个（界面上的第一个），将mCurrentPosition设置为1
                mCurrentPosition = 1;
            }
            position = mCurrentPosition - 1;
        }
        setIndicator(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == 1) { // viewPager在滚动
            isScrolling = true;
            return;
        } else if (state == 0) { // viewPager滚动结束

            releaseTime = System.currentTimeMillis();
            //跳转到第mCurrentPosition个页面（没有动画效果，实际效果页面上没变化）
            mViewPager.setCurrentItem(mCurrentPosition, false);

        }
        isScrolling = false;
    }

    /**
     * 为textview设置文字
     *
     * @param textView
     * @param text
     */
    public static void setText(TextView textView, String text) {
        if (text != null && textView != null) {
            textView.setText(text);
        }
    }

    /**
     * 为textview设置文字
     *
     * @param textView
     * @param text
     */
    public static void setText(TextView textView, int text) {
        if (textView != null) {
            setText(textView, text + "");
        }
    }

    /**
     * 是否循环，默认开启。必须在setData前调用
     *
     * @param isCycle 是否循环
     */
    public void setCycle(boolean isCycle) {
        this.isCycle = isCycle;
    }


    /**
     * 是否处于循环状态
     *
     * @return
     */
    public boolean isCycle() {
        return isCycle;
    }

    /**
     * 设置是否轮播，默认轮播,轮播一定是循环的
     *
     * @param isWheel
     */
    public void setWheel(boolean isWheel) {
        this.isWheel = isWheel;
        if (isWheel) {
            isCycle = true;
            handler.postDelayed(runnable, delay);
        }
    }


    /**
     * 刷新数据，当外部视图更新后，通知刷新数据
     */
    public void refreshData() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 是否处于轮播状态
     *
     * @return
     */
    public boolean isWheel() {
        return isWheel;
    }

    /**
     * 设置轮播暂停时间,单位毫秒（默认4000毫秒）
     *
     * @param delay
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * 轮播控件的监听事件
     *
     * @author minking
     */
    public static interface ImageCycleViewListener {

        /**
         * 单击图片事件
         *
         * @param url
         * @param position
         * @param imageView
         */
        public void onImageClick(String url, int position, View imageView);
    }


    public static interface MyViewInitListener {


        public void onMyViewInitListener(String url, View myView);
    }


}
