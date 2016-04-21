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
    private float scaleFactor;
    private float panY;
    private float panX;
    private ToggleButton scaleButton;
    private ToggleButton rotateButton;
    private ToggleButton panXButton;
    private ToggleButton panYButton;
    private SeekBar upperSeekbar;
    private float[] rotateM = new float[16];
    private float[] scaleM = new float[16];
    private float[] panXM = new float[16];
    private float[] panYM = new float[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init GPU components
        mGPUImage = new MyGPUImage(this);
        mGPUImage.setGLSurfaceView((GLSurfaceView) findViewById(R.id.surface_view));
        mGPUImage.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);

        //set image
        Uri mUri = Uri.parse("android.resource://com.example.coneptum.gpuimage/drawable/image");
        mGPUImage.setImage(mUri);

        //**testing scale, translate and rotate**//
        //set transformation seekBar
        upperSeekbar = (SeekBar) findViewById(R.id.seekbarRotate);
        if (upperSeekbar != null) {
            upperSeekbar.setMax(360);
            upperSeekbar.setOnSeekBarChangeListener(this);
        }

        //set buttons
        scaleButton = (ToggleButton) findViewById(R.id.scale);
        rotateButton = (ToggleButton) findViewById(R.id.rotate);
        panXButton = (ToggleButton) findViewById(R.id.panX);
        panYButton = (ToggleButton) findViewById(R.id.panY);
        //set listeners
        scaleButton.setOnCheckedChangeListener(this);
        rotateButton.setOnCheckedChangeListener(this);
        panXButton.setOnCheckedChangeListener(this);
        panYButton.setOnCheckedChangeListener(this);

        //**testing filters**//
        //set filters seekBar
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        if (seekbar != null) {
            seekbar.setMax(200);
            seekbar.setOnSeekBarChangeListener(this);
        }

        //init filters
        brightnessFilter = new GPUImageBrightnessFilter();
        saturationFilter = new GPUImageSaturationFilter();
        contrastFilter = new GPUImageContrastFilter();
        transformFilter = new GPUImageTransformFilter();
        monochromeFilter=new GPUImageMonochromeFilter();

        //set buttons
        Button revertButton = (Button) findViewById(R.id.revert);
        brightnessButton = (ToggleButton) findViewById(R.id.brightness);
        colorButton = (ToggleButton) findViewById(R.id.color);
        monoColorButton = (ToggleButton) findViewById(R.id.monocolor);
        //Set listeners
        if (revertButton != null) {
            revertButton.setOnClickListener(this);
        }
        brightnessButton.setOnClickListener(this);
        colorButton.setOnClickListener(this);
        monoColorButton.setOnClickListener(this);

        setInitFilterValues();
        setImageGroupFilters();

    }

    private void setImageGroupFilters() {
        imageFilterGroup = new GPUImageFilterGroup();
        imageFilterGroup.addFilter(transformFilter);
        imageFilterGroup.addFilter(brightnessFilter);
        imageFilterGroup.addFilter(saturationFilter);
        imageFilterGroup.addFilter(contrastFilter);
        applyFilters();
    }

    private void setInitFilterValues() {
        //brightness
        brightness = 0;
        brightnessFilter.setBrightness(brightness);
        //color
        saturation = 1;
        saturationFilter.setSaturation(saturation);
        contrast = 1;
        contrastFilter.setContrast(contrast);
        //monochrome
        monocolor = 0.5f;
        monochromeFilter.setIntensity(1);
        float[] monoInitValues = {0.5f, 0.5f, 0.5f, 1};
        monochromeFilter.setColor(monoInitValues);
        //transform
        scaleFactor = 1;
        degrees=0;
        panX=0;
        panY=0;
        Matrix.setRotateM(rotateM, 0, degrees, 0, 0, -1f);
        transformFilter.setTransform3D(rotateM);
        //seekBars
        seekbar.setVisibility(View.INVISIBLE);
        seekbar.setProgress(100);
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
        if (seekBar == this.seekbar) {

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
        /* TONI'S CODE
        if (colorVal >=0){
            self.saturationFilter.saturation=1+colorVal;
            self.contrastFilter.contrast=1+3*colorVal*0.5;
        } else{
            self.contrastFilter.contrast=1.;
            self.saturationFilter.saturation=1+colorVal;
        }
         */
        } else {
            if (rotateButton.isChecked()) {
                degrees = progress;
            } else if (scaleButton.isChecked()) {
                scaleFactor = (float) (progress) / 100 + 1; // de 1 a 4.6
                Log.i("scalefactor", scaleFactor + "");
            } else if (panXButton.isChecked()) {
                panX = (float) (progress) / 180 - 1;
                Log.i("panX", panX + "");
            } else {
                panY = (float) (progress) / 180 - 1;
                Log.i("panY", panY + "");
            }
            Matrix.setRotateM(rotateM, 0, degrees, 0, 0, -1f);
            transformFilter.setTransform3D(rotateM);
            Matrix.scaleM(scaleM, 0, transformFilter.getTransform3D(), 0, scaleFactor, scaleFactor, 1);
            transformFilter.setTransform3D(scaleM);
            Matrix.translateM(panXM, 0, transformFilter.getTransform3D(), 0, panX, 0, 0);
            transformFilter.setTransform3D(panXM);
            Matrix.translateM(panYM, 0, transformFilter.getTransform3D(), 0, 0, panY, 0);
            transformFilter.setTransform3D(panYM);
        }

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
            scaleButton.setChecked(false);
            rotateButton.setChecked(false);
            panYButton.setChecked(false);
            panXButton.setChecked(false);
            buttonView.setChecked(true);
            if (rotateButton.isChecked()) {
                upperSeekbar.setProgress((int) degrees);
            } else if (scaleButton.isChecked()) {
                upperSeekbar.setProgress((int) ((scaleFactor - 1) * 100));
            } else if (panXButton.isChecked()) {
                upperSeekbar.setProgress((int) ((panX + 1) * 180));
            } else {
                upperSeekbar.setProgress((int) ((panY + 1) * 180));
            }
        }
    }
}
