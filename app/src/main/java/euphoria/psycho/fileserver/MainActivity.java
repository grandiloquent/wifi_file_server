package euphoria.psycho.fileserver;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE = 1;
    private TextView mTextView;
    private ImageView mImageView;

    private void initialize() {
        Shared.requestManageAllFilesPermission(this);
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
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            Log.e("B5aOx2", String.format("onActivityResult, %s", data));
        }
    }
}
