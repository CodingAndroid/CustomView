package code.com.customview;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import code.com.customview.circlebar.CircleBarView;
import code.com.customview.utils.LinearGradientUtil;

public class MainActivity extends AppCompatActivity {

    private CircleView mCircleView;
    private CircleBarView mCircleBarView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCircleView = findViewById(R.id.layout);
        mCircleView.setCircleMargin(0.9f);
        mCircleView.setAdapter(new CircleAdapter() {
            @Override
            public int getCount() {
                return 8;
            }

            @Override
            public View getView(final int position, ViewGroup parent) {
                ImageView imageView = new ImageView(parent.getContext());
                imageView.setImageResource(R.mipmap.weibo);
                if (position == 0) {
                    imageView.setImageResource(R.mipmap.weichat);
                }
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "position--" + position, Toast.LENGTH_SHORT).show();
                    }
                });
                return imageView;
            }
        });

        mTextView = findViewById(R.id.text_progress);
        mCircleBarView = findViewById(R.id.circle_bar);
        mCircleBarView.setOnAnimationListener(new CircleBarView.OnAnimationListener() {
            @Override
            public String textChanged(float interpolatedTime, int progressNum, int maxNum) {
                DecimalFormat decimalFormat=new DecimalFormat("0.00");
                String s = decimalFormat.format(interpolatedTime * progressNum / maxNum * 100) + "%";
                return s;
            }

            @Override
            public void progressColorChange(Paint paint, float interpolatedTime, int progressNum, int maxNum) {
                LinearGradientUtil linearGradient = new LinearGradientUtil(Color.YELLOW, Color.RED);
                paint.setColor(linearGradient.getColor(interpolatedTime));
            }
        });
        mCircleBarView.setTextView(mTextView);
        mCircleBarView.setAnimTime(100, 3000);
    }
}
