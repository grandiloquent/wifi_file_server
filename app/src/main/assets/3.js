(() => {
    var items = [
        ["_id", 0],
        ["title", 2],
        ["url", 2],
        ["play", 2],
        ["music_play", 2],
        ["music_title", 2],
        ["music_author", 2],
        ["cover", 2],
        ['create_at', 1],
        ['update_at', 1]
    ];
    var tableName = "video";
    const buffer = [];

    let temporary = [];
    for (let index = 0; index < items.length; index++) {
        const element = items[index];
        if (element[1] === 0) {
            temporary.push(`${element[0]} INTEGER PRIMARY KEY AUTOINCREMENT`);
        } else if (element[1] === 1) {
            temporary.push(`${element[0]} INTEGER`);
        } else if (element[1] === 2) {
            temporary.push(`${element[0]} TEXT`);
        }
    }
    const sql = `create table ${tableName}(
        ${temporary.join(',\n')}
    )`;
    buffer.push(`@Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("${sql.replaceAll(/[\r\n]/g, '')}");
    }`);
    buffer.push(`public String query${capitalize(camelCase(tableName))}(int id) throws JSONException {
        Cursor c = getReadableDatabase().rawQuery("select ${items.map(x => x[0]).slice(1).join(',')} from ${tableName} where ${items.map(x => x[0])[0]} = ?", new String[]{
                Integer.toString(id)
        });
        JSONObject object = new JSONObject();
        if (c.moveToNext()) {
            ${items.map(x => x[0]).slice(1).map((x, index) => {
        return `object.put("${x}", c.getString(${index}));`
    }).join('\n')}
        }
        c.close();
        return object.toString();
    }`);
    buffer.push(`public String queryAll() throws JSONException {
        Cursor c = getReadableDatabase().rawQuery("select ${items.map(x => x[0]).join(',')} from ${tableName}",null);
        JSONArray jsonArray = new JSONArray();
        while (c.moveToNext()) {
        JSONObject object = new JSONObject();
            ${items.map(x => x[0]).map((x, index) => {
        return `object.put("${x}", c.getString(${index}));`
    }).join('\n')}
    jsonArray.put(object);
        }
        c.close();
        return jsonArray.toString();
    }`);

    buffer.push(`public long insert${capitalize(camelCase(tableName))}(${capitalize(camelCase(tableName))} ${camelCase(tableName)}) {
        ContentValues values = new ContentValues();
        ${items.map(x => x[0]).slice(1).map((x, index) => {
        return `values.put("${x}", ${camelCase(tableName)}.${capitalize(camelCase(x))});`
    }).join('\n')}
        return getWritableDatabase().insert("${tableName}", null, values);
    }`);

    buffer.push(`public void delete${capitalize(camelCase(tableName))}(int id) {
        getWritableDatabase().delete("${tableName}", "${items[0][0]} = ?", new String[]{Integer.toString(id)});
    }`)

    buffer.push(`public class ${capitalize(camelCase(tableName))}{
        public int Id;
        ${items.slice(1).map((x, index) => {
        if (x[1] === 2)
            return `public String ${capitalize(camelCase(x[0]))};`
        if (x[1] === 1 || x[1] === 0)
            return `public int ${capitalize(camelCase(x[0]))};`
    }).join('\n')}
    }`);

    buffer.push(`
    ${capitalize(camelCase(tableName))} ${camelCase(tableName)}=new ${capitalize(camelCase(tableName))}();
    ${items.slice(1).map((x, index) => {
        if (x[1] === 2)
            return `${camelCase(tableName)}.${capitalize(camelCase(x[0]))}="";`
        if (x[1] === 1 || x[1] === 0)
            return `${camelCase(tableName)}.${capitalize(camelCase(x[0]))}=0;`
    }).join('\n')}`);
    console.log(buffer.join('\n'))

    function camelCase(string) {
        return string.replaceAll(/[_]([a-z])/g, m => m[1].toUpperCase());
    }
    function capitalize(string) {
        return string[0].toUpperCase() + string.slice(1);
    }
})();

