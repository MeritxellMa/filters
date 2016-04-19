package com.example.coneptum.gpuimage;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageRenderer;

/**
 * Created by coneptum on 19/04/16.
 */
public class MyRenderer extends GPUImageRenderer {

    private float degrees;

    public MyRenderer(GPUImageFilter filter) {
        super(filter);
    }
}
