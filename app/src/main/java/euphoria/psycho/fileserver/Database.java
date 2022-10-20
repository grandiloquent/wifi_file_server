package euphoria.psycho.fileserver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Database extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public Database(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    public Cursor query(String tableName, String[] projections) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(tableName);
        return builder.query(db, projections, null, null, null, null, null);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE notes(_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,content TEXT,create_at INTEGER NOT NULL,update_at  INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(sqLiteDatabase);
    }

    public long insertNote(String title, String content) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("create_at", System.currentTimeMillis());
        values.put("update_at", System.currentTimeMillis());
        return getWritableDatabase().insert("notes", null, values);
    }

    public long updateNote(int id, String title, String content) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("update_at", System.currentTimeMillis());
        return getWritableDatabase().update("notes", values, "_id = ?", new String[]{
                Integer.toString(id)
        });
    }

//    public List<Pair<String, Long>> queryNotes() {
//        Cursor c = getReadableDatabase().rawQuery("select title,update_at from ntoes", null);
//        List<Pair<String, Long>> notes = new ArrayList<>();
//        while (c.moveToNext()) {
//            notes.add(Pair.create(c.getString(0), c.getLong(1)));
//        }
//        c.close();
//        return notes;
//    }

    public String queryNotes() throws JSONException {
        Cursor c = getReadableDatabase().rawQuery("select title,update_at from ntoes", null);
        JSONArray jsonArray = new JSONArray();
        while (c.moveToNext()) {
            JSONObject object = new JSONObject();
            object.put("title", c.getString(0));
            object.put("update_at", c.getLong(1));
            jsonArray.put(object);
        }
        c.close();
        return jsonArray.toString();
    }

    public String queryNote(int id) throws JSONException {
        Cursor c = getReadableDatabase().rawQuery("select title,content,update_at from ntoes where _id = ?", new String[]{
                Integer.toString(id)
        });
        JSONObject object = new JSONObject();
        if (c.moveToNext()) {
            object.put("title", c.getString(0));
            object.put("content", c.getString(1));
            object.put("update_at", c.getLong(2));
        }
        c.close();
        return object.toString();
    }
}
