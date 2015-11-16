package com.example.robert.together;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.example.robert.together.Person.Gender;

/**
 * Created by robert on 10/19/15.
 */
public class ImagePicker {
    public byte[] fetchUrlBytes(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//        urlConnection.connect();
//        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        try {
            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(urlConnection.getResponseMessage() + " with: " + urlString);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = urlConnection.getInputStream();
            byte[] buf = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = in.read(buf)) > 0) {
                out.write(buf, 0, bytesRead);
            }

            out.close();
            return out.toByteArray();
        } finally {
            urlConnection.disconnect();
        }
    }

    public String fetchUrlString(String urlString) throws IOException {
        return new String(fetchUrlBytes(urlString));
    }

    public Bitmap fetchImage(String urlString) throws IOException {
        byte[] bytes = fetchUrlBytes(urlString);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }

    public List<Person> fetchPerson(int id) throws IOException {
//        String resp = fetchUrlString("http://www.baidu.com" + id);


        return Person.sPersons;
    }

}
