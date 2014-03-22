package com.gunit.spacecrack.restservice;

import android.util.Log;

import com.gunit.spacecrack.application.SpaceCrackApplication;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by Dimitri on 20/02/14.
 */

/**
 * Manages all the calls to the REST Service of the server
 */
public class RestService {

    private static final String TAG = "REST Service";

    public static String getRequest(String url) {
        String result = null;
        HttpClient httpClient = getHttpClient(true);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-type", "application/json");

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
                Log.i(TAG, "GET request succeeded!");
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                Log.i(TAG, "GET request failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        httpClient.getConnectionManager().shutdown();
        return result;
    }

    public static String postRequestWithoutAccessToken(String url, JSONObject user) {
        String accessToken = null;
        HttpClient httpClient = getHttpClient(false);
        HttpPost httpPost = getHttpPost(url, user.toString());

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
                Log.i(TAG, "POST Request succeeded");
            } else {
                Log.i(TAG, "POST Request failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        httpClient.getConnectionManager().shutdown();
        return accessToken;
    }

    public static Integer postRegisterUser(String url, JSONObject user) {
        HttpClient httpClient = getHttpClient(false);
        HttpPost httpPost = getHttpPost(url, user.toString());

        int statusCode = 0;
        try {
            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httpPost);

            //Check the Status code of the response
            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                Log.i(TAG, "POST Request succeeded");
            } else {
                Log.i(TAG, "POST Request failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        httpClient.getConnectionManager().shutdown();
        return statusCode;
    }

    public static String postGame(String url, JSONObject user) {
        String result = null;
        HttpClient httpClient = getHttpClient(true);
        HttpPost httpPost = getHttpPost(url, user.toString());

        try {
            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httpPost);

            //Check the Status code of the response
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity());
                Log.i(TAG, "POST Game Request succeeded");
            } else {
                Log.i(TAG, "POST Game Request failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        httpClient.getConnectionManager().shutdown();
        return result;
    }

    public static String postAction(String url, String action) {
        String result = null;
        HttpClient httpClient = getHttpClient(true);
        HttpPost httpPost = getHttpPost(url, action);

        try {
            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httpPost);

            //Check the Status code of the response
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                result = EntityUtils.toString(response.getEntity());
                Log.i(TAG, "POST Action Request succeeded");
            } else if (statusCode == 406) {
                result = String.valueOf(statusCode);
            } else {
                Log.i(TAG, "POST Action Request failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        httpClient.getConnectionManager().shutdown();
        return result;
    }

    public static boolean postRequest(String url, JSONObject jsonObject) {
        boolean result = false;
        HttpClient httpClient = getHttpClient(true);
        HttpPost httpPost = getHttpPost(url, jsonObject.toString());

        try {
            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httpPost);

            //Check the Status code of the response
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                result = true;
                Log.i(TAG, "POST request succeeded");
            } else {
                Log.i(TAG, "POST request failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        httpClient.getConnectionManager().shutdown();
        return result;
    }

    public static int acceptInvite(String url) {
        int result = 0;
        HttpClient httpClient = getHttpClient(true);
        HttpPost httpPost = getHttpPost(url, null);

        try {
            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httpPost);

            //Check the Status code of the response
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                result = statusCode;
                Log.i(TAG, "Accept Request succeeded");
            } else {
                Log.i(TAG, "Accept Request failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        httpClient.getConnectionManager().shutdown();
        return result;
    }

    public static int declineInvite(String url) {
        int result = 0;
        HttpDelete httpDelete = new HttpDelete(url);
        HttpClient httpClient = getHttpClient(true);

        httpDelete.setHeader("accept", "application/json");
        httpDelete.setHeader("Content-type", "application/json");

        try {
            // Execute HTTP Delete Request
            HttpResponse response = httpClient.execute(httpDelete);

            //Check the Status code of the response
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                result = statusCode;
                Log.i(TAG, "Decline Request succeeded");
            } else {
                Log.i(TAG, "Decline Request failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        httpClient.getConnectionManager().shutdown();
        return result;
    }

    public static int postVerificationToken(String url ,String verificationTokenJson) {
        HttpClient httpClient = getHttpClient(true);
        HttpPost httpPost = getHttpPost(url, verificationTokenJson);

        int statusCode = 0;
        try {
            HttpResponse response = httpClient.execute(httpPost);
            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                Log.i(TAG, "POST VerificationToken Request succeeded");
            } else {
                Log.i(TAG, "POST VerificationToken Request failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClient.getConnectionManager().shutdown();
        return statusCode;
    }

    private static DefaultHttpClient getHttpClient(boolean addCookie) {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, SpaceCrackApplication.NETWORK_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, SpaceCrackApplication.NETWORK_TIMEOUT);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

        if (addCookie) {
            CookieStore cookieStore = httpClient.getCookieStore();
            BasicClientCookie cookie = new BasicClientCookie("accessToken", "%22" + SpaceCrackApplication.accessToken + "%22");

            cookie.setDomain(SpaceCrackApplication.IP_ADDRESS);
            cookie.setPath("/");
            cookieStore.addCookie(cookie);
            httpClient.setCookieStore(cookieStore);
        }

        return httpClient;
    }

    private static HttpPost getHttpPost(String url, String entity) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        if (entity != null) {
            StringEntity stringEntity = null;
            try {
                stringEntity = new StringEntity(entity);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            httpPost.setEntity(stringEntity);
        }
        return httpPost;
    }



}
