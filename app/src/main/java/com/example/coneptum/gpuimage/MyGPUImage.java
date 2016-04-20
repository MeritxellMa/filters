package com.example.coneptum.gpuimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageRenderer;
import jp.co.cyberagent.android.gpuimage.Rotation;

/**
 * Created by coneptum on 13/04/16.
 */
public class MyGPUImage extends GPUImage {

    private final Context mContext;
    private float degrees;
    private int panX;
    private int panY;
    private float scaleFactor;
    private MyRenderer mRenderer;
    private Bitmap mCurrentBitmap;
    private ScaleType mScaleType;
    private Uri mUri;
    private GPUImageFilter mFilter;

    /**
     * Instantiates a new GPUImage object.
     *
     * @param context the context
     */
    public MyGPUImage(Context context) {
        super(context);
        mContext = context;
        mScaleType = ScaleType.CENTER_CROP;
        scaleFactor = 1;
        panX=0;
        panY=0;


        try {
            GPUImage object = new GPUImage(context);

            //assignem el renderer (private) de gpuimage al nostre
            /*Field privateField = GPUImage.class.
                    getDeclaredField("mRenderer");
            privateField.setAccessible(true);
            mRenderer = (GPUImageRenderer) privateField.get(object);*/
/*            Field privateField = GPUImage.class.
                    getDeclaredField("mFilter");
            privateField.setAccessible(true);
            mFilter=(GPUImageFilter) privateField.get(object);*/
            mFilter=new GPUImageFilter();
            mRenderer=new MyRenderer(mFilter, mContext);


            //assignem el bitmap (private) al nostre
            Field privateField2 = GPUImage.class.
                    getDeclaredField("mCurrentBitmap");
            privateField2.setAccessible(true);
            mCurrentBitmap = (Bitmap) privateField2.get(object);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setImage(Uri uri) {
        mUri = uri;
        new LoadImageUriTask(this, uri).execute();
    }

    public void setImage(Uri uri, float degrees, float scaleFactor) {
        this.degrees = degrees;
        this.scaleFactor = scaleFactor;
        setImage(uri);
    }
    public void revert() {
        this.degrees = 0;
        this.scaleFactor = 1;
        setRotation(Rotation.NORMAL);
        if (mUri != null) {
            setImage(mUri);
        }else{
            Log.i("Error setting degrees:","Set an image first");
        }
    }

    public void setDegrees(float degrees) {
       /* this.degrees = degrees;
        if (mUri != null) {
            setImage(mUri);
        }else{
            Log.i("Error setting degrees:","Set an image first");
        }*/
        //mRenderer.setDegrees(degrees);
        //mFilter=new GPUImageBrightnessFilter(0);
        setRotation(90);
        setFilter(mFilter);
    }

    private void setRotation(float degrees) {
        Log.d("WARNING", "MyGPUImage setRotation");
        MyFilter f=new MyFilter();
        setFilter(f);
        //mRenderer.setRotation(degrees, mCurrentBitmap);
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        if (mUri != null) {
            setImage(mUri);
        }else{
            Log.i("Error setting scale:","Set an image first");
        }
    }

    private int getOutputWidth() {
        if (mRenderer != null && getRendererWidth() != 0) {
            return getRendererWidth();
        } else if (mCurrentBitmap != null) {
            return mCurrentBitmap.getWidth();
        } else {
            WindowManager windowManager =
                    (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            return display.getWidth();
        }
    }

    private int getOutputHeight() {

        if (mRenderer != null && getRenedererHeight() != 0) {
            return getRenedererHeight();
        } else if (mCurrentBitmap != null) {
            return mCurrentBitmap.getHeight();
        } else {
            WindowManager windowManager =
                    (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            return display.getHeight();
        }
    }

    public int getRendererWidth() {
        //access a les values del render
        GPUImageRenderer privateObject = mRenderer;
        int returnWidth = 0;
        Method privateWidthMethod;
        try {
            privateWidthMethod = GPUImageRenderer.class.
                    getDeclaredMethod("getFrameWidth");
            privateWidthMethod.setAccessible(true);
            returnWidth = (int) privateWidthMethod.invoke(privateObject);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return returnWidth;
    }

    public int getRenedererHeight() {
        //access a les values del render
        GPUImageRenderer privateObject = mRenderer;
        int returnHeight = 0;
        Method privateHeightMethod;
        try {
            privateHeightMethod = GPUImageRenderer.class.
                    getDeclaredMethod("getFrameHeight");
            privateHeightMethod.setAccessible(true);
            returnHeight = (int) privateHeightMethod.invoke(privateObject);

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return returnHeight;
    }

    public void setPan(int panX, int panY) {
        this.panX = panX;
        this.panY=panY;
        if (mUri != null) {
            setImage(mUri);
        } else {
            Log.i("Error setting pan:", "Set an image first");
        }
    }


    private abstract class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

        private final GPUImage mGPUImage;
        private int mOutputWidth;
        private int mOutputHeight;

        @SuppressWarnings("deprecation")
        public LoadImageTask(final GPUImage gpuImage) {
            mGPUImage = gpuImage;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            if (mRenderer != null && getRendererWidth() == 0) {
                try {
                    synchronized (mRenderer.mSurfaceChangedWaiter) {
                        mRenderer.mSurfaceChangedWaiter.wait(3000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mOutputWidth = getOutputWidth();
            mOutputHeight = getOutputHeight();
            return loadResizedImage();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mGPUImage.deleteImage();
            mGPUImage.setImage(bitmap);
            mCurrentBitmap=bitmap;
        }

        protected abstract Bitmap decode(BitmapFactory.Options options);

        private Bitmap loadResizedImage() {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            decode(options);
            int scale = 1;
            while (checkSize(options.outWidth / scale > mOutputWidth, options.outHeight / scale > mOutputHeight)) {
                scale++;
            }

            scale--;
            if (scale < 1) {
                scale = 1;
            }
            options = new BitmapFactory.Options();
            options.inSampleSize = scale;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inPurgeable = true;
            options.inTempStorage = new byte[32 * 1024];
            Bitmap bitmap = decode(options);
            if (bitmap == null) {
                return null;
            }
            bitmap = rotateImage(bitmap);
            bitmap = scaleBitmap(bitmap);
            bitmap = moveImage(bitmap);
            return bitmap;
        }

        private Bitmap scaleBitmap(Bitmap bitmap) {
            // resize to desired dimensions
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] newSize = getScaleSize(width, height);
            Bitmap workBitmap = Bitmap.createScaledBitmap(bitmap, newSize[0], newSize[1], true);
            if (workBitmap != bitmap) {
                bitmap.recycle();
                bitmap = workBitmap;
                System.gc();
            }
/*
            if (mScaleType == ScaleType.CENTER_CROP) {
                // Crop it
                int diffWidth = newSize[0] - mOutputWidth;
                int diffHeight = newSize[1] - mOutputHeight;
                workBitmap = Bitmap.createBitmap(bitmap, diffWidth / 2, diffHeight / 2,
                        newSize[0] - diffWidth, newSize[1] - diffHeight);
                if (workBitmap != bitmap) {
                    bitmap.recycle();
                    bitmap = workBitmap;
                }
            }*/

            return bitmap;
        }

        /**
         * Retrieve the scaling size for the image dependent on the ScaleType.<br>
         * <br>
         * If CROP: sides are same size or bigger than output's sides<br>
         * Else   : sides are same size or smaller than output's sides
         */
        private int[] getScaleSize(int width, int height) {
            float newWidth;
            float newHeight;

            float widthRatio = (float) width / mOutputWidth;
            float heightRatio = (float) height / mOutputHeight;

            boolean adjustWidth = mScaleType == ScaleType.CENTER_CROP
                    ? widthRatio > heightRatio : widthRatio < heightRatio;
            Log.i("width / mOutputWidth", widthRatio + "");
            Log.i("height / mOutputHeight", heightRatio + "");

            if (adjustWidth) {
                newHeight = mOutputHeight;
                newWidth = (newHeight / height) * width;
            } else {
                newWidth = mOutputWidth;
                newHeight = (newWidth / width) * height;
            }

            if (scaleFactor >= 1) {

                newHeight *= scaleFactor;
                newWidth *= scaleFactor;
            }
            Log.i("scaleImagewidth", newWidth + "");
            Log.i("scaleImageHeight", newHeight + "");

            return new int[]{Math.round(newWidth), Math.round(newHeight)};
        }

        private boolean checkSize(boolean widthBigger, boolean heightBigger) {
            if (mScaleType == ScaleType.CENTER_CROP) {
                return widthBigger && heightBigger;
            } else {
                return widthBigger || heightBigger;
            }
        }

        private Bitmap rotateImage(final Bitmap bitmap) {
            if (bitmap == null) {
                return null;
            }
            Bitmap rotatedBitmap = bitmap;

            float orientation = degrees;
            if (orientation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);
                bitmap.recycle();
            }

            return rotatedBitmap;
        }

        private Bitmap moveImage(final Bitmap bitmap) {
            if (bitmap == null) {
                return null;
            }
            Bitmap movedBitmap = bitmap;
            Matrix matrix = new Matrix();
            //matrix.postTranslate(pan, pan);

            Log.i("panX", panX+"");
            Log.i("panY", panY+"");
            Log.i("bitmapWidth", bitmap.getWidth()+"");
            Log.i("bitmapHeight", bitmap.getHeight()+"");
            Log.i("bitmapWidth -panX", bitmap.getWidth()-panX+"");
            Log.i("bitmapHeight-panY", bitmap.getHeight() - panY + "");
            Log.i("bitmap null?", (bitmap==null)+"");

/*
            if(bitmap.getHeight()-panY>0&&bitmap.getWidth()-panX>0) {
                setScaleType(ScaleType.CENTER_CROP);
                movedBitmap = Bitmap.createBitmap(bitmap, panX, panY, bitmap.getWidth()-panX,
                        bitmap.getHeight()-panY, matrix, true);
                bitmap.recycle();
            }*/
/*            else{
                panX=bitmap.getWidth()+1;
                panY=bitmap.getHeight()+1;
                movedBitmap = Bitmap.createBitmap(bitmap, panX, panY, bitmap.getWidth() - panX,
                        bitmap.getHeight() - panY, matrix, true);
            }*/




            return movedBitmap;
        }

    }

    private class LoadImageUriTask extends LoadImageTask {

        private final Uri mUri;

        public LoadImageUriTask(GPUImage gpuImage, Uri uri) {
            super(gpuImage);
            mUri = uri;
        }

        @Override
        protected Bitmap decode(BitmapFactory.Options options) {
            try {
                InputStream inputStream;
                if (mUri.getScheme().startsWith("http") || mUri.getScheme().startsWith("https")) {
                    inputStream = new URL(mUri.toString()).openStream();
                } else {
                    inputStream = mContext.getContentResolver().openInputStream(mUri);
                }
                return BitmapFactory.decodeStream(inputStream, null, options);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }


}