package com.example.toothbrush;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import java.util.stream.Collectors;

public class ParticleView extends View {
    private Paint paint;
    private List<Particle> particles = new ArrayList<>();
    private Random random = new Random();
    private int particleCount = 100;

    public ParticleView(Context context) {
        super(context);
        init();
    }

    public ParticleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ParticleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                for (int i = 0; i < particleCount; i++) {
                    particles.add(new Particle());
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Particle particle : particles) {
            paint.setColor(particle.color);
            canvas.drawCircle(particle.x, particle.y, particle.radius, paint);
        }
        updateParticles();
        invalidate();
    }

    private void updateParticles() {
        for (Particle particle : particles) {
            particle.x += particle.vx;
            particle.y += particle.vy;
            // 色とアルファ値を少しずつ変化させる
            int alpha = Color.alpha(particle.color);
            int red = Color.red(particle.color);
            int green = Color.green(particle.color);
            int blue = Color.blue(particle.color);
            float radius = particle.radius;
            radius += random.nextFloat() / 3;
            particle.radius = radius;

            // アルファ値を少しずつ変化させる
            alpha += random.nextInt(5) - 2;
            if (alpha < 0) alpha = 0;
            if (alpha > 255) alpha = 255;

            // 緑の色味を少しずつ変化させる
            green += random.nextInt(5) - 2;
            if (green < 0) green = 0;
            if (green > 255) green = 255;
            particle.color = Color.argb(alpha, red, green, blue);

            if (particle.x < 0 || particle.x > getWidth()) {
                particle.vx = -particle.vx;
            }
            if (particle.y < 0 || particle.y > getHeight()) {
                particle.vy = -particle.vy;
            }
        }
        particles = particles.stream()
                .filter(particle -> particle.radius < 30)
                .collect(Collectors.toList());
        for (int i = particles.size(); i < particleCount; i++) {
            particles.add(new Particle());
        }
    }

    private class Particle {
        float x, y, vx, vy, radius;
        int color;

        Particle() {
            x = random.nextInt(getWidth());
            y = random.nextInt(getHeight());
            vx = random.nextFloat() * 4 - 2;
            vy = random.nextFloat() * 4 - 2;
            radius = random.nextFloat() * 5 + 5;
            // 色を暖色系に設定
            int r = 220; // 赤を最大に
            int g = random.nextInt(256); // 緑をランダムに
            int b = 0; // 青を最小に
            int alpha = random.nextInt(256); // 透明度をランダムに

            color = Color.argb(alpha, r, g, b);
        }
    }
}