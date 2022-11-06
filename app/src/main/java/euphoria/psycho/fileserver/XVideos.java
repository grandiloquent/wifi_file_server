package euphoria.psycho.fileserver;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;

import euphoria.psycho.fileserver.VideoDatabase.Video;

public class XVideos {
    public static Video fetch(String uri) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(uri).openConnection(new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 10809)));
        c.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        c.addRequestProperty("Accept-Encoding", "gzip, deflate, br");
        c.addRequestProperty("Accept-Language", "en");
        c.addRequestProperty("Cache-Control", "no-cache");
        c.addRequestProperty("Connection", "keep-alive");
        c.addRequestProperty("Pragma", "no-cache");
        c.addRequestProperty("sec-ch-ua", "\"Google Chrome\";v=\"95\", \"Chromium\";v=\"95\", \";Not A Brand\";v=\"99\"");
        c.addRequestProperty("sec-ch-ua-mobile", "?0");
        c.addRequestProperty("sec-ch-ua-platform", "\"Windows\"");
        c.addRequestProperty("Sec-Fetch-Dest", "document");
        c.addRequestProperty("Sec-Fetch-Mode", "navigate");
        c.addRequestProperty("Sec-Fetch-Site", "none");
        c.addRequestProperty("Sec-Fetch-User", "?1");
        c.addRequestProperty("Upgrade-Insecure-Requests", "1");
        c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
        String contents = Shared.readString(c);
        Video video = new Video();
        video.Title = Shared.substring(contents, "setVideoTitle('", "');");
        video.Url = uri;
        video.Play = Shared.substring(contents, "setVideoUrlHigh('", "');");
        video.MusicPlay = "";
        video.MusicTitle = "";
        video.MusicAuthor = "";
        video.Cover = Shared.substring(contents, "setThumbUrl169('", "');");
        video.CreateAt = System.currentTimeMillis();
        video.UpdateAt = System.currentTimeMillis();
        return video;
    }
}
