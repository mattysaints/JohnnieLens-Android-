package com.example.johnnielens;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 123;

    private CameraKitView cameraView;
    private ImageButton button;
    private ImageButton openGallery;
    private Bundle bundle = new Bundle();
    private List<String> result = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = findViewById(R.id.camera);
        button = findViewById(R.id.button);
        openGallery = findViewById(R.id.openGallery);
        progressBar = findViewById(R.id.progress_circular);
        //cameraView.getPermissions();
        cameraView.setPermissions(CameraKitView.PERMISSION_STORAGE);

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                cameraView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(CameraKitView cameraKitView, byte[] picture) {
                        Bitmap image = BitmapFactory.decodeByteArray(picture, 0,picture.length);
                        //getLabels(image);
                        getLabels(resizeBitmap(image,300));
                    }
                });
            }
        });

        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Scegli un'immagine"),GALLERY_REQUEST_CODE);
            }
        });

        cameraView.setPreviewListener(new CameraKitView.PreviewListener() {
            @Override
            public void onStart() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onStop() {
            }
        });

    }

    public Bitmap resizeBitmap(Bitmap getBitmap, int maxSize) {
        int width = getBitmap.getWidth();
        int height = getBitmap.getHeight();
        double x;

        if (width >= height && width > maxSize) {
            x = width / height;
            width = maxSize;
            height = (int) (maxSize / x);
        } else if (height >= width && height > maxSize) {
            x = height / width;
            height = maxSize;
            width = (int) (maxSize / x);
        }
        return Bitmap.createScaledBitmap(getBitmap, width, height, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                Bitmap image = BitmapFactory.decodeStream(inputStream);
                progressBar.setVisibility(View.VISIBLE);
                getLabels(image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void getLabels(Bitmap bitmapImage) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmapImage); //ML Kit può elaborare le immagini solo quando sono nel formato FirebaseVisionImage, coversione
        FirebaseVisionOnDeviceImageLabelerOptions options = new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f).build();

        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                .getOnDeviceImageLabeler(options);

        FirebaseVisionCloudImageLabelerOptions options_cloud = new FirebaseVisionCloudImageLabelerOptions.Builder().setConfidenceThreshold(0.9f).build();

        FirebaseVisionImageLabeler label_cloud = FirebaseVision.getInstance().getCloudImageLabeler(options_cloud);

        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                try {
                    translateLabel(firebaseVisionImageLabels);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        /*label_cloud.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                try {
                    for (FirebaseVisionImageLabel label: labels) {
                        System.out.println(label.getText()+""+label.getConfidence());
                    }
                    translateLabel(labels);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });*/
    }


    private void translateLabel(List<FirebaseVisionImageLabel> labels) throws InterruptedException {
        FirebaseTranslatorOptions opzioni =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.EN)
                        .setTargetLanguage(FirebaseTranslateLanguage.IT)
                        .build();
        final FirebaseTranslator englishItalianTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(opzioni);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        englishItalianTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                // (Set a flag, unhide the translation UI, etc.)
                                Log.d("Modello","Modello Traduzione Scaricato");
                            }
                        });

        for(int i=0; i< labels.size(); i++){
            if(labels.get(i).getText().equals(labels.get(labels.size()-1).getText())){
                englishItalianTranslator.translate(labels.get(i).getText()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        result.add(s);
                        bundle.putString("Keys",result.toString());
                        Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
            } else {
                englishItalianTranslator.translate(labels.get(i).getText()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        result.add(s);
                    }
                });
            }
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        cameraView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        cameraView.onPause();
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onStop() {
        cameraView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraView.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }



}
