package com.siyuan.loginsystem.ui;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class with methods to perform http operations
 */
public class HttpUtilities {
    /**
     * @param username
     * @param password
     * @param urlString
     * @return
     * @throws IOException
     */
    public static JSONObject getHttpPostResult(final String username, String password, final String urlString) throws IOException, JSONException {
        String result = "";
        JSONObject obj = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
//        conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();
            password = md5(username) + md5(password);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", username);
            jsonObject.put("password", password);
            String jsonInputString = jsonObject.toString();
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                obj = new JSONObject(readStream(conn.getInputStream()));
            } else {
                obj = new JSONObject(readStream(conn.getErrorStream()));
            }
//            JSONObject jsonObject1 = new JSONObject(readStream(conn.getInputStream()));
//            result = obj.getString("status");
            return obj;
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static JSONObject getHttpGetResult(final String urlString) throws IOException, JSONException {
        JSONObject obj = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                obj = new JSONObject(readStream(conn.getInputStream()));
            } else {
                obj = new JSONObject(readStream(conn.getErrorStream()));
            }
//            JSONObject jsonObject1 = new JSONObject(readStream(conn.getInputStream()));
//            result = obj.getString("status");
            if (obj.getString("status").equals("OK")) System.out.println("用户名："+obj.getString("user_id"));
            return obj;
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        return obj;
    }

//    /**
//     * @param httpURLConnection
//     * @return
//     * @throws JSONException
//     */
//    public static String parseResponse(HttpURLConnection httpURLConnection) throws JSONException,
//            IOException {
//        JSONObject resultObject = new JSONObject(readStream(httpURLConnection.getInputStream()));
//        String result = resultObject.getString("status");
//        System.out.println("parseResponse" + result);
//        return result;
//    }

    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("readStream" + builder.toString());
        return builder.toString();
    }


    private static final String md5(String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
