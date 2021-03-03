# CarouselViewPager
轮播图
一个简化版的轮播图

implementation 'cn.yangkang.carousellibrary:CarouselViewPager:1.0.0'

        //自定义item
        carouselView3.setMyView(R.layout.item_banner_image2, new CarouselViewPager.MyViewInitListener() {
            @Override
            public void onMyViewInitListener(String url, View myView) {
                final ImageView imageView = myView.findViewById(R.id.image);
                RequestOptions options = new RequestOptions().transform(new CenterCrop());
                Glide.with(MainActivity.this).load(url)
                        .apply(options)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(imageView);
            }
        });
        carouselView3.setData(objects, new CarouselViewPager.ImageCycleViewListener() {
            @Override
            public void onImageClick(String url, int index, View imageView) {

            }
        });
        
    private int roun = 0;//item圆角
    private int pageMargin = 0;//item间间隔
    private boolean isScrolling = false; // 滚动框是否滚动着
    private boolean isCycle = true; // 是否循环，默认为true
    private boolean isWheel = true; // 是否轮播，默认为true(是否自动滚动)
    private int delay = 3000; // 默认轮播时间
    /**
     * none不显示，dot圆点，number数字
     */
    private String NONE_INDICATOR = "none";
    private String DOT_INDICATOR = "dot";
    private String NUMBER_INDICATOR = "number";
    
 
<resources>
    <declare-styleable name="CarouselViewPager">
        <attr name="fillet" format="dimension" />
        <attr name="pageMargin" format="dimension" />
        <attr name="indicatorFocus" format="reference" />
        <attr name="indicatorNormal" format="reference" />
        <attr name="isWheel" format="boolean" />
        <attr name="indicatorType" format="string" />
    </declare-styleable>
</resources>
    
        <cn.yangkang.carousellibrary.CarouselViewPager
        android:id="@+id/carousel1"
        android:layout_width="345dp"
        android:layout_height="110dp"
        android:layout_gravity="center_horizontal"
        android:layout_marginTop="10dp"
        app:fillet="5dp"
        app:indicatorFocus="@drawable/radius_bg1"
        app:indicatorNormal="@drawable/radius_bg2"
        app:indicatorType="dot"
        app:pageMargin="15dp" />
    
    
    
    
