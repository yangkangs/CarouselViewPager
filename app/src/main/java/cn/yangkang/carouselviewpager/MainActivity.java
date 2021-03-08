package cn.yangkang.carouselviewpager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import cn.yangkang.carousellibrary.CarouselViewPager;
import cn.yangkang.carousellibrary.RoundedCornersTransform;

public class MainActivity extends AppCompatActivity {

    CarouselViewPager carouselView1, carouselView2, carouselView3;
    List<String> objects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        objects.add("https://doctor.metadoc.cn/api/ih-resources/images/1358085651920060416");
        objects.add("https://doctor.metadoc.cn/api/ih-resources/images/1358085817863503872");
        objects.add("https://doctor.metadoc.cn/api/ih-resources/images/1358085910922526720");
        objects.add("https://doctor.metadoc.cn/api/ih-resources/images/1358085997899808768");
        objects.add("https://doctor.metadoc.cn/api/ih-resources/images/1358086084080173056");
        carouselView1 = findViewById(R.id.carousel1);
        carouselView2 = findViewById(R.id.carousel2);
        carouselView3 = findViewById(R.id.carousel3);
        carouselView1.setData(objects, new CarouselViewPager.ImageCycleViewListener() {
            @Override
            public void onImageClick(String url, int index, View imageView) {

            }
        });
        carouselView2.setData(objects, new CarouselViewPager.ImageCycleViewListener() {
            @Override
            public void onImageClick(String url, int index, View imageView) {

            }
        });

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
    }
}