package com.example.quickdraw;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Classifier {

    // Name of the model file (under assets folder)
    private static final String MODEL_PATH = "fruits.tflite";

    // TensorFlow Lite interpreter for running inference with the tflite model
    private final Interpreter interpreter;

    // A ByteBuffer to hold image data for input to model
    private final ByteBuffer inputImage;

    private final int[] imagePixels = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

    // Input size
    private static final int DIM_BATCH_SIZE = 1;    // batch size
    public static final int DIM_IMG_SIZE_X = 28;   // height
    public static final int DIM_IMG_SIZE_Y = 28;   // width
    private static final int DIM_PIXEL_SIZE = 1;    // 1 for gray scale & 3 for color images

    /* Output*/
    // Output size is 4 (number of fruit classes)
    private static final int CLASSES = 4;

    // Output array [batch_size, number of fruit classes]
    // 4 floats, each corresponds to the probability of each fruit class
    private float[][] outputArray = new float[DIM_BATCH_SIZE][CLASSES];

    public Classifier(Activity activity) throws IOException {
        interpreter = new Interpreter(loadModelFile(activity));
        inputImage =
                ByteBuffer.allocateDirect(4
                        * DIM_BATCH_SIZE
                        * DIM_IMG_SIZE_X
                        * DIM_IMG_SIZE_Y
                        * DIM_PIXEL_SIZE);
        inputImage.order(ByteOrder.nativeOrder());
    }

    // Memory-map the model file in Assets
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    public int classify(Bitmap bitmap) {
        preprocess(bitmap);
        runInference();
        return postprocess();
    }


    private void preprocess(Bitmap bitmap) {
        convertBitmapToByteBuffer(bitmap);
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (inputImage == null) {
            return;
        }
        inputImage.rewind();

        bitmap.getPixels(imagePixels, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = imagePixels[pixel++];
                inputImage.putFloat(convertToGreyScale(val));
            }
        }
    }

    private float convertToGreyScale(int color) {
        float r = ((color >> 16) & 0xFF);
        float g = ((color >> 8) & 0xFF);
        float b = ((color) & 0xFF);

        int grayscaleValue = (int) (0.299f * r + 0.587f * g + 0.114f * b);
        float preprocessedValue = grayscaleValue / 255.0f;
        return preprocessedValue;
    }

    private void runInference() {
        interpreter.run(inputImage, outputArray);
    }

    private int postprocess() {
        int maxIndex = -1;
        float maxProb = 0.0f;
        for (int i = 0; i < outputArray[0].length; i++) {
            if (outputArray[0][i] > maxProb) {
                maxProb = outputArray[0][i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

}
