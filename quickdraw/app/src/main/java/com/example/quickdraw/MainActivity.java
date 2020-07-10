package com.example.quickdraw;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.io.IOException;
import java.util.Locale;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextToSpeech textToSpeech;
    private CustomView customView;
    private TextView resultTextView;
    private Classifier classifier;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_guess).setOnClickListener(this);
        findViewById(R.id.button_clear).setOnClickListener(this);

        customView = findViewById(R.id.customView);
        resultTextView = findViewById(R.id.result);

        try {
            classifier = new Classifier(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Text to Speech
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);

                    if(result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "Language not supported");
                    }
                    else{
                        Log.e("TTS", "Language English");
                    }
                }
                else{
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_guess:
                Bitmap scaledBitmap = customView.getBitmap(classifier.DIM_IMG_SIZE_X, classifier.DIM_IMG_SIZE_Y);
                int digit = classifier.classify(scaledBitmap);

                if(digit==0){
                    resultTextView.setText("Apple!");
                    textToSpeech.speak("I think, this is an Apple!", TextToSpeech.QUEUE_FLUSH, null);
                }
                if(digit==1){
                    resultTextView.setText("Banana!");
                    textToSpeech.speak("I think, this is a Banana!", TextToSpeech.QUEUE_FLUSH, null);
                }
                if(digit==2){
                    resultTextView.setText("Grape!");
                    textToSpeech.speak("I think, this is a Grape!", TextToSpeech.QUEUE_FLUSH, null);
                }
                if(digit==3){
                    resultTextView.setText("Pineapple!");
                    textToSpeech.speak("I think, this is a Pineapple!", TextToSpeech.QUEUE_FLUSH, null);
                }
                break;

            case R.id.button_clear:
                customView.clear();
                resultTextView.setText("");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if(textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}