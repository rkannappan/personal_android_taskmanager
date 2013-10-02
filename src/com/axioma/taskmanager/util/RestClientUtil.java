package com.axioma.taskmanager.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.util.Base64;

/**
 * @author rkannappan
 */
public class RestClientUtil {

   public static String getJSONFromUrl(final String url, Context context) {
      return new RestClientUtil().getJSONFromUrl(url, PreferenceUtil.getAppUserName(context),
               PreferenceUtil.getAppPassword(context));
   }

   private String getJSONFromUrl(String url, String userName, String password) {
      String encodedURL = url.replaceAll(" ", "%20");

      HttpClient httpClient = new DefaultHttpClient();
      HttpContext localContext = new BasicHttpContext();
      HttpGet httpGet = new HttpGet(encodedURL);

      String userDetails = userName + ":" + new DigestUtil().scrambleString(password);

      httpGet.setHeader("Authorization", "Basic " + new String(Base64.encode(userDetails.getBytes(), Base64.NO_WRAP)));
      String text = null;
      try {

         HttpResponse response = httpClient.execute(httpGet, localContext);

         HttpEntity entity = response.getEntity();

         text = getASCIIContentFromEntity(entity);

      } catch (Exception e) {
         e.printStackTrace();
         return e.getLocalizedMessage();

      }

      return text;
   }

   private String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
      InputStream in = entity.getContent();

      StringBuffer out = new StringBuffer();
      int n = 1;
      while (n > 0) {
         byte[] b = new byte[4096];

         n = in.read(b);

         if (n > 0)
            out.append(new String(b, 0, n));
      }

      return out.toString();
   }
}