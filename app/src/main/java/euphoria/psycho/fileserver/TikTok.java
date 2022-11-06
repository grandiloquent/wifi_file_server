package euphoria.psycho.fileserver;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;

public class TikTok {
    public static String fetchJson(String uri) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL("https://www.tikwm.com/api/?count=12&cursor=0&web=1&hd=1&url=" + uri)
                .openConnection(new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 10809)));
        c.addRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
        c.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        c.addRequestProperty("sec-ch-ua", "\"Chromium\";v=\"104\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"104\"");
        c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36");
        return Shared.readString(c);
    }
}
