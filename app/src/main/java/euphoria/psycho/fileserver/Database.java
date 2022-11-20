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
        sqLiteDatabase.execSQL("CREATE TABLE \"note_tag\" (\n" +
                "\t\"_id\"\tINTEGER,\n" +
                "\t\"note_id\"\tINTEGER,\n" +
                "\t\"tag_id\"\tINTEGER,\n" +
                "\tPRIMARY KEY(\"_id\" AUTOINCREMENT)\n" +
                ")");
        sqLiteDatabase.execSQL("CREATE TABLE \"tag\" (\n" +
                "\t\"_id\"\tINTEGER,\n" +
                "\t\"name\"\tTEXT NOT NULL,\n" +
                "\tPRIMARY KEY(\"_id\" AUTOINCREMENT)\n" +
                ")");
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

    public long insertNote(JSONObject jsonObject) throws Exception {
        ContentValues values = new ContentValues();
        values.put("_id", jsonObject.getInt("_id"));
        values.put("title", jsonObject.getString("title"));
        values.put("content", jsonObject.getString("content"));
        values.put("create_at", jsonObject.getLong("create_at"));
        values.put("update_at", jsonObject.getLong("update_at"));
        return getWritableDatabase().insert("notes", null, values);
    }

    public long updateNote(int id, String title, String content, String tag) {

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("update_at", System.currentTimeMillis());
        Cursor cursor = getReadableDatabase().rawQuery("select _id,name from tag where name = ?", new String[]{tag});
        if (!cursor.moveToNext()) {
            ContentValues cv = new ContentValues();
            cv.put("name", tag);
            long tagId = getWritableDatabase().insert("tag", null, cv);
            ContentValues nt = new ContentValues();
            nt.put("note_id", id);
            nt.put("tag_id", tagId);
            getWritableDatabase().delete("note_tag", "note_id = ?", new String[]{
                    Integer.toString(id)
            });
            getWritableDatabase().insert("note_tag", null, nt);
        } else {
            long tagId = cursor.getLong(0);
            ContentValues nt = new ContentValues();
            nt.put("note_id", id);
            nt.put("tag_id", tagId);
            getWritableDatabase().delete("note_tag", "note_id = ?", new String[]{
                    Integer.toString(id)
            });
            getWritableDatabase().insert("note_tag", null, nt);
        }
        cursor.close();
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

    public String queryNotes(String tag) throws JSONException {
        Cursor c = tag.equals("null") ?
                getReadableDatabase().rawQuery("select notes._id,notes.title,notes.update_at from notes where _id not in (select note_id from note_tag)", null)
                : getReadableDatabase().rawQuery("select notes._id,notes.title,notes.update_at from notes\n" +
                "JOIN note_tag on note_tag.note_id=notes._id\n" +
                "JOIN tag on tag._id=note_tag.tag_id\n" +
                "where tag.name=? or tag.name is null", new String[]{tag});
        JSONArray jsonArray = new JSONArray();
        while (c.moveToNext()) {
            JSONObject object = new JSONObject();
            object.put("id", c.getInt(0));
            object.put("title", c.getString(1));
            object.put("update_at", c.getLong(2));
            jsonArray.put(object);
        }
        c.close();
        if (jsonArray.length() == 0) return null;
        return jsonArray.toString();
    }

    public String queryTags() {
        //getWritableDatabase().delete("tag","name = ?",new String[]{"|JavaScript"});

        Cursor cursor = getReadableDatabase().rawQuery("select name from tag", null);
        JSONArray jsonArray = new JSONArray();
        while (cursor.moveToNext()) {
            jsonArray.put(cursor.getString(0));
        }
        cursor.close();
        return jsonArray.toString();
    }

    public String queryAll() throws JSONException {
        Cursor c = getReadableDatabase().rawQuery("select * from notes", null);
        JSONArray jsonArray = new JSONArray();
        while (c.moveToNext()) {
            JSONObject object = new JSONObject();
            object.put("id", c.getInt(0));
            object.put("title", c.getString(1));
            object.put("content", c.getString(2));
            object.put("create_at", c.getLong(3));
            object.put("update_at", c.getLong(4));
            jsonArray.put(object);
        }
        c.close();
        if (jsonArray.length() == 0) return null;

        return jsonArray.toString();
    }

    public String queryNote(int id) throws JSONException {
        Cursor c = getReadableDatabase().rawQuery("select title,content,update_at,tag.name from notes left join note_tag on note_tag.note_id=notes._id left join tag on tag._id=note_tag.tag_id  where notes._id = ?", new String[]{
                Integer.toString(id)
        });
        JSONObject object = new JSONObject();
        if (c.moveToNext()) {
            object.put("title", c.getString(0));
            object.put("content", c.getString(1));
            object.put("update_at", c.getLong(2));
            object.put("tag", c.getString(3));
        }
        c.close();
        return object.toString();
    }
}
