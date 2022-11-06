package euphoria.psycho.fileserver;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;

import euphoria.psycho.fileserver.VideoDatabase.Video;

public class TikTok {
    public static Video fetch(String uri) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL("https://www.tikwm.com/api/?count=12&cursor=0&web=1&hd=1&url=" + uri)
                .openConnection(new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 10809)));
        c.addRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
        c.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        c.addRequestProperty("sec-ch-ua", "\"Chromium\";v=\"104\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"104\"");
        c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36");
        JSONObject jsonObject = new JSONObject(Shared.readString(c));
        Video video = new Video();
        video.Title = jsonObject.getJSONObject("data").getString("title");
        video.Url = uri;
        video.Play = "https://www.tikwm.com/" + jsonObject.getJSONObject("data").getString("hdplay");
        video.MusicPlay = jsonObject.getJSONObject("data").getJSONObject("music_info").getString("play");
        video.MusicTitle = jsonObject.getJSONObject("data").getJSONObject("music_info").getString("title");
        video.MusicAuthor = jsonObject.getJSONObject("data").getJSONObject("music_info").getString("author");
        video.Cover = "https://www.tikwm.com/" + jsonObject.getJSONObject("data").getString("cover");
        video.CreateAt = System.currentTimeMillis();
        video.UpdateAt = System.currentTimeMillis();
        return video;
    }
}
