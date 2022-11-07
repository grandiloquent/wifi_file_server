package euphoria.psycho.fileserver;

import android.util.Log;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import euphoria.psycho.fileserver.VideoDatabase.Video;

public class Douyin {
    public static Video fetch(String uri) throws Exception {
        uri = processUri(uri);
        String id = Shared.substring(getLocation(uri), "video/", "/");
        String api = "https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=" + id;
        HttpURLConnection c = (HttpURLConnection) new URL(api).openConnection();
        String body = Shared.readString(c);
        JSONObject jsonObject = new JSONObject(body);
        Video video = new Video();
        video.Title = jsonObject
                .getJSONArray("item_list").getJSONObject(0)
                .getJSONObject("share_info")
                .getString("share_title");
        video.Url = uri;
        video.Play = jsonObject
                .getJSONArray("item_list").getJSONObject(0)
                .getJSONObject("video")
                .getJSONObject("play_addr")
                .getJSONArray("url_list")
                .getString(0)
                .replace("/playwm/", "/play/");
        video.MusicPlay = jsonObject
                .getJSONArray("item_list").getJSONObject(0)
                .getJSONObject("music")
                .getJSONObject("play_url")
                .getJSONArray("url_list")
                .getString(0);
        video.MusicTitle = jsonObject
                .getJSONArray("item_list").getJSONObject(0)
                .getJSONObject("music")
                .getString("title");
        video.MusicAuthor = jsonObject
                .getJSONArray("item_list").getJSONObject(0)
                .getJSONObject("music")
                .getString("author");
        video.Cover = jsonObject
                .getJSONArray("item_list").getJSONObject(0)
                .getJSONObject("video")
                .getJSONObject("cover")
                .getJSONArray("url_list")
                .getString(0);
        video.VideoType = 0;
        video.CreateAt = System.currentTimeMillis();
        video.UpdateAt = System.currentTimeMillis();
        return video;
    }

    private static String getLocation(String uri) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(uri).openConnection();
        c.setInstanceFollowRedirects(false);
        return c.getHeaderField("Location");
    }

    private static String processUri(String uri) {
        // https://v.douyin.com/r1LyFDv/
        // Shared.substring(uri, "https://", " ")
        Pattern pattern = Pattern.compile("https://v.douyin.com/[a-zA-Z0-9]+/");
        Matcher matcher = pattern.matcher(uri);
        if (!matcher.find()) return null;
        return matcher.group();
    }

}
