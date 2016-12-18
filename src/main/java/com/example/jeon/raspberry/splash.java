package com.example.jeon.raspberry;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class splash extends AppCompatActivity {
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Animation ut = AnimationUtils.loadAnimation(this,R.anim.blink);
        ImageView img = (ImageView) findViewById(R.id.imageView);
        img.startAnimation(ut);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },2000);




    }



    class thr extends Thread{
        public thr() {
        }

        @Override
        public void run() {
            try {
                this.sleep(2000);


            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }



    }
}
