package com.example.dodgegame;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import java.text.DecimalFormat;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    GameSurface gameSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameSurface.resume();
    }

    public class GameSurface extends SurfaceView implements Runnable, SensorEventListener
    {
        Thread gameThread;
        CountDownTimer countDownTimer;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap babydragon, missiles;
        Random random = new Random();
        int babydragonX = 0;
        int babydragonY = 0;
        int missileX = 0;
        int missileY = 0;
        int flipX = 1;
        int flipY = 1;
        int screenWidth;
        int screenHeight;
        int red = 0;
        int green = 0;
        int blue = 0;
        int missileRand;
        Paint paintProperty;

        public GameSurface(Context context)
        {
            super(context);

            new CountDownTimer(31000, 1000)
            {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {

                }
            };
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return false;
                }
            });

            holder = getHolder();
            babydragon = BitmapFactory.decodeResource(getResources(), R.drawable.babydragon_50);
            missiles = BitmapFactory.decodeResource(getResources(), R.drawable.missile_50);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth = sizeOfScreen.x;
            screenHeight = sizeOfScreen.y;

            SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(GameSurface.this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

            paintProperty = new Paint();
            paintProperty.setColor(Color.RED);
            paintProperty.setTextSize(35);

        }

        @Override
        public void run() {
            while (running)
            {
                if (holder.getSurface().isValid() == false)
                    continue;
                Canvas canvas = holder.lockCanvas();
                canvas.drawRGB(red, green, blue);
                canvas.drawBitmap(babydragon, (screenWidth/2)- babydragon.getWidth()/2 + babydragonX, (float) ((screenHeight/2)- babydragon.getHeight()/2 + babydragonY), null);
                canvas.drawText("Points: ", 300, 300, paintProperty);

                increment();
                createMissile(canvas);
                rainingMissiles();
                collisionCheck();
                holder.unlockCanvasAndPost(canvas);
            }
        }

        public void increment()
        {
            if (babydragonX >= screenWidth/2 - babydragon.getWidth()/2)
            {
                babydragonX -= 1;
            }
            else if (babydragonX <= -1*screenWidth/2 + babydragon.getWidth()/2)
            {
                babydragonX += 1;
            }
            else
                babydragonX += flipX;
            if (babydragonY >= screenHeight/2 - babydragon.getHeight()/2)
            {
                babydragonY -= 1;
            }
            else if (babydragonY <= -1*screenHeight/2 + babydragon.getHeight()/2)
            {
                babydragonY += 1;
            }
            else
                babydragonY += flipY;
        }

        public void createMissile (Canvas canvas)
        {
            canvas.drawBitmap(missiles, missileRand, -missiles.getHeight()-missileY, null );
            Missile missile = new Missile(missiles);
        }
        public void rainingMissiles()
        {
            int yLimit = 10;
            missileY -= yLimit;

            if (-missiles.getHeight()-missileY >= screenHeight)
            {
                Log.d("refire missile", "check");
                missileY = -missiles.getHeight();
                missileRand = random.nextInt(screenWidth/2);
            }
        }
        @SuppressLint("ResourceType")
        public void collisionCheck()
        {
            Rect bbdragon = new Rect((screenWidth/2)- babydragon.getWidth()/2 + babydragonX, ((screenHeight/2)- babydragon.getHeight()/2 + babydragonY), (screenWidth/2)- babydragon.getWidth()/2 + babydragonX+babydragon.getWidth(), ((screenHeight/2)- babydragon.getHeight()/2 + babydragonY) + babydragon.getHeight());

            Rect missile = new Rect(missileRand, -missiles.getHeight()-missileY, missileRand+missiles.getWidth(),-missiles.getHeight()-missileY+missiles.getHeight());

            if (missile.intersect(bbdragon))
            {
                Log.d("collided", "oof");
                babydragon = BitmapFactory.decodeResource(getResources(), R.drawable.sadbabydragon);
            }
            else
                babydragon = BitmapFactory.decodeResource(getResources(), R.drawable.babydragon_50);
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            flipX = (int) (-1*sensorEvent.values[0]);
            flipY = (int) sensorEvent.values[1];
            if (flipX > 5){
                flipX+=-2;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        public void resume()
        {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        public void pause()
        {
            running = false;
            while (true)
            {
                try
                {
                    gameThread.join();
                }
                catch (InterruptedException e)
                {

                }
            }
        }
    }
}
