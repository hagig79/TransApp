package com.example.test.transapp;


import android.os.AsyncTask;
import android.util.Xml;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class TransTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {

        // アクセストークンを取得する
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost("https://datamarket.accesscontrol.windows.net/v2/OAuth2-13");
        ArrayList<NameValuePair> httpParams = new ArrayList<NameValuePair>();
        httpParams.add(new BasicNameValuePair("client_id", "your_client_id"));
        httpParams.add(new BasicNameValuePair("client_secret", "your_client_secret"));
        httpParams.add(new BasicNameValuePair("scope", "http://api.microsofttranslator.com"));
        httpParams.add(new BasicNameValuePair("grant_type", "client_credentials"));
        String accessToken = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(httpParams, HTTP.UTF_8));
            HttpResponse response = httpClient.execute(post);
            String responseText = EntityUtils.toString(response.getEntity(), "UTF-8");
            JSONObject object = new JSONObject(responseText);
            accessToken = object.getString("access_token");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("access_token: " + accessToken);

        if (accessToken == null) {
            return null;
        }

        // 翻訳APIを実行する
        String text = params[0];
        String fromLang = "ja";
        String toLang = "en";

        String resultString = "";
        try {
            HttpGet get = new HttpGet("http://api.microsofttranslator.com/v2/Http.svc/Translate?text=" + URLEncoder.encode(text, "utf-8") + "&from=" + fromLang + "&to=" + toLang);
            get.addHeader("Authorization", "Bearer " + accessToken);
            HttpResponse response = httpClient.execute(get);
            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println(resultString);

            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(new StringReader(resultString));
            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.TEXT) {
                    resultString = xmlPullParser.getText();
                }
                eventType = xmlPullParser.next();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return resultString;
    }
}
