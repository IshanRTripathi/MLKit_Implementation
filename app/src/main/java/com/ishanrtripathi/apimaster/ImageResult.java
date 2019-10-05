package com.ishanrtripathi.apimaster;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageResult extends AppCompatActivity {

    ImageView receivedImageView;

    public static int IMAGE_REQUEST_CODE=10001;

    FirebaseVisionImage firebaseVisionImage;

    Task<FirebaseVisionText> result;

    EditText resultTextView;

    StringBuilder res=new StringBuilder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_result);
        receivedImageView=findViewById(R.id.receivedImageView);
        resultTextView= findViewById(R.id.resultTextView);

        Bundle extras= getIntent().getExtras();
        assert extras != null;
        String path=extras.getString("imagePath");
        Toast.makeText(this, "Path :"+path, Toast.LENGTH_SHORT).show();
        receivedImageView.setImageURI(Uri.parse(path));

        if(parseImage(path))
        {
            Toast.makeText(this, "parsing Successfull", Toast.LENGTH_SHORT).show();
            recogniseText();
        }
        else
        {
            Toast.makeText(this, "parsing failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void recogniseText() {
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        if(firebaseVisionImage!=null) {
            result =
                    detector.processImage(firebaseVisionImage)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    Toast.makeText(ImageResult.this, "Success", Toast.LENGTH_SHORT).show();
                                    processText(firebaseVisionText);
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ImageResult.this, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
        }
        else
            Toast.makeText(this, "firebaseVisionImage is null", Toast.LENGTH_SHORT).show();
    }

    private void processText(FirebaseVisionText text) {
        String resultText = text.getText();
        for (FirebaseVisionText.TextBlock block: text.getTextBlocks()) {
            String blockText = block.getText();
            Float blockConfidence = block.getConfidence();
            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (FirebaseVisionText.Line line: block.getLines()) {
                String lineText = line.getText();
                Float lineConfidence = line.getConfidence();
                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (FirebaseVisionText.Element element: line.getElements()) {
                    String elementText = element.getText();
                    res.append(elementText).append('\n');
                    Log.i("Recognised Text :", elementText);
                    Float elementConfidence = element.getConfidence();
                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                }
                resultTextView.setText(res.toString());
            }
        }
    }

    private boolean parseImage(String path) {

        try {
            firebaseVisionImage= FirebaseVisionImage.fromFilePath(ImageResult.this,
                    Uri.fromFile(new File(String.valueOf(Uri.parse(path)))));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
