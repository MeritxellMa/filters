package com.example.coneptum.gpuimage;

import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageMonochromeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

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
    private SeekBar seekbar;
    private float brightness;
    private float saturation;
    private float contrast;
    private float monocolor;
    private Button scaleButton;
    private Button rotateButton;
    private EditText scaleFactor;
    private EditText degrees;
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUri = Uri.parse("android.resource://com.example.coneptum.gpuimage/drawable/image");
        mGPUImage = new MyGPUImage(this);
        mGPUImage.setGLSurfaceView((GLSurfaceView) findViewById(R.id.surface_view));
        mGPUImage.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
       // mGPUImage.setImage(uri); // this loads image on the current thread, should be run in a thread

        mGPUImage.setImage(mUri);

        seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbar.setMax(200);
        seekbar.setOnSeekBarChangeListener(this);

        revertButton = (Button) findViewById(R.id.revert);
        brightnessButton = (ToggleButton) findViewById(R.id.brightness);
        colorButton = (ToggleButton) findViewById(R.id.color);
        monoColorButton = (ToggleButton) findViewById(R.id.monocolor);

        revertButton.setOnClickListener(this);
        brightnessButton.setOnClickListener(this);
        colorButton.setOnClickListener(this);
        monoColorButton.setOnClickListener(this);

        //testing scale and rotate
        scaleButton=(Button)findViewById(R.id.scale_button);
        rotateButton=(Button)findViewById(R.id.rotate_button);
        scaleFactor=(EditText)findViewById(R.id.scale_factor);
        degrees=(EditText)findViewById(R.id.degrees);

        scaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float factorToScale=Float.parseFloat(scaleFactor.getText().toString());
                mGPUImage.setScaleFactor(factorToScale);
            }
        });

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float degreesToRotate=Float.parseFloat(degrees.getText().toString());
                mGPUImage.setDegrees(degreesToRotate);
            }
        });

        brightnessFilter = new GPUImageBrightnessFilter();
        saturationFilter = new GPUImageSaturationFilter();
        contrastFilter = new GPUImageContrastFilter();

        float[] monoInitValues = {0.5f,0.5f, 0.5f, 1};
        monochromeFilter = new GPUImageMonochromeFilter(1, monoInitValues);

        //setInitFilterValues();
        setImageGroupFilters();
    }

    private void setImageGroupFilters() {
        imageFilterGroup = new GPUImageFilterGroup();
        imageFilterGroup.addFilter(brightnessFilter);
        imageFilterGroup.addFilter(saturationFilter);
        imageFilterGroup.addFilter(contrastFilter);
    }

    private void setInitFilterValues() {
        brightness=0;
        brightnessFilter.setBrightness(brightness);
        saturation=1;
        saturationFilter.setSaturation(saturation);
        contrast=1;
        contrastFilter.setContrast(contrast);
        monocolor=0.5f;
        monochromeFilter.setIntensity(1);
        float[] monoInitValues = {0.5f,0.5f, 0.5f, 1};
        monochromeFilter.setColor(monoInitValues);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.revert:
                revertFilters();
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
            progress=(brightness + 1) * 100;
            seekbar.setProgress((int) progress);
        } else if (colorButton.isChecked()) {
            progress=(saturation) * 100;
            seekbar.setProgress((int) progress);
        } else {
            progress=monocolor*100;
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
            brightness=value;
            Log.i("brillo", value + "");
        } else if (colorButton.isChecked()) {
            saturation=value+1;
            saturationFilter.setSaturation(saturation);
            Log.i("saturation", value + "");
            if (value >= 0) {
                contrast=1.5f*value+1;
                contrastFilter.setContrast(contrast);
            } else {
                contrast=1;
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

}
