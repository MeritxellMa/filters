package com.example.coneptum.gpuimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.Arrays;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageRenderer;
import jp.co.cyberagent.android.gpuimage.Rotation;
import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;

/**
 * Created by coneptum on 19/04/16.
 */
public class MyRenderer extends GPUImageRenderer {
    static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };
    private FloatBuffer mGLCubeBuffer;
    private FloatBuffer mGLTextureBuffer;
    private float mDegrees;
    private Rotation mRotation;
    private GPUImageFilter mFilter;
    private Uri mUri;
    private int mImageWidth;
    private int mImageHeight;
    private Context context;
    private Bitmap mBitmap;
    private GPUImageRenderer object;
    private int mGLTextureId;


    public MyRenderer(GPUImageFilter filter, Context context) throws IllegalAccessException, NoSuchFieldException {
        super(filter);
        mFilter = filter;
        this.context = context;
        object = this;
    }

    private void adjustImageScaling() {
        getBitmapBounds();
        //TODO output igual a image bounds per proves pero no es aixi
        float outputWidth = mImageWidth;
        float outputHeight = mImageHeight;
        if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
            outputWidth = mImageHeight;
            outputHeight = mImageWidth;
        }

        float ratio1 = outputWidth / mImageWidth;
        float ratio2 = outputHeight / mImageHeight;
        float ratioMax = Math.max(ratio1, ratio2);

        int imageWidthNew = Math.round(mImageWidth * ratioMax);
        int imageHeightNew = Math.round(mImageHeight * ratioMax);
        Log.d("WARNING", "imageHeightNew:" + imageHeightNew);
        Log.d("WARNING", "imageWidthNew:" + imageWidthNew);
        Log.d("WARNING", "ratiomax:" + ratioMax);
        Log.d("WARNING", "currentBitmapWidth:" + mImageWidth);
        Log.d("WARNING", "outputHeight:" + outputHeight);
        Log.d("WARNING", "outputWidth:" + outputWidth);

        float ratioWidth = imageWidthNew / outputWidth;
        float ratioHeight = imageHeightNew / outputHeight;

        Log.d("WARNING", "ratioWidth:" + ratioWidth);
        Log.d("WARNING", "ratioHeight:" + ratioHeight);

        float[] cube = CUBE;
        Log.d("WARNING", "cube:" + Arrays.toString(cube));
        float[] textureCords = TextureRotationUtil.getRotation(mRotation, false, false);

        Log.d("WARNING", "texture:" + Arrays.toString(textureCords));
        if (GPUImage.ScaleType.CENTER_CROP == GPUImage.ScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            Log.d("WARNING", "distHorizontal:" + distHorizontal);
            float distVertical = (1 - 1 / ratioHeight) / 2;
            Log.d("WARNING", "disVertical:" + distVertical);
            textureCords = new float[]{
                    addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
                    addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
                    addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
                    addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical),
            };

            Log.d("WARNING", "texture:" + Arrays.toString(textureCords));
        } else {
            cube = new float[]{
                    CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
                    CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
                    CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
                    CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,
            };
        }

        getGLCubeBuffer().clear();
        getGLCubeBuffer().put(cube).position(0);
        getGLTextureBuffer().clear();
        getGLTextureBuffer().put(textureCords).position(0);
    }

    private FloatBuffer getGLCubeBuffer() {
        Field privateField;
        try {
            privateField = GPUImageRenderer.class.getDeclaredField("mGLCubeBuffer");
            privateField.setAccessible(true);
            return (FloatBuffer) privateField.get(object);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private FloatBuffer getGLTextureBuffer() {
        Field privateField;
        try {
            privateField = GPUImageRenderer.class.getDeclaredField("mGLTextureBuffer");
            privateField.setAccessible(true);
            return (FloatBuffer) privateField.get(object);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    public void getBitmapBounds() {
        mImageWidth = mBitmap.getWidth();
        mImageHeight = mBitmap.getHeight();
    }


}
