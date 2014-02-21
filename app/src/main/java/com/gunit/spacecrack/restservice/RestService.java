package com.gunit.spacecrack.restservice;

import android.util.Log;

import com.gunit.spacecrack.application.SpaceCrackApplication;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by Dimitri on 20/02/14.
 */
public class RestService {

    private static final String TAG = "REST Service";

    public static String getRequest(String url) {
        String result = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);

        CookieStore cookieStore = ((DefaultHttpClient) httpClient).getCookieStore();
        BasicClientCookie cookie = new BasicClientCookie("accessToken", "%22" + SpaceCrackApplication.accessToken + "%22");
        cookie.setDomain(SpaceCrackApplication.IP_ADDRESS);
        cookie.setPath("/");
        cookieStore.addCookie(cookie);
        ((DefaultHttpClient) httpClient).setCookieStore(cookieStore);

        httpGet.setHeader("Content-type", "application/json");
//        httpGet.setHeader("Cookie", "accessToken=%22" + SpaceCrackApplication.accessToken + "%22");

        try {
            // Execute HTTP Get Request
            HttpResponse response = httpClient.execute(httpGet);

            //Check the Status code of the response
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                response.getEntity().writeTo(outputStream);
                outputStream.close();
                result = outputStream.toString();
                Log.i(TAG, "Get request succeeded!");
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                Log.i(TAG, "Get request failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        httpClient.getConnectionManager().shutdown();
        return result;
    }

//    public static String login(JSONObject user) {
//        String accessToken = null;
//        HttpClient httpClient = new DefaultHttpClient();
//        HttpPost httpPost = new HttpPost(SpaceCrackApplication.URL_LOGIN);
//        HttpConnectionParams.setConnectionTimeout(new BasicHttpParams(), SpaceCrackApplication.NETWORK_TIMEOUT);
//
//        StringEntity stringEntity = null;
//        try {
//            stringEntity = new StringEntity(user.toString());
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        httpPost.setHeader("accept", "application/json");
//        httpPost.setHeader("Content-type", "application/json");
//        httpPost.setEntity(stringEntity);
//
//        try {
//            // Execute HTTP Post Request
//            HttpResponse response = httpClient.execute(httpPost);
//
//            //Check the Status code of the response
//            int statusCode = response.getStatusLine().getStatusCode();
//            if (statusCode == HttpStatus.SC_OK) {
//                String responseBody = EntityUtils.toString(response.getEntity());
//                try {
//                    //Get the access token
//                    JSONObject responseJson = new JSONObject(responseBody);
//                    accessToken = responseJson.getString("value");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                Log.i(TAG, "Logged in");
//            } else {
//                Log.i(TAG, "Login failed");
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        httpClient.getConnectionManager().shutdown();
//        return accessToken;
//    }

    public static String postRequest(String url, JSONObject user) {
        String accessToken = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(user.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        httpPost.setHeader("accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(stringEntity);

        try {
            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httpPost);

            //Check the Status code of the response
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                String responseBody = EntityUtils.toString(response.getEntity());
                try {
                    //Get the access token
                    JSONObject responseJson = new JSONObject(responseBody);
                    accessToken = responseJson.getString("value");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "Logged in");
            } else {
                Log.i(TAG, "Login failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        httpClient.getConnectionManager().shutdown();
        return accessToken;
    }

    public static boolean editProfile(JSONObject profile) {
        boolean result = false;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(SpaceCrackApplication.URL_PROFILE);
        HttpContext httpContext = new BasicHttpContext();

        CookieStore cookieStore = ((DefaultHttpClient) httpClient).getCookieStore();
        BasicClientCookie cookie = new BasicClientCookie("accessToken", "%22" + SpaceCrackApplication.accessToken + "%22");
        cookie.setDomain(SpaceCrackApplication.IP_ADDRESS);
        cookie.setPath("/");
        cookieStore.addCookie(cookie);
        ((DefaultHttpClient) httpClient).setCookieStore(cookieStore);

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(profile.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        httpPost.setHeader("accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(stringEntity);

        try {
            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httpPost, httpContext);

            //Check the Status code of the response
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                result = true;
                Log.i(TAG, "Profile edited");
            } else {
                Log.i(TAG, "Profile not edited");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        httpClient.getConnectionManager().shutdown();
        return result;
    }
}
