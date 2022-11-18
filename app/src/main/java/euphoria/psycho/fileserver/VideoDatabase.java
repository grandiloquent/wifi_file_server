package euphoria.psycho.fileserver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VideoDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public VideoDatabase(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    public void deleteVideo(int id) {
        getWritableDatabase().delete("video", "_id = ?", new String[]{Integer.toString(id)});
    }

    public long insertVideo(Video video) {
        ContentValues values = new ContentValues();
        if (video.Id > 0)
            values.put("_id", video.Id);
        values.put("title", video.Title);
        values.put("url", video.Url);
        values.put("play", video.Play);
        values.put("music_play", video.MusicPlay);
        values.put("music_title", video.MusicTitle);
        values.put("music_author", video.MusicAuthor);
        values.put("cover", video.Cover);
        values.put("video_type", video.VideoType);
        values.put("create_at", video.CreateAt);
        values.put("update_at", video.UpdateAt);
        return getWritableDatabase().insert("video", null, values);
    }

    public String queryAll(int t) throws JSONException {
//        getWritableDatabase().execSQL("CREATE UNIQUE INDEX \"url_ui\" ON \"video\" (\n" +
//                "\t\"url\"\n" +
//                ");");
        Cursor c = getReadableDatabase().rawQuery("select _id,title,url,play,music_play,music_title,music_author,cover,create_at,update_at from video where video_type = ? order by update_at desc", new String[]{Integer.toString(t)});
        JSONArray jsonArray = new JSONArray();
        //clearDuplicate();
        while (c.moveToNext()) {
            JSONObject object = new JSONObject();
            object.put("_id", c.getInt(0));
            object.put("title", c.getString(1));
            object.put("url", c.getString(2));
            object.put("play", c.getString(3));
            object.put("music_play", c.getString(4));
            object.put("music_title", c.getString(5));
            object.put("music_author", c.getString(6));
            object.put("cover", c.getString(7));
            object.put("create_at", c.getLong(8));
            object.put("update_at", c.getLong(9));
            jsonArray.put(object);
        }
        c.close();
        return jsonArray.toString();
    }

    public void clearDuplicate() {
        Cursor c = getReadableDatabase().rawQuery("select _id,url from video", null);
        List<Pair<Integer, String>> j = new ArrayList<>();
        while (c.moveToNext()) {
            j.add(Pair.create(c.getInt(0), c.getString(1)));
        }
        c.close();

        List<String> deleted = new ArrayList<>();

        for (Pair<Integer, String> v : j) {
            if (deleted.indexOf(v.second) != -1) {
                continue;
            }
            deleted.add(v.second);
            int id = v.first;
            Cursor cursor = getReadableDatabase().rawQuery("select _id from video where url = ?", new String[]{
                    v.second
            });
            while (cursor.moveToNext()) {
                if (cursor.getInt(0) != id) {
                    getWritableDatabase().delete("video", "_id=?", new String[]{Integer.toString(cursor.getInt(0))});
                }
            }
            cursor.close();
        }

    }

    public String queryVideo(int id) throws JSONException {
        Cursor c = getReadableDatabase().rawQuery("select title,url,play,music_play,music_title,music_author,cover,create_at,update_at from video where _id = ?", new String[]{
                Integer.toString(id)
        });
        JSONObject object = new JSONObject();
        if (c.moveToNext()) {
            object.put("title", c.getString(0));
            object.put("url", c.getString(1));
            object.put("play", c.getString(2));
            object.put("music_play", c.getString(3));
            object.put("music_title", c.getString(4));
            object.put("music_author", c.getString(5));
            object.put("cover", c.getString(6));
            object.put("create_at", c.getLong(7));
            object.put("update_at", c.getLong(8));
        }
        c.close();
        return object.toString();
    }

    public long updateVideo(Video video) {
        ContentValues values = new ContentValues();
        if (video.Title != null && video.Title.length() > 0) values.put("title", video.Title);
        if (video.Url != null && video.Url.length() > 0) values.put("url", video.Url);
        if (video.Play != null && video.Play.length() > 0) values.put("play", video.Play);
        if (video.MusicPlay != null && video.MusicPlay.length() > 0)
            values.put("music_play", video.MusicPlay);
        if (video.MusicTitle != null && video.MusicTitle.length() > 0)
            values.put("music_title", video.MusicTitle);
        if (video.MusicAuthor != null && video.MusicAuthor.length() > 0)
            values.put("music_author", video.MusicAuthor);
        if (video.Cover != null && video.Cover.length() > 0) values.put("cover", video.Cover);
        if (video.VideoType > 0) values.put("video_type", video.VideoType);
        if (video.CreateAt > 0) values.put("create_at", video.CreateAt);
        if (video.UpdateAt > 0) values.put("update_at", video.UpdateAt);
        return getWritableDatabase().update("video", values, "_id = ?", new String[]{
                Integer.toString(video.Id)
        });
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table video(        _id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,url TEXT,play TEXT,music_play TEXT,music_title TEXT,music_author TEXT,cover TEXT,video_type INTEGER,create_at INTEGER,update_at INTEGER    )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static class Video {
        public int Id;
        public String Title;
        public String Url;
        public String Play;
        public String MusicPlay;
        public String MusicTitle;
        public String MusicAuthor;
        public String Cover;
        public int VideoType;
        public long CreateAt;
        public long UpdateAt;

        @Override
        public String
        toString() {
            return "Video{" +
                    "Id=" + Id +
                    ", Title='" + Title + '\'' +
                    ", Url='" + Url + '\'' +
                    ", Play='" + Play + '\'' +
                    ", MusicPlay='" + MusicPlay + '\'' +
                    ", MusicTitle='" + MusicTitle + '\'' +
                    ", MusicAuthor='" + MusicAuthor + '\'' +
                    ", Cover='" + Cover + '\'' +
                    ", VideoType=" + VideoType +
                    ", CreateAt=" + CreateAt +
                    ", UpdateAt=" + UpdateAt +
                    '}';
        }
    }
}
