package com.tesseract.aidllib;


import com.tesseract.aidllib.OrientationCallback;


interface OrientationInterface {

    void registerListener(in OrientationCallback callback);

    void unregisterListener(in OrientationCallback callback);
}