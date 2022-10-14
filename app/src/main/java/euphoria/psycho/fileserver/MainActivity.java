package euphoria.psycho.fileserver;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_CODE_DOCUMENT = 2;
    public static final String TREE_URI = "tree_uri";
    private TextView mTextView;
    private ImageView mImageView;

    private void initialize() {
        Shared.requestManageAllFilesPermission(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String treeUri = sharedPreferences.getString(TREE_URI, null);
        if (treeUri == null) {
            Shared.requestDocumentPermission(this, "data", REQUEST_CODE_DOCUMENT);
        }
        FileServer fileServer = new FileServer(MainActivity.this);
        try {
            fileServer.start();
        } catch (IOException e) {
            Log.e("B5aOx2", String.format("initialize, %s", e.getMessage()));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String> needPermissions = Arrays.stream(new String[]{
                permission.INTERNET,
                permission.ACCESS_WIFI_STATE,
                permission.READ_EXTERNAL_STORAGE,
        }).filter(permission -> checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                .collect(Collectors.toList());
        if (VERSION.SDK_INT <= 28 && checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            needPermissions.add(permission.WRITE_EXTERNAL_STORAGE);
        } else if (VERSION.SDK_INT >= VERSION_CODES.P && (checkSelfPermission(permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED)) {
            needPermissions.add(permission.FOREGROUND_SERVICE);
        }
        if (needPermissions.size() > 0) {
            requestPermissions(needPermissions.toArray(new String[0]), REQUEST_CODE);
            return;
        }
        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, REQUEST_CODE, 0, "帮助");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Arrays.stream(grantResults)
                .filter(g -> g != PackageManager.PERMISSION_GRANTED).count() > 0) {
            Toast.makeText(this, "程序缺少必要程序无法运行", Toast.LENGTH_LONG).show();
            finish();
        } else {
            initialize();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_DOCUMENT) {
            getContentResolver().takePersistableUriPermission(
                    data.getData(),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPreferences.edit().putString(TREE_URI, data.getData().toString()).apply();
        }
    }
}
