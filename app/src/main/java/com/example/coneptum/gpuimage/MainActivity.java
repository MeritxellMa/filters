package com.example.coneptum.gpuimage;

import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageMonochromeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageTransformFilter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    private MyGPUImage mGPUImage;
    private Button revertButton;
    private ToggleButton brightnessButton;
    private ToggleButton colorButton;
    private ToggleButton monoColorButton;
    private GPUImageFilterGroup imageFilterGroup;
    private GPUImageBrightnessFilter brightnessFilter;
    private GPUImageSaturationFilter saturationFilter;
    private GPUImageContrastFilter contrastFilter;
    private GPUImageMonochromeFilter monochromeFilter;
    private GPUImageTransformFilter transformFilter;
    private SeekBar seekbar;
    private float brightness;
    private float saturation;
    private float contrast;
    private float monocolor;
    private float degrees;
    private ToggleButton scaleButton;
    private ToggleButton rotateButton;
    private ToggleButton panX;
    private ToggleButton panY;
    private Uri mUri;
    private SeekBar upperSeekbar;
    private GLSurfaceView glSurfaceView;
    private float[] rotateM = new float[16];
    private float[] scaleM = new float[16];
    private float[] panXM = new float[16];
    private float[] panYM = new float[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUri = Uri.parse("android.resource://com.example.coneptum.gpuimage/drawable/image");
        mGPUImage = new MyGPUImage(this);
        glSurfaceView = (GLSurfaceView) findViewById(R.id.surface_view);
        mGPUImage.setGLSurfaceView(glSurfaceView);
        mGPUImage.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
        // mGPUImage.setImage(uri); // this loads image on the current thread, should be run in a thread

        mGPUImage.setImage(mUri);

        seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbar.setMax(200);
        seekbar.setOnSeekBarChangeListener(this);

        upperSeekbar = (SeekBar) findViewById(R.id.seekbarRotate);
        upperSeekbar.setMax(360);
        upperSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (rotateButton.isChecked()) {
                    degrees = progress;
                    Matrix.setRotateM(rotateM, 0, progress, 0, 0, -1f);
                    transformFilter.setTransform3D(rotateM);
                } else if (scaleButton.isChecked()) {
                    //TODO aixo esta pq si no existeix transform3d fa coses molt rares---ineficient...boolean?
                    if (degrees == 0) {
                        Matrix.setRotateM(rotateM, 0, 0, 0, 0, -1f);
                        transformFilter.setTransform3D(rotateM);
                    }

                    float scaleFactor = (float) (progress) / 100 + 1; // de 1 a 4.6
                    Log.i("scalefactor", scaleFactor + "");
                    Matrix.scaleM(scaleM, 0, transformFilter.getTransform3D(), 0, scaleFactor, scaleFactor, 1);
                    transformFilter.setTransform3D(scaleM);
                } else if (panX.isChecked()) {
                    //TODO aixo esta pq si no existeix transform3d fa coses molt rares---ineficient...boolean?
                    if (degrees == 0) {
                        Matrix.setRotateM(rotateM, 0, 0, 0, 0, -1f);
                        transformFilter.setTransform3D(rotateM);
                    }

                    float panX = (float) (progress) / 180 - 1;
                    Log.i("panX", panX + "");
                    Matrix.translateM(panXM, 0, transformFilter.getTransform3D(), 0, panX, 0, 0);
                    transformFilter.setTransform3D(panXM);

                }else{
                    //TODO aixo esta pq si no existeix transform3d fa coses molt rares---ineficient...boolean?
                    if (degrees == 0) {
                        Matrix.setRotateM(rotateM, 0, 0, 0, 0, -1f);
                        transformFilter.setTransform3D(rotateM);
                    }
                    float panY = (float) (progress) / 180 - 1;
                    Log.i("panY", panY + "");
                    Matrix.translateM(panYM, 0, transformFilter.getTransform3D(), 0, 0, panY, 0);
                    transformFilter.setTransform3D(panYM);
                }
                applyFilters();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        revertButton = (Button) findViewById(R.id.revert);
        brightnessButton = (ToggleButton) findViewById(R.id.brightness);
        colorButton = (ToggleButton) findViewById(R.id.color);
        monoColorButton = (ToggleButton) findViewById(R.id.monocolor);

        revertButton.setOnClickListener(this);
        brightnessButton.setOnClickListener(this);
        colorButton.setOnClickListener(this);
        monoColorButton.setOnClickListener(this);

        //testing scale, translate and rotate
        scaleButton = (ToggleButton) findViewById(R.id.scale);
        rotateButton = (ToggleButton) findViewById(R.id.rotate);
        panX = (ToggleButton) findViewById(R.id.panX);
        panY = (ToggleButton) findViewById(R.id.panY);

        scaleButton.setOnCheckedChangeListener(this);
        rotateButton.setOnCheckedChangeListener(this);
        panX.setOnCheckedChangeListener(this);
        panX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float[] prova = transformFilter.getTransform3D();
                Matrix.translateM(prova, 0, panXM, 0, 100, 0, 0);
                transformFilter.setTransform3D(prova);
                applyFilters();
            }
        });
        panY.setOnCheckedChangeListener(this);


        //filters
        brightnessFilter = new GPUImageBrightnessFilter();
        saturationFilter = new GPUImageSaturationFilter();
        contrastFilter = new GPUImageContrastFilter();
        transformFilter = new GPUImageTransformFilter();


        transformFilter.setTransform3D(rotateM);

        float[] monoInitValues = {0.5f, 0.5f, 0.5f, 1};
        monochromeFilter = new GPUImageMonochromeFilter(1, monoInitValues);

        //setInitFilterValues();
        setImageGroupFilters();
    }

    private void setImageGroupFilters() {
        imageFilterGroup = new GPUImageFilterGroup();
        imageFilterGroup.addFilter(transformFilter);
        imageFilterGroup.addFilter(brightnessFilter);
        imageFilterGroup.addFilter(saturationFilter);
        imageFilterGroup.addFilter(contrastFilter);
    }

    private void setInitFilterValues() {
        brightness = 0;
        brightnessFilter.setBrightness(brightness);
        saturation = 1;
        saturationFilter.setSaturation(saturation);
        contrast = 1;
        contrastFilter.setContrast(contrast);
        monocolor = 0.5f;
        monochromeFilter.setIntensity(1);
        float[] monoInitValues = {0.5f, 0.5f, 0.5f, 1};
        monochromeFilter.setColor(monoInitValues);
        transformFilter.onDestroy();
        transformFilter = new GPUImageTransformFilter();
        upperSeekbar.setProgress(180);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.revert:
                revertFilters();
                mGPUImage.revert();
                break;
            case R.id.brightness:
                setButton(brightnessButton);
                break;
            case R.id.color:
                setButton(colorButton);
                break;
            case R.id.monocolor:
                if (monoColorButton.isChecked()) {
                    monoFilterSettings(true);
                    setButtonProgress();
                    seekbar.setVisibility(View.VISIBLE);
                } else {
                    monoFilterSettings(false);
                    if (colorButton.isChecked()) {
                        setButton(colorButton);
                    } else if (brightnessButton.isChecked()) {
                        setButton(brightnessButton);
                    } else {
                        seekbar.setVisibility(View.INVISIBLE);
                    }
                }

                break;
        }
    }

    private void monoFilterSettings(boolean activate) {
        if (activate) {
            imageFilterGroup.addFilter(monochromeFilter);
        } else {
            setImageGroupFilters();
            monoColorButton.setChecked(false);
            seekbar.setVisibility(View.INVISIBLE);
        }
        applyFilters();
    }


    private void setButtonProgress() {
        float progress;
        if (brightnessButton.isChecked()) {
            progress = (brightness + 1) * 100;
            seekbar.setProgress((int) progress);
        } else if (colorButton.isChecked()) {
            progress = (saturation) * 100;
            seekbar.setProgress((int) progress);
        } else {
            progress = monocolor * 100;
            seekbar.setProgress((int) progress);
        }
    }

    private void revertFilters() {
        setInitFilterValues();
        setButtonProgress();
        monoFilterSettings(false);
        colorButton.setChecked(false);
        brightnessButton.setChecked(false);
        applyFilters();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float value = progress / 100f - 1;
        if (brightnessButton.isChecked()) {
            brightnessFilter.setBrightness(value);
            brightness = value;
            Log.i("brillo", value + "");
        } else if (colorButton.isChecked()) {
            saturation = value + 1;
            saturationFilter.setSaturation(saturation);
            Log.i("saturation", value + "");
            if (value >= 0) {
                contrast = 1.5f * value + 1;
                contrastFilter.setContrast(contrast);
            } else {
                contrast = 1;
                contrastFilter.setContrast(contrast);
            }
            Log.i("contrast", value + "");
        } else {
           /* monocolor=value+1;
            monochromeFilter.setIntensity(1);
            float monoValue = monocolor;
            float[] monoValues = {monoValue, monoValue, monoValue, monoValue};
            monochromeFilter.setColor(monoValues);*/
        }
        /*
        if (colorVal >=0){
            self.saturationFilter.saturation=1+colorVal;
            self.contrastFilter.contrast=1+3*colorVal*0.5;
        } else{
            self.contrastFilter.contrast=1.;
            self.saturationFilter.saturation=1+colorVal;
        }
         */

        applyFilters();
    }

    private void applyFilters() {
        mGPUImage.setFilter(imageFilterGroup);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    public void setButton(CompoundButton button) {
        brightnessButton.setChecked(false);
        colorButton.setChecked(false);
        button.setChecked(true);
        seekbar.setVisibility(View.VISIBLE);
        setButtonProgress();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (panX.isChecked() || panY.isChecked()) {
                upperSeekbar.setProgress(180);
            } else {
                upperSeekbar.setProgress(0);
            }
            scaleButton.setChecked(false);
            rotateButton.setChecked(false);
            panY.setChecked(false);
            panX.setChecked(false);
            buttonView.setChecked(true);
        }
    }
}
