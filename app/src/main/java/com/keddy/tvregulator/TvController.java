package com.keddy.tvregulator;

import android.util.Log;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * TVRegulator, com.keddy.tvregulator by גיא
 */

public class TvController {
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static String TAG = "TV_CONTROLLER_TAG";
    private String ipAddress = null;

    private Pattern ipAddressPattern;
    private Pattern volumePattern;
    private Matcher ipAddressMatcher;
    private Matcher volumeMatcher;


    public TvController(String ipAddress) {
        // Log.i(TAG, "Constructing TvController with" + ipAddress);
        if (ipAddress == null || !validateIpAddress(ipAddress)) {
            throw new IllegalArgumentException();
        } else {
            this.ipAddress = ipAddress;
        }
    }

    private boolean validateIpAddress(String ipAddress) {
        if (ipAddress == null) {
            return false;
        }


        ipAddressPattern = Pattern.compile(IPADDRESS_PATTERN);
        ipAddressMatcher = ipAddressPattern.matcher(ipAddress);
        return ipAddressMatcher.matches();
    }

    /**
     * @param volume value to set the volume on the tv
     * @return boolean, true or false based on success
     * @throws IllegalArgumentException if volume supplied is not in range
     */
    public boolean setVolume(int volume) throws IllegalArgumentException {
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("volume must be between 0 to 100");
        }

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("text/xml; charset=\"utf-8\"");
        RequestBody body = RequestBody.create(mediaType, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n   <s:Body>\r\n      <u:SetVolume xmlns:u=\"urn:schemas-upnp-org:service:RenderingControl:1\">\r\n         <InstanceID>0</InstanceID>\r\n         <Channel>Master</Channel>\r\n         <DesiredVolume>" + volume + "</DesiredVolume>\r\n      </u:SetVolume>\r\n   </s:Body>\r\n</s:Envelope>");
        Request request = new Request.Builder()
                .url("http://" + ipAddress + ":52235/upnp/control/RenderingControl1")
                .post(body)
                .addHeader("content-type", "text/xml; charset=\"utf-8\"")
                .addHeader("soapaction", "\"urn:schemas-upnp-org:service:RenderingControl:1#SetVolume\"")
                .addHeader("cache-control", "no-cache")
                .build();

        boolean retValue = false;
        try {
            Response response = client.newCall(request).execute();
            retValue = response.code() == 200;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        return retValue;
    }

    public int getVolume() {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("text/xml; charset=\"utf-8\"");
        RequestBody body = RequestBody.create(mediaType, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n<s:Body>\r\n<ns0:GetVolume xmlns:ns0=\"urn:schemas-upnp-org:service:RenderingControl:1\">\r\n<InstanceID>0</InstanceID>\r\n<Channel>Master</Channel>\r\n</ns0:GetVolume>\r\n</s:Body>\r\n</s:Envelope>");
        Request request = new Request.Builder()
                .url("http://" + ipAddress + ":52235/upnp/control/RenderingControl1")
                .post(body)
                .addHeader("soapaction", "\"SoapAction:urn:schemas-upnp-org:service:RenderingControl:1#GetVolume\"")
                .addHeader("content-type", "text/xml; charset=\"utf-8\"")
                .addHeader("cache-control", "no-cache")
                .build();


        int retValue = -1;
        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String responseString = responseBody.string();
                if (responseString != null) {
                    retValue = parseVolumeFromResponseString(responseString);
                }
            }
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, e.toString());
        }
        return retValue;
    }

    private int parseVolumeFromResponseString(String responseString) {
        if (responseString == null) {
            return -1;
        }

        volumePattern = Pattern.compile("<CurrentVolume>(\\d+)<\\/CurrentVolume>");
        volumeMatcher = volumePattern.matcher(responseString);
        if (volumeMatcher.find()) {
            return Integer.parseInt(volumeMatcher.group(1));
        } else {
            return -1;
        }
    }
}
