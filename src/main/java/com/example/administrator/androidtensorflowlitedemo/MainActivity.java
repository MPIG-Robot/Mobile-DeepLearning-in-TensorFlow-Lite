package com.example.administrator.androidtensorflowlitedemo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String MODEL_PATH = "mobilenet_quant_v1_224.tflite";
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 224;

    private Classifier classifier;

    private Executor executor = Executors.newSingleThreadExecutor();                                    // SingleThreadExecutor: 仅有一个核心线程的线程池, 任务到来时 自动加入任务队列, 核心线程空闲时按顺序执行任务
    private TextView resultTextView;
    private TextView result2TextView;
    private TextView result3TextView;
    private TextView titleTextView;
    private Button btnDetectObject, btnToggleCamera;
    private ImageView imageViewResult;
    private CameraView cameraView;

    private String resultString;
    private String result2String;
    private String result3String;

    private LinearLayout imageViewResultLinearLayout;
    private ImageView detectObjectImageView;
    private ImageView toggleCameraImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = findViewById(R.id.cameraView);
        imageViewResult = findViewById(R.id.imageViewResult);
        imageViewResultLinearLayout = findViewById(R.id.imageViewResultLinearLayout);

        detectObjectImageView =  findViewById(R.id.detectObjectImageView);
        detectObjectImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnDetectObject.performClick();
            }
        });

        toggleCameraImageView =  findViewById(R.id.toggleCameraImageView);
        toggleCameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnToggleCamera.performClick();
            }
        });

        titleTextView = findViewById(R.id.titleTextView);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(                                                   // 使用support的代码调用, 开启自动调整大小的功能
                titleTextView, TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(                                        // 粒度模式的设置参数, 定义字体调整的最大值和最小值以及每次字体调整的步长, 在调整字体大小的时候, 系统会根据最大值和最小值以及步长来调整字体大小并选择一个最佳字体大小, setAutoSizeTextTypeUniformWithConfiguration(被设置的TextView, 自动缩放的最小字号, 自动缩放的最大字号, 参数二与参数三所用的单位)
                titleTextView, 5, 100,
                1, TypedValue.COMPLEX_UNIT_SP);

        resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setMovementMethod(new ScrollingMovementMethod());
        TextViewCompat.setAutoSizeTextTypeWithDefaults(                                                   // 使用support的代码调用, 开启自动调整大小的功能
                resultTextView, TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(                                        // 粒度模式的设置参数, 定义字体调整的最大值和最小值以及每次字体调整的步长, 在调整字体大小的时候, 系统会根据最大值和最小值以及步长来调整字体大小并选择一个最佳字体大小, setAutoSizeTextTypeUniformWithConfiguration(被设置的TextView, 自动缩放的最小字号, 自动缩放的最大字号, 参数二与参数三所用的单位)
                resultTextView, 5, 100,
                1, TypedValue.COMPLEX_UNIT_SP);

        result2TextView = findViewById(R.id.result2TextView);
        result2TextView.setMovementMethod(new ScrollingMovementMethod());
        TextViewCompat.setAutoSizeTextTypeWithDefaults(                                                   // 使用support的代码调用, 开启自动调整大小的功能
                result2TextView, TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(                                        // 粒度模式的设置参数, 定义字体调整的最大值和最小值以及每次字体调整的步长, 在调整字体大小的时候, 系统会根据最大值和最小值以及步长来调整字体大小并选择一个最佳字体大小, setAutoSizeTextTypeUniformWithConfiguration(被设置的TextView, 自动缩放的最小字号, 自动缩放的最大字号, 参数二与参数三所用的单位)
                result2TextView, 5, 100,
                1, TypedValue.COMPLEX_UNIT_SP);

        result3TextView = findViewById(R.id.result3TextView);
        result3TextView.setMovementMethod(new ScrollingMovementMethod());
        TextViewCompat.setAutoSizeTextTypeWithDefaults(                                                   // 使用support的代码调用, 开启自动调整大小的功能
                result3TextView, TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(                                        // 粒度模式的设置参数, 定义字体调整的最大值和最小值以及每次字体调整的步长, 在调整字体大小的时候, 系统会根据最大值和最小值以及步长来调整字体大小并选择一个最佳字体大小, setAutoSizeTextTypeUniformWithConfiguration(被设置的TextView, 自动缩放的最小字号, 自动缩放的最大字号, 参数二与参数三所用的单位)
                result3TextView, 5, 100,
                1, TypedValue.COMPLEX_UNIT_SP);

        btnToggleCamera = findViewById(R.id.btnToggleCamera);
        btnDetectObject = findViewById(R.id.btnDetectObject);

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(                                                     // 缩放成新的Bitmap
                        bitmap,
                        INPUT_SIZE,
                        INPUT_SIZE,
                        false
                );
                imageViewResult.setImageBitmap(bitmap);
                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

                String[] resultSplitStrings = results.toString().split(", ");
                for (int i = 0; i < resultSplitStrings.length; i++) {
                    if (resultSplitStrings.length == 3) {
                        if (i == 0) {
                            resultString = resultSplitStrings[i].substring(1);
                        } else if (i == resultSplitStrings.length - 1) {
                            result3String = resultSplitStrings[i].substring(0, resultSplitStrings[i].length() - 1);
                        } else {
                            result2String = resultSplitStrings[i];
                        }
                    } else if (resultSplitStrings.length == 2) {
                        if (i == 0) {
                            resultString = resultSplitStrings[i].substring(1);
                        } else if (i == resultSplitStrings.length - 1) {
                            result2String = resultSplitStrings[i].substring(0, resultSplitStrings[i].length() - 1);
                        }
                    } else if (resultSplitStrings.length == 1) {
                        if (i == 0) {
                            resultString = resultSplitStrings[i].substring(1, resultSplitStrings[i].length() - 1);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "判断数量超过3个，无法实现", Toast.LENGTH_SHORT).show();
                    }
                }

                switch (resultSplitStrings.length) {
                    case 1:
                        resultTextView.setText(resultString);
                        result2TextView.setText(" ");
                        result3TextView.setText(" ");
                        break;
                    case 2:
                        resultTextView.setText(resultString);
                        result2TextView.setText(result2String);
                        result3TextView.setText(" ");
                        break;
                    case 3:
                        resultTextView.setText(resultString);
                        result2TextView.setText(result2String);
                        result3TextView.setText(result3String);
                        break;
                }
                imageViewResultLinearLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {
            }
        });

        btnToggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.toggleFacing();
            }
        });
        btnDetectObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
            }
        });
        initTensorFlowAndLoadModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();                                                                         // 请求使用相机
    }

    @Override
    protected void onPause() {
        cameraView.stop();                                                                          // 关闭拍照功能
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {                                                           // 按照SingleThreadExecutor的特性, 会将任务依次执行
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {                                                           // 按照SingleThreadExecutor的特性, 会将任务依次执行
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),                                                            // 取得AssetManager, 读取assets目录下的文件
                            MODEL_PATH,
                            LABEL_PATH,                                                            // assets目录下的文件路径
                            INPUT_SIZE);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }
}
