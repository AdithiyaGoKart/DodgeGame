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
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
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
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap babydragon, missiles;
        Random random = new Random();
        int babydragonX = 0;
        int babydragonY = 0;
        int missileY = 0;
        int flipX = 1;
        int flipY = 1;
        int points = 0;
        int screenWidth;
        int screenHeight;
        int missileRand;
        Paint paintProperty;
        private SoundPool soundPool;
        MediaPlayer mediaPlayer;
        int sound;

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

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                AudioAttributes audioAttributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build();
                soundPool = new SoundPool.Builder().setMaxStreams(2).setAudioAttributes(audioAttributes).build();
            } else
                soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);

            sound = soundPool.load(getApplicationContext(), R.raw.explosion, 1);

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
                canvas.drawRGB(0, 0, 0);
                canvas.drawBitmap(babydragon, (screenWidth/2)- babydragon.getWidth()/2 + babydragonX, (float) ((screenHeight/2)- babydragon.getHeight()/2 + babydragonY), null);
                canvas.drawText("Points: "+points, 100, 100, paintProperty);

                mediaPlayer = MediaPlayer.create(getContext(), R.raw.bgmusic2);
                mediaPlayer.start();

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
            int counter = 0;
            Rect bbdragon = new Rect((screenWidth/2)- babydragon.getWidth()/2 + babydragonX, ((screenHeight/2)- babydragon.getHeight()/2 + babydragonY), (screenWidth/2)- babydragon.getWidth()/2 + babydragonX+babydragon.getWidth(), ((screenHeight/2)- babydragon.getHeight()/2 + babydragonY) + babydragon.getHeight());

            Rect missile = new Rect(missileRand, -missiles.getHeight()-missileY, missileRand+missiles.getWidth(),-missiles.getHeight()-missileY+missiles.getHeight());

            if (missile.intersect(bbdragon))
            {
                counter = 1;
                points-=10;
                soundPool.play(sound, 1, 1,1 , 0, 1);
                babydragon = BitmapFactory.decodeResource(getResources(),R.drawable.sadbabydragon);
            }
            else{
                if (((screenHeight/2)- babydragon.getHeight()/2 + babydragonY) + babydragon.getHeight() <= 0){
                    points+=10;
                }
                babydragon = BitmapFactory.decodeResource(getResources(), R.drawable.babydragon_50);
                
                System.out.println("5");
            }

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
