package com.example.app;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.info_card.Info;
import com.example.app.info_card.InfoAdapter;
import com.example.app.info_card.OnLongClickListener;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // 高德API的用户key
    private String user_key = "47865bb865655e4d4741dd10cca9e5ee";
    private ImageView showImage;
    private TextView warningText;

    private List<Info> infoList;
    private InfoAdapter infoAdapter;

    // 数据库
    private SQLiteDatabase db;

    // 创建socket连接
    private Socket socket;

    private BufferedReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = openOrCreateDatabase("Info.db", MODE_PRIVATE, null);
        String CREATE_SQL = "create table if not exists Info("
                + "location text,"
                + "time text primary key not null,"
                + "distance text,"
                + "level text,"
                + "map BLOB"
                + ")";
        db.execSQL(CREATE_SQL);

        /*
         * 获取infoList数据
         * */
        infoList = new ArrayList<>();
        Cursor cursor = db.query("Info", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                // 查询位移地点
                @SuppressLint("Range") String location = cursor.getString(cursor.getColumnIndex("location"));
                // 查询位移时间
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("time"));
                // 查询位移距离
                @SuppressLint("Range") String distance = cursor.getString(cursor.getColumnIndex("distance"));
                // 查询位移等级
                @SuppressLint("Range") String level = cursor.getString(cursor.getColumnIndex("level"));

                // 查询位移处的图像
                @SuppressLint("Range") byte[] mapString = cursor.getBlob(cursor.getColumnIndex("map"));
                // 将字节数组转换为InputStream
                InputStream inputStream = new ByteArrayInputStream(mapString);
                // 将InputStream解码为Bitmap
                Bitmap map = BitmapFactory.decodeStream(inputStream);

                // 增加infoList数据
                infoList.add(new Info(location, time, distance, level, map));
            } while (cursor.moveToNext());
        }
        cursor.close();
        /*
         * 使用RecycleListVie
         * */
        infoAdapter = new InfoAdapter(infoList);
        infoAdapter.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public void onLongClick(int position, Info info) {
                deleteInfo(position, info);
            }
        });
        RecyclerView cardRecyclerView = findViewById(R.id.cardRecyclerView);
        cardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardRecyclerView.setAdapter(infoAdapter);

        showImage = findViewById(R.id.showImage);
        warningText = findViewById(R.id.warningText);

        socketWorking();
    }

    private void socketWorking() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int port = 443; // Port to connect to
                    socket = new Socket("47.113.150.60", port); // Connect to the server
                    System.out.println("Connected to server on port " + port);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    List<String> receivedData = new ArrayList();

                    // Set a timeout for the socket
                    socket.setSoTimeout(5000); // 5 seconds timeout

                    while (!Thread.currentThread().isInterrupted()) {
                        // Send a heartbeat message to the server
                        writer.println("HEARTBEAT");

                        // Check for incoming data
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("Received data: " + line);
                            receivedData.add(line);
                        }

                        if (receivedData.size() >= 4) {
                            updateUIByGaoDeAPI(receivedData.get(0), receivedData.get(1), receivedData.get(2), receivedData.get(3));
                        } else {
                            System.err.println("Received data does not have at least 4 elements");
                        }

                        // Clear the received data for the next iteration
                        receivedData.clear();
                    }
                } catch (IOException e) {
                    // Handle connection or communication errors
                } finally {
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    /*
     * 通过高德地图API获得地图
     * */
    @SuppressLint("StaticFieldLeak")
    private void updateUIByGaoDeAPI(String location, String time, String distance, String level) {

        String gaoDeUrl = "https://restapi.amap.com/v3/staticmap?key=" + user_key +
                "&location=" + location +
                "&zoom=" + "14" +
                "&size=" + "300*200" +
                "&markers=mid,0xFF0000,警:" + location;

        new AsyncTask<String, Void, Bitmap>() {
            private String location;
            private String time;
            private String distance;
            private String level;
            // 在后台线程中执行耗时操作
            @Override
            protected Bitmap doInBackground(String... strings) {
                try {
                    URL url = new URL(strings[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    location = strings[1];
                    time = strings[2];
                    distance = strings[3];
                    level = strings[4];
                    return BitmapFactory.decodeStream(input);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            // 在主线程中更新 UI
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    // Set the downloaded map image to the ImageView
                    showImage.setImageBitmap(bitmap);
                    addInfo(new Info(location, time, distance, level, bitmap));
                    String text = "大坝发生位移!\n经纬度:" + location + "\n请尽快排查!";
                    warningText.setText(text);
                }
            }
        }.execute(gaoDeUrl, location, time, distance, level);
    }


    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // 你可以选择其他的压缩格式，如 JPEG
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    /**
     * 数据库的存储操作
     */
    public void addInfo(Info info) {
        //获取信息
        String location = info.getLocation();
        String time = info.getTime();
        String distance = info.getDistance();
        String level = info.getLevel();
        Bitmap map = info.getMap();
        // 将Bitmap转为字节数组
        byte[] mapString = bitmapToByteArray(map);

        // 写入数据库
        ContentValues cvs = new ContentValues();
        cvs.put("location", location);
        cvs.put("time", time);
        cvs.put("distance", distance);
        cvs.put("level", level);
        cvs.put("map", mapString);
        db.insert("Info", null, cvs);

        infoList.add(info);
        infoAdapter.notifyItemInserted(infoList.size() - 1);
    }

    /**
     * 数据库的删除操作
     */
    private void deleteInfo(int position, Info info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认删除");
        builder.setMessage("确定要删除该记录吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String Delete_SQL = "delete from Info where location='" + info.getLocation() + "'";
                db.execSQL(Delete_SQL);
                infoList.remove(info);
                infoAdapter.notifyItemRemoved(position);
            }
        });
        builder.setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}

