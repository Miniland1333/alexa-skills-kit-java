package com.testr;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Henry on 3/14/2016.
 */
public final class AdafruitREST {

    private static final String XAIOKey = "446ba89583a03715b692b65f2062141c30e7a80f";
    private static final String root = "https://io.adafruit.com/api";
    private static final boolean debug = false;


    private AdafruitREST() {}


    public static void main(String[] args) throws IOException {
        String feedID = "welcome-feed";
        get(feedID);
        post(feedID,"5");
    }

    public static String get(String feedID) {
        String returnStatement="No Value";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(root + "/feeds/" + feedID);
        // The underlying HTTP connection is still held by the response object
        // to allow the response content to be streamed directly from the network socket.
        // In order to ensure correct deallocation of system resources
        // the user MUST call CloseableHttpResponse#close() from a finally clause.
        // Please note that if response content is not fully consumed the underlying
        // connection cannot be safely re-used and will be shut down and discarded
        // by the connection manager.
        httpGet.addHeader("x-aio-key", XAIOKey);
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            sysOut(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed


            if (response1.getStatusLine().getStatusCode() == 200) {

                String response = EntityUtils.toString(entity1);
                sysOut(response);
                if (entity1 != null) {
                    // parsing JSON
                    JSONObject result = new JSONObject(response); //Convert String to JSON Object
                    sysOut(result.get("last_value"));
                    returnStatement = (String)result.get("last_value");
                }
            }
            EntityUtils.consume(entity1);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnStatement;
    }

    public static String post(String feedID, String value) throws UnsupportedEncodingException {
        String returnStatement = "No Value";
        HttpPost httpPost = new HttpPost(root + "/feeds/" + feedID + "/data");
        httpPost.addHeader("x-aio-key", XAIOKey);
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("value", value));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        CloseableHttpClient httpclient = HttpClients.createDefault();

        try (CloseableHttpResponse response2 = httpclient.execute(httpPost)) {
            sysOut(response2.getStatusLine());
            HttpEntity entity2 = response2.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed

            if (response2.getStatusLine().getStatusCode() == 201) {
                String response = EntityUtils.toString(entity2);
                sysOut(response);

                if (entity2 != null) {
                    // parsing JSON
                    JSONObject result = new JSONObject(response); //Convert String to JSON Object
                    sysOut(result.get("value"));
                    returnStatement = (String)result.get("value");
                }

            }
            EntityUtils.consume(entity2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnStatement;
    }

    private static void sysOut(Object in){
        if (debug){
            String out = String.valueOf(in);
            System.out.println(out);
        }
    }
}
