package com.example.toothbrush.facemeshdetector;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;

import com.example.toothbrush.GraphicOverlay;
import com.example.toothbrush.GraphicOverlay.Graphic;
import com.google.mlkit.vision.facemesh.FaceMesh;
import com.google.mlkit.vision.facemesh.FaceMeshPoint;

import java.util.List;

/**
 * Graphic instance for rendering face position and mesh info within the associated graphic overlay
 * view.
 */
public class FaceMeshGraphic extends Graphic {
  private static final float BOX_STROKE_WIDTH = 5.0f;

  private final Paint positionPaint;
  private final Paint boxPaint;
  private final FaceMesh faceMesh;
  private String lastText = "";

  FaceMeshGraphic(GraphicOverlay overlay, FaceMesh faceMesh) {
    super(overlay);

    this.faceMesh = faceMesh;
    final int selectedColor = Color.WHITE;

    positionPaint = new Paint();
    positionPaint.setColor(selectedColor);

    boxPaint = new Paint();
    boxPaint.setColor(selectedColor);
    boxPaint.setStyle(Style.STROKE);
    boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

  }

  /** Draws the face annotations for position on the supplied canvas. */
  @Override
  public void draw(Canvas canvas) {
    String text = "-";

    if (faceMesh == null) {
      sendMessage(text);
      lastText = text;
      return;
    }

    // Draws the bounding box.
    RectF rect = new RectF(faceMesh.getBoundingBox());
    // If the image is flipped, the left will be translated to right, and the right to left.
    float x0 = translateX(rect.left);
    float x1 = translateX(rect.right);
    rect.left = min(x0, x1);
    rect.right = max(x0, x1);
    rect.top = translateY(rect.top);
    rect.bottom = translateY(rect.bottom);

    // 上唇の下
    List<FaceMeshPoint> upperLipBottoms = faceMesh.getPoints(FaceMesh.UPPER_LIP_BOTTOM);
    List<FaceMeshPoint> upperLipTop = faceMesh.getPoints(FaceMesh.UPPER_LIP_TOP);
    List<FaceMeshPoint> lowerLipBottom = faceMesh.getPoints(FaceMesh.LOWER_LIP_BOTTOM);
    List<FaceMeshPoint> lowerLipTop = faceMesh.getPoints(FaceMesh.LOWER_LIP_TOP);
    Paint paint = new Paint();
    Path path = new Path();
    paint.setStyle(Style.FILL_AND_STROKE);
    paint.setStrokeWidth(2f);
    paint.setColor(Color.RED);
    paint.setAntiAlias(true);
    FaceMeshPoint start = upperLipBottoms.get(0);
    path.moveTo(
            translateX(start.getPosition().getX()),
            translateY(start.getPosition().getY()));

    for (FaceMeshPoint point : upperLipBottoms) {
      path.lineTo(
              translateX(point.getPosition().getX()),
              translateY(point.getPosition().getY())
      );
    }
    for (int i = upperLipTop.size() - 1; i >= 0; i--) {
      FaceMeshPoint point = upperLipTop.get(i);
      path.lineTo(
              translateX(point.getPosition().getX()),
              translateY(point.getPosition().getY())
      );
    }
    start = lowerLipBottom.get(0);
    path.moveTo(
            translateX(start.getPosition().getX()),
            translateY(start.getPosition().getY()));

    for (FaceMeshPoint point : lowerLipBottom) {
      // スタート地点を移動
      path.lineTo(
              translateX(point.getPosition().getX()),
              translateY(point.getPosition().getY())
      );
    }
    for (int i = lowerLipTop.size() - 1; i >= 0; i--) {
      FaceMeshPoint point = lowerLipTop.get(i);
      // スタート地点を移動
      path.lineTo(
              translateX(point.getPosition().getX()),
              translateY(point.getPosition().getY())
      );

    }

    path.close();
    canvas.drawPath(path, paint);


    // 顔の高さ
    float faceHeight = faceMesh.getBoundingBox().bottom - faceMesh.getBoundingBox().top;
    // 顔の幅
    float faceWidth = faceMesh.getBoundingBox().right - faceMesh.getBoundingBox().left;

    // 唇の開き具合(高さ)
    float lipHeight = lowerLipTop.get(6).getPosition().getY() - upperLipBottoms.get(6).getPosition().getY();
    // 唇の開き具合(横)
    float lipWidth = lowerLipTop.get(12).getPosition().getX() - lowerLipTop.get(0).getPosition().getX();
    if (lipHeight / faceHeight > 0.12 ) {
      // あ, お
      if (lipWidth / faceWidth > 0.25 ) {
        text = "あ";
      } else {
        text = "お";
      }
    } else if (lipHeight / faceHeight > 0.09) {
      // え
      if (lipWidth / faceWidth > 0.25 ) {
        text = "え";
      } else {
        text = "お";
      }
    } else if (lipHeight / faceHeight > 0.05) {
      // い
      if (lipWidth / faceWidth > 0.25 ) {
        text = "い";
      } else {
        text = "お";
      }
    } else if (lipHeight / faceHeight > 0.02) {
      if (lipWidth / faceWidth < 0.20 ) {
        text = "う";
      }
    }
    if (!lastText.equals(text)) {
      sendMessage(text);
      lastText = text;
    }

  }

  protected void sendMessage(String msg){
    Intent broadcast = new Intent();
    broadcast.putExtra("message", msg);
    broadcast.setAction("DO_ACTION");
    getApplicationContext().sendBroadcast(broadcast);
  }

}
