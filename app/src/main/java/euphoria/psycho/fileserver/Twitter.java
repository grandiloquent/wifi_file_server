package euphoria.psycho.fileserver;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import euphoria.psycho.fileserver.VideoDatabase.Video;

public class Twitter {
    public static Video fetch(String uri) throws Exception {
        URL url = new URL("https://twittervideodownloaderpro.com/twittervideodownloadv2/index.php");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 10809)));
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.56 Mobile Safari/537.36");
        HashMap<String, String> postDataParams = new HashMap<>();
        postDataParams.put("id", Shared.substringAfterLast(uri, "/"));
        OutputStream os = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
        writer.write(getPostDataString(postDataParams));
        writer.flush();
        writer.close();
        os.close();
        int statusCode = connection.getResponseCode();
        if (statusCode != 200) {
            return null;
        }
        String s = Shared.readString(connection);
        JSONObject jsonObject = new JSONObject(s);
        JSONArray videos = jsonObject.getJSONArray("videos");
        int maxIndex = 0;
        int maxSize = 0;
        for (int i = 0; i < videos.length(); i++) {
            int size = videos.getJSONObject(i).getInt("size");
            if (maxSize < size) {
                maxIndex = i;
                maxSize = size;
            }
        }
        Video video = new Video();
        video.Title = videos.getJSONObject(maxIndex).getString("text");
        video.Url = uri;
        video.Play = videos.getJSONObject(maxIndex).getString("url");
        video.Cover = videos.getJSONObject(maxIndex).getString("thumb");
        video.CreateAt = System.currentTimeMillis();
        video.UpdateAt = System.currentTimeMillis();
        return video;
    }

    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) first = false;
            else result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

}
