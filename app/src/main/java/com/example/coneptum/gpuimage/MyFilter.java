package com.example.coneptum.gpuimage;

import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.FloatBuffer;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by coneptum on 20/04/16.
 */
public class MyFilter extends GPUImageFilter {

/*    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    private final float[] mTranslateMatrix = new float[16];*/

    public MyFilter(){
    }


    @Override
    public void onDraw(int textureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        super.onDraw(textureId, cubeBuffer, textureBuffer);
        /*Matrix.setIdentityM(mModelMatrix, 0); // initialize to identity matrix
       // Matrix.translateM(mModelMatrix, 0, -0.5f, 0, 0); // translation to the left
        Matrix.rotateM(mModelMatrix, 0, 45, 0, 0, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);*/
    }

}
