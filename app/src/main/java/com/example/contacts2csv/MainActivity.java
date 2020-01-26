package com.example.contacts2csv;

import java.io.File;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends Activity implements OnClickListener {
    private int iSum;
    private EditText mEditText;
    private Button mHelpButton;
    private Button mInsertButton;
    private Button mOutputButton;
    private TextView mResultTextView;
    private TextView mOsTextView;
    private RadioButton[] mOsSetButtons = new RadioButton[2];
    private RadioButton[] mModeButtons = new RadioButton[2];

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ContactContant.INSERT_FAIL:
                    mResultTextView.setText(ContactContant.FAIL_INSERT);
                    endInsertContact();
                    break;
                case ContactContant.INSERT_SUCCESS:
                    mResultTextView.setText(String.format(
                            ContactContant.SUCCESS_INSERT,
                            ContactToolInsertUtils.getSuccessCount(),
                            ContactToolInsertUtils.getFailCount()));
                    endInsertContact();
                    break;
                case ContactContant.OUTPUT_FAIL:
                    mResultTextView.setText(ContactContant.FAIL_OUTPUT);
                    endOutputContact();
                    break;
                case ContactContant.OUTPUT_SUCCESS:
                    mResultTextView.setText((String.format(
                            ContactContant.SUCCESS_OUTPUT,
                            ContactToolOutputUtils.getCount())));
                    endOutputContact();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    /*init widgets*/
    private void init() {
        mEditText = (EditText) findViewById(R.id.edit_text);
        mHelpButton = (Button) findViewById(R.id.help_button);
        mHelpButton.setOnClickListener(this);
        mInsertButton = (Button) findViewById(R.id.insert_button);
        mInsertButton.setOnClickListener(this);
        mOutputButton = (Button) findViewById(R.id.output_button);
        mOutputButton.setOnClickListener(this);
        mResultTextView = (TextView) findViewById(R.id.result_view);
        mOsTextView = (TextView)findViewById(R.id.os_text);
        mOsSetButtons[0] = (RadioButton) findViewById(R.id.radio_button_win);
        // set gbk default
        mOsSetButtons[0].setChecked(true);
        mOsSetButtons[1] = (RadioButton) findViewById(R.id.radio_button_linux);
        mModeButtons[0] = (RadioButton) findViewById(R.id.radio_insert);
        mModeButtons[0].setOnClickListener(this);
        mModeButtons[1] = (RadioButton) findViewById(R.id.radio_output);
        mModeButtons[1].setOnClickListener(this);
        setInsertWidgetEnabled(false);
        setOutputWidgetEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.help_button:
                createDialog(this,ContactContant.HELP_DIALOG_TITLE,ContactContant.HELP_MESSAGE,false,
                        ContactContant.DIALOG_TYPE_HELP);
                break;
            case R.id.insert_button:
                insertContact();
                break;
            case R.id.output_button:
                outputContact();
                break;
            case R.id.radio_insert:
                setOutputWidgetEnabled(false);
                setInsertWidgetEnabled(true);
                break;
            case R.id.radio_output:
                setInsertWidgetEnabled(false);
                setOutputWidgetEnabled(true);
                break;
        }
    }

    public void createDialog(Context context, String title, String message,
                             boolean hasCancel, final int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(ContactContant.DIALOG_OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        switch (type) {
                            case ContactContant.DIALOG_TYPE_HELP:
                                dialog.cancel();
                                break;
                            case ContactContant.DIALOG_TYPE_INSERT:
                                doInsertContact();
                                break;
                            case ContactContant.DIALOG_TYPE_OUTPUT:
                                doOutputContact();
                                break;
                        }
                    }
                });
        if (hasCancel) {
            builder.setNeutralButton(ContactContant.DIALOG_CANCEL,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            dialog.cancel();
                        }
                    });
        }
        builder.show();
    }

    private void setInsertWidgetEnabled(boolean enable) {
        mOsSetButtons[0].setEnabled(enable);
        mOsSetButtons[1].setEnabled(enable);
        mInsertButton.setEnabled(enable);
        mEditText.setEnabled(enable);
        int visable = enable ? View.VISIBLE : View.INVISIBLE;
        mOsSetButtons[0].setVisibility(visable);
        mOsSetButtons[1].setVisibility(visable);
        mOsTextView.setVisibility(visable);
        if(!enable){
            mResultTextView.setText(ContactContant.NO_TEXT);
        }
    }

    private void insertContact() {
        String path = mEditText.getText().toString();
        if (path == null || path.equals(ContactContant.NO_TEXT)) {
            ContactToolUtils.showToast(this,
                    ContactContant.FAIL_EDITTEXT_NOT_INPUT);
            mResultTextView.setText(ContactContant.FAIL_EDITTEXT_NOT_INPUT);
            return;
        }
        path = ContactContant.FILE_NAME_PARENT + path;
        if (!new File(path).exists()) {
            ContactToolUtils
                    .showToast(this, ContactContant.FAIL_FIRE_NOT_EXIST);
            mResultTextView.setText(ContactContant.FAIL_FIRE_NOT_EXIST);
            return;
        }
        if (mInsertThread != null) {
            mInsertThread.interrupt();
            mInsertThread = null;
        }
        String charset = mOsSetButtons[0].isChecked() ? ContactContant.CHARSET_GBK
                : ContactContant.CHARSET_UTF8;
        mInsertThread = new Thread(new InsertRunnable(this, path, charset));
        createDialog(this, ContactContant.WARNDIALOG_TITLE,
                ContactContant.INSERT_WARNDIALOG_MESSAGE, true,
                ContactContant.DIALOG_TYPE_INSERT);
    }

    private void doInsertContact() {
        setInsertWidgetEnabled(false);
        mResultTextView.setText(ContactContant.STATUS_INSERTING);
        if (mInsertThread != null) {
            mInsertThread.start();
        }
    }

    private void endInsertContact() {
        mEditText.setText(ContactContant.NO_TEXT);
        setInsertWidgetEnabled(true);
    }

    private Thread mInsertThread;

    class InsertRunnable implements Runnable {
        private Context mContext;
        private String mPath;
        private String mCharset;

        public InsertRunnable(Context context, String path, String charset) {
            mPath = path;
            mContext = context;
            mCharset = charset;
        }

        @Override
        public void run() {
            boolean result = ContactToolInsertUtils.insertIntoContact(mContext,
                    mPath, mCharset);
            if (result) {
                mHandler.sendEmptyMessage(ContactContant.INSERT_SUCCESS);
            } else {
                mHandler.sendEmptyMessage(ContactContant.INSERT_FAIL);
            }
        }
    }

    private void setOutputWidgetEnabled(boolean enable) {
        mOutputButton.setEnabled(enable);
        if(!enable){
            mResultTextView.setText(ContactContant.NO_TEXT);
        }
    }

    private static final int NOT_NOTICE = 2;//如果勾选了不再询问
    private AlertDialog alertDialog;
    private AlertDialog mDialog;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {//选择了“始终允许”
                    //Toast.makeText(this, "" + "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])){//用户选择了禁止不再询问

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("permission")
                                .setMessage("点击允许才可以使用我们的app哦")
                                .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (mDialog != null && mDialog.isShowing()) {
                                            mDialog.dismiss();
                                        }
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);//注意就是"package",不用改成自己的包名
                                        intent.setData(uri);
                                        startActivityForResult(intent, NOT_NOTICE);
                                    }
                                });
                        mDialog = builder.create();
                        mDialog.setCanceledOnTouchOutside(false);
                        mDialog.show();
                    }else {//选择禁止
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("permission")
                                .setMessage("点击允许才可以使用我们的app哦")
                                .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (alertDialog != null && alertDialog.isShowing()) {
                                            alertDialog.dismiss();
                                        }
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                    }
                                });
                        alertDialog = builder.create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                    }
                }
            }
        }
    }


    private void outputContact(){
        //使用兼容库就无需判断系统版本
        int hasWriteStoragePermission = -11;
        int hasReadContacts = -11;
        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS}, 1);
        //if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED && hasReadContacts == PackageManager.PERMISSION_GRANTED) {
        //拥有权限，执行操作

        //权限不足，就进入申请权限死循环
        while (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED || hasReadContacts != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "权限不足。需要读写联系人权限、读写外部存储权限！", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS}, 1);
            hasWriteStoragePermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            hasReadContacts = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_CONTACTS);
        }

        //拥有权限，执行操作
        File file = new File(ContactContant.OUTPUT_PATH);
        if(file.exists()){
            createDialog(this, ContactContant.WARNDIALOG_TITLE,
                    ContactContant.OUTPUT_WARNDIALOG_MESSAGE, true,
                    ContactContant.DIALOG_TYPE_OUTPUT);
        }else {
            doOutputContact();
        }
        //}else{
        //	//没有权限，向用户请求权限
        //}
    }

    private void doOutputContact(){
        setOutputWidgetEnabled(false);
        mResultTextView.setText(ContactContant.STATUS_OUTPUTING);
        if (mOutputThread != null) {
            mOutputThread.interrupt();
            mOutputThread = null;
        }
        mOutputThread = new Thread(new OutputRunnable(this));
        if (mOutputThread != null) {
            mOutputThread.start();
        }
    }

    private Thread mOutputThread;

    class OutputRunnable implements Runnable {
        private Context mContext;

        public OutputRunnable(Context context) {
            mContext = context;
        }

        @Override
        public void run() {
            boolean result = ContactToolOutputUtils.outputContacts(mContext);
            if (result) {
                mHandler.sendEmptyMessage(ContactContant.OUTPUT_SUCCESS);
            } else {
                mHandler.sendEmptyMessage(ContactContant.OUTPUT_FAIL);
            }
        }
    }

    private void endOutputContact() {
        setOutputWidgetEnabled(true);
    }
}
