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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends Activity implements OnClickListener {
    public static MainActivity m_MA;
    private EditText m_etInfo;
    private Button m_btnHelp;
    private Button m_btnInsert;
    private Button m_btnOutput;
    private TextView m_tvResult;
    private TextView m_tvOs;
    private RadioButton[] m_rbtnOs = new RadioButton[2];
    private RadioButton[] m_rbtnMode = new RadioButton[2];

    private Handler m_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ContactContant.INSERT_FAIL:
                    m_tvResult.setText(ContactContant.FAIL_INSERT);
                    endInsertContact();
                    break;
                case ContactContant.INSERT_SUCCESS:
                    m_tvResult.setText(String.format(
                            ContactContant.SUCCESS_INSERT,
                            ContactToolInsertUtils.getSuccessCount(),
                            ContactToolInsertUtils.getFailCount()));
                    endInsertContact();
                    break;
                case ContactContant.OUTPUT_FAIL:
                    m_tvResult.setText(ContactContant.FAIL_OUTPUT);
                    endOutputContact();
                    break;
                case ContactContant.OUTPUT_SUCCESS:
                    m_tvResult.setText((String.format(
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
        m_MA = this;
        init();
    }

    /*init widgets*/
    private void init() {
        m_etInfo = (EditText) findViewById(R.id.edit_text);
        m_btnHelp = (Button) findViewById(R.id.help_button);
        m_btnHelp.setOnClickListener(this);
        m_btnInsert = (Button) findViewById(R.id.insert_button);
        m_btnInsert.setOnClickListener(this);
        m_btnOutput = (Button) findViewById(R.id.output_button);
        m_btnOutput.setOnClickListener(this);
        m_tvResult = (TextView) findViewById(R.id.result_view);
        m_tvOs = (TextView)findViewById(R.id.os_text);
        m_rbtnOs[0] = (RadioButton) findViewById(R.id.radio_button_win);
        // set gbk default
        m_rbtnOs[0].setChecked(true);
        m_rbtnOs[1] = (RadioButton) findViewById(R.id.radio_button_linux);
        m_rbtnMode[0] = (RadioButton) findViewById(R.id.radio_insert);
        m_rbtnMode[0].setOnClickListener(this);
        m_rbtnMode[1] = (RadioButton) findViewById(R.id.radio_output);
        m_rbtnMode[1].setOnClickListener(this);

        //启动时选中导出联系人
        m_rbtnMode[1].setChecked(true);
        setInsertWidgetEnabled(false);
        setOutputWidgetEnabled(true);
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
                setInsertWidgetEnabled(true);
                setOutputWidgetEnabled(false);
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

    private void setInsertWidgetEnabled(boolean bEnable) {
        m_rbtnOs[0].setEnabled(bEnable);
        m_rbtnOs[1].setEnabled(bEnable);
        m_btnInsert.setEnabled(bEnable);
        m_etInfo.setEnabled(bEnable);
        int iVisable = bEnable ? View.VISIBLE : View.INVISIBLE;
        m_rbtnOs[0].setVisibility(iVisable);
        m_rbtnOs[1].setVisibility(iVisable);
        m_tvOs.setVisibility(iVisable);
        if(!bEnable){
            m_tvResult.setText(ContactContant.NO_TEXT);
        }
    }

    private void insertContact() {
        String sPath = m_etInfo.getText().toString();
        if (sPath == null || sPath.equals(ContactContant.NO_TEXT)) {
            ContactToolUtils.showToast(this, ContactContant.FAIL_EDITTEXT_NOT_INPUT);
            m_tvResult.setText(ContactContant.FAIL_EDITTEXT_NOT_INPUT);
            return;
        }
        sPath = ContactContant.FILE_NAME_PARENT + sPath;
        if (!new File(sPath).exists()) {
            ContactToolUtils.showToast(this, ContactContant.FAIL_FIRE_NOT_EXIST);
            m_tvResult.setText(ContactContant.FAIL_FIRE_NOT_EXIST);
            return;
        }
        if (m_threadInsert != null) {
            m_threadInsert.interrupt();
            m_threadInsert = null;
        }
        String sCharset = m_rbtnOs[0].isChecked() ? ContactContant.CHARSET_GBK : ContactContant.CHARSET_UTF8;
        m_threadInsert = new Thread(new InsertRunnable(this, sPath, sCharset));
        createDialog(this, ContactContant.WARNDIALOG_TITLE, ContactContant.INSERT_WARNDIALOG_MESSAGE,
                true, ContactContant.DIALOG_TYPE_INSERT);
    }

    private void doInsertContact() {
        setInsertWidgetEnabled(false);
        m_tvResult.setText(ContactContant.STATUS_INSERTING);
        if (m_threadInsert != null) {
            m_threadInsert.start();
        }
    }

    private void endInsertContact() {
        m_etInfo.setText(ContactContant.NO_TEXT);
        setInsertWidgetEnabled(true);
    }

    private Thread m_threadInsert;

    class InsertRunnable implements Runnable {
        private Context m_context;
        private String m_sPath;
        private String m_sCharset;

        public InsertRunnable(Context context, String sPath, String sCharset) {
            m_sPath = sPath;
            m_context = context;
            m_sCharset = sCharset;
        }

        @Override
        public void run() {
            boolean bResult = ContactToolInsertUtils.insertIntoContact(m_context, m_sPath, m_sCharset);
            if (bResult) {
                m_handler.sendEmptyMessage(ContactContant.INSERT_SUCCESS);
            } else {
                m_handler.sendEmptyMessage(ContactContant.INSERT_FAIL);
            }
        }
    }

    private void setOutputWidgetEnabled(boolean bEnable) {
        m_btnOutput.setEnabled(bEnable);
        if(!bEnable){
            m_tvResult.setText(ContactContant.NO_TEXT);
        }
    }

    private static final int NOT_NOTICE = 2;//如果勾选了不再询问
    private AlertDialog m_dla;
    private AlertDialog m_dlgAlert;
    @Override
    public void onRequestPermissionsResult(int iRequestCode, String[] sPermissions, int[] iGrantResults) {
        super.onRequestPermissionsResult(iRequestCode, sPermissions, iGrantResults);

        if (iRequestCode == 1) {
            for (int i = 0; i < sPermissions.length; i++) {
                if (iGrantResults[i] == PERMISSION_GRANTED) {//选择了“始终允许”
                    //Toast.makeText(this, "" + "权限" + sPermissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, sPermissions[i])){//用户选择了禁止不再询问

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("permission")
                                .setMessage("点击允许才可以使用我们的app哦")
                                .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (m_dla != null && m_dla.isShowing()) {
                                            m_dla.dismiss();
                                        }
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);//注意就是"package",不用改成自己的包名
                                        intent.setData(uri);
                                        startActivityForResult(intent, NOT_NOTICE);
                                    }
                                });
                        m_dla = builder.create();
                        m_dla.setCanceledOnTouchOutside(false);
                        m_dla.show();
                    }else {//选择禁止
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("permission")
                                .setMessage("点击允许才可以使用我们的app哦")
                                .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (m_dlgAlert != null && m_dlgAlert.isShowing()) {
                                            m_dlgAlert.dismiss();
                                        }
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                    }
                                });
                        m_dlgAlert = builder.create();
                        m_dlgAlert.setCanceledOnTouchOutside(false);
                        m_dlgAlert.show();
                    }
                }
            }
        }
    }


    private void outputContact(){
        //使用兼容库就无需判断系统版本
        int iHasWriteStoragePermission = -11;
        int iHasReadContacts = -11;
        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS}, 1);
        //if (iHasWriteStoragePermission == PackageManager.PERMISSION_GRANTED && iHasReadContacts == PackageManager.PERMISSION_GRANTED) {
        //拥有权限，执行操作

        //权限不足，就进入申请权限死循环
        while (iHasWriteStoragePermission != PackageManager.PERMISSION_GRANTED || iHasReadContacts != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "权限不足。需要读写联系人权限、读写外部存储权限！", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS}, 1);
            iHasWriteStoragePermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            iHasReadContacts = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_CONTACTS);
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
        m_tvResult.setText(ContactContant.STATUS_OUTPUTING);
        if (m_threadOutput != null) {
            m_threadOutput.interrupt();
            m_threadOutput = null;
        }
        m_threadOutput = new Thread(new OutputRunnable(this));
        if (m_threadOutput != null) {
            m_threadOutput.start();
        }
    }

    private Thread m_threadOutput;

    class OutputRunnable implements Runnable {
        private Context m_context;

        public OutputRunnable(Context context) {
            m_context = context;
        }

        @Override
        public void run() {
            boolean result = ContactToolOutputUtils.outputContacts(m_context);
            if (result) {
                m_handler.sendEmptyMessage(ContactContant.OUTPUT_SUCCESS);
            } else {
                m_handler.sendEmptyMessage(ContactContant.OUTPUT_FAIL);
            }
        }
    }

    private void endOutputContact() {
        setOutputWidgetEnabled(true);
    }
}
