package com.example.outingtools;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String DIR_PATH = Environment.getExternalStorageDirectory().getPath() + "/outingTools/pic/";
    private static final String PIC_TYPE = ".jpg";

    Button button;
    EditText edittext;
    ImageView imageview;
    TextView textview;
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        //imageview = findViewById(R.id.imageView2);
        //textview = findViewById(R.id.textView);

        button.setOnClickListener(v -> {
            edittext = findViewById(R.id.editTextTextMultiLine);
            Uri imageUri = Uri.fromFile(new File(generateFilePath()));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, 1);
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
            //startActivityForResult(intent, 2);
        });

        requestMyPermissions();
    }

    private void requestMyPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //没有授权，编写申请权限代码
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            Log.i(TAG, "requestMyPermissions# 有写SD权限");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //没有授权，编写申请权限代码
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            Log.i(TAG, "requestMyPermissions# 有读SD权限");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            //获取照片数据
            String filePath = generateFilePath();
            File imgFile = new File(filePath);
            if (imgFile.exists()) {
                imgFile.deleteOnExit();
            }
            if (!imgFile.getParentFile().exists()) {
                Log.i(TAG, "saveBitmap# make dir: " + imgFile.getParentFile().mkdirs());
            }
            try {
                Log.i(TAG, "saveBitmap# create new file: " + imgFile.createNewFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
            //if (!imgFile.exists()) {
                //imgFile.mkdirs();
            //}

            //textview.setText(saveBitmap(cameraPhotoBitmap));
            notifySystemToScan(this, generateFilePath());
        }

    }

    /**
     * 保存bitmap到文件中
     *
     * @param bitmap 输入的bitmap
     * @return 返回保存文件路径
     */
    private String saveBitmap(Bitmap bitmap) {
        String filePath = generateFilePath();
        try {
            File imgFile = new File(filePath);
            if (imgFile.exists()) {
                imgFile.deleteOnExit();
            }
            if (!imgFile.getParentFile().exists()) {
                Log.i(TAG, "saveBitmap# make dir: " + imgFile.getParentFile().mkdirs());
            }
            Log.i(TAG, "saveBitmap# create new file: " + imgFile.createNewFile());

            FileOutputStream fos = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, this.getString(R.string.save_file_ok), Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, R.string.save_file_fail, Toast.LENGTH_SHORT).show();
            filePath = "";
            e.printStackTrace();
            Log.e(TAG, "saveBitmap# save img error msg: " + e.getLocalizedMessage());
        } catch (IOException e) {
            Toast.makeText(this, R.string.save_file_fail, Toast.LENGTH_SHORT).show();
            filePath = "";
            e.printStackTrace();
            Log.e(TAG, "saveBitmap# save img error msg: " + e.getLocalizedMessage());
        }
        notifySystemToScan(this, filePath);
        return filePath;
    }

    /**
     * @return 返回保存文件路径
     */
    private String generateFilePath() {
        String dirPath = DIR_PATH;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dirPath = this.getFilesDir().getAbsolutePath() + "/pic/";
        }

        String fileName = edittext.getText().toString();
        if (fileName.isEmpty()) {
            fileName = generateDateStr();
        }

        return dirPath + fileName + PIC_TYPE;
    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    private String generateDateStr() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.CHINA);
        return dateFormat.format(new Date());
    }
    public static void notifySystemToScan(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(filePath);

        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }
}
