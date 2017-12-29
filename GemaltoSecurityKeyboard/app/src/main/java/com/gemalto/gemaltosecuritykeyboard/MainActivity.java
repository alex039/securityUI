package com.gemalto.gemaltosecuritykeyboard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity implements View.OnTouchListener, View.OnFocusChangeListener, View.OnClickListener{
    private ImageView myKeyBoard;
    //private EditText editText;
    private RelativeLayout kb_layout;
    private LinearLayout passwor_layout;
    private TextView[] password;
    private Button btn_modify;
    private TextView resultView;
    private Button btn_comfirm;
    private TouchPoint[] touchPoint;
    private LocationReq request;
    private String filename;
    private ProgressDialog loading_Dialog;
    private int index = 0;
    public static final int  CMD_GET_UI = 1;
    public static final int  CMD_POST_POSITION = 2;
    //public static final String host = "http://192.168.199.174:8802";
    public static final String host = "http://182.61.48.120:8082";
    private int cmd;
    private Bitmap bitmap;
    private ImageView kb_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myKeyBoard = (ImageView) findViewById(R.id.iv_kb);
        kb_layout = (RelativeLayout) findViewById(R.id.keyboard_layout);
        btn_modify = (Button) findViewById(R.id.kb_cancle);
        btn_comfirm = (Button) findViewById(R.id.kb_comfirm);
        kb_view = (ImageView) findViewById(R.id.iv_kb);
        resultView = (TextView) findViewById(R.id.result_view);
        password = new TextView[4];
        touchPoint = new TouchPoint[4];
        touchPoint[0] = new TouchPoint();
        touchPoint[1] = new TouchPoint();
        touchPoint[2] = new TouchPoint();
        touchPoint[3] = new TouchPoint();
        passwor_layout = (LinearLayout) findViewById(R.id.password_pan);
        password[0] = (TextView) findViewById(R.id.tv_pass1);
        password[1] = (TextView) findViewById(R.id.tv_pass2);
        password[2] = (TextView) findViewById(R.id.tv_pass3);
        password[3] = (TextView) findViewById(R.id.tv_pass4);
        myKeyBoard.setOnTouchListener(this);
        btn_modify.setOnClickListener(this);
        btn_comfirm.setOnClickListener(this);

        for(int i = 0; i < 4; i++)
        {
            password[i].setOnTouchListener(this);
        }

        for(int i = 0; i < 4; i++)
        {
            password[i].setOnFocusChangeListener(this);
        }
    }

    public void showKeyboard() {
        int visibility = kb_layout.getVisibility();
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            kb_layout.setVisibility(View.VISIBLE);
        }
    }

    public void hideKeyboard() {
        int visibility = kb_layout.getVisibility();
        if (visibility == View.VISIBLE) {
            kb_layout.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (v.getId())
        {
            case R.id.iv_kb:
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    if(index < 4)
                    {
                        touchPoint[index].setX((int)event.getX());
                        touchPoint[index].setY((int)event.getY());
                        password[index].setText("*");
                        index++;
                    }
                }

                break;
            case R.id.tv_pass1:
            case R.id.tv_pass2:
            case R.id.tv_pass3:
            case R.id.tv_pass4:
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
//                    loading_Dialog = new ProgressDialog(MainActivity.this,R.style.dialog);
//                    loading_Dialog.show(this,"","下载安全UI");
                    resultView.setText("");
                    cmd = CMD_GET_UI;
                    doRequest(cmd);
                }
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(!hasFocus)
        {
            hideKeyboard();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.kb_cancle:
                for(int i = 0; i < 4; i++)
                {
                    password[i].setText("");
                    index = 0;
                }
                break;
            case R.id.kb_comfirm:
                if(index >= 4)
                {
                    for(int i = 0; i < 4; i++)
                    {
                        password[i].setText("");
                    }
                    cmd = CMD_POST_POSITION;
                    doRequest(cmd);
                    hideKeyboard();
                    index = 0;
                }
                break;
        }
    }


    private void doRequest(final  int cmd) {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                switch (cmd) {
                    case CMD_GET_UI:
                        return doHttpGet();
                    case CMD_POST_POSITION:
                        return doHttpPost();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String reuslt) {
                super.onPostExecute(reuslt);
                if(cmd == CMD_GET_UI)
                {
                    if(reuslt.equals("OK"))
                    {
                        kb_view.setImageBitmap(bitmap);
                        showKeyboard();
                    }
                    //loading_Dialog.cancel();
                }
                if(cmd == CMD_POST_POSITION)
                {
                    if(reuslt.equals("{\"status\":\"KO\"}"))
                    {
                        resultView.setText("密码错误");
                    }
                    else
                    {
                        resultView.setText("密码正确");
                    }
                }
            }
        };
        task.execute();
    }


    private String doHttpGet() {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(host+"/rest/download/3");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            if (HttpURLConnection.HTTP_OK == urlConnection.getResponseCode()) {
                InputStream inputStream = urlConnection.getInputStream();
                byte[] data = readInputStreamByte(inputStream);
                bitmap= BitmapFactory.decodeByteArray(data, 0, data.length);
                filename = urlConnection.getHeaderField("Content-Disposition");
                filename = filename.substring(filename.indexOf("\"")+1, filename.lastIndexOf("\""));

                //return readInputStream(inputStream);
                return "OK";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != urlConnection) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
    private void createRequest()
    {
        request = new LocationReq();
        Location[] location = new Location[4];


        for(int i = 0; i < 4; i++){
            location[i] = new Location();
            location[i].setX(touchPoint[i].getX());
            location[i].setY(touchPoint[i].getY());
        }

        request.setIndex(location);
        request.setFileName(filename);

    }
    private String doHttpPost() {
        HttpURLConnection urlConnection = null;
        try {

            createRequest();
            //JSONObject obj = new JSONObject(data);
            JSONObject obj = (JSONObject)JSON.toJSON(request);
            String  s = obj.toString();
            URL url = new URL(host+"/rest/valid/3");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(obj.toString().getBytes());
            outputStream.flush();
            if (HttpURLConnection.HTTP_OK == urlConnection.getResponseCode()) {
                InputStream inputStream = urlConnection.getInputStream();
                return readInputStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
        } finally {
            if (null != urlConnection) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    String readInputStream(InputStream in) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read = 0;
        while ((read = in.read(buffer)) > 0) {
            outputStream.write(buffer, 0, read);
        }

        return new String(outputStream.toByteArray());
    }
    byte[] readInputStreamByte(InputStream in) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read = 0;
        while ((read = in.read(buffer)) > 0) {
            outputStream.write(buffer, 0, read);
        }

        return outputStream.toByteArray();
    }
}
