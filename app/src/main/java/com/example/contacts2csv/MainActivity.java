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
import android.os.Environment;
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
    private RadioButton[] m_rbtnArrOs = new RadioButton[2];
    private RadioButton[] m_rbtnArrMode = new RadioButton[2];
    public static String m_sPathDownloads;    //存储数据的默认路径
    public String m_sFilePath;              //文件路径
    public static CommonFun m_Fun;    //通用函数类

    public ContactOutput m_output;      //导出联系人
    public ContactInsert m_insert;      //导入联系人

    //线程消息处理对象
    private Handler m_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ContactStrings.INSERT_FAIL:
                    m_tvResult.setText(ContactStrings.FAIL_INSERT);
                    endInsertContact();
                    break;
                case ContactStrings.INSERT_SUCCESS:
                    m_tvResult.setText(String.format(ContactStrings.SUCCESS_INSERT, ContactInsert.getSuccessCount(), ContactInsert.getFailCount()));
                    endInsertContact();
                    break;
                case ContactStrings.OUTPUT_FAIL:
                    m_tvResult.setText(ContactStrings.FAIL_OUTPUT);
                    endOutputContact();
                    break;
                case ContactStrings.OUTPUT_SUCCESS:
                    m_tvResult.setText((String.format(ContactStrings.SUCCESS_OUTPUT + "到：\n" + m_sFilePath, m_output.getSum())));
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

    private String getUserPath(){
        String sPath = "";

        //若Android中存在外部存储，便用Android的外部存储目录，否则便使用Android内部存储目录
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            sPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            //sPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            //System.out.println("getExternalFilesDir = " + m_sPathDownloads);
            // /storage/emulated/0/Android/data/com.example.OctopusMessage/files/Download
            //物理路径：/sdcard/Android/data/com.example.OctopusMessage/files/Download/OctopusMessage/OctopusMessage_Config.xml
        } else {
            sPath = getApplicationContext().getFilesDir().getAbsolutePath();
            //System.out.println("getApplicationContext = " + getApplicationContext().getFilesDir().getAbsolutePath());
            // /data/user/0/com.example.OctopusMessage/files
            //物理路径：/data/data/com.example.OctopusMessage/shared_prefs/OctopusMessage_Config.xml
        }

        //System.out.println("mPathDownloads = " + m_sPathDownloads);
        //I/System.out: mPathDownloads = /storage/emulated/0/Download

        return sPath;
    }

    /*init widgets*/
    private void init() {
        m_sPathDownloads = getUserPath();
        m_Fun = new CommonFun();
        m_sFilePath = "";
        m_output = new ContactOutput();      //导出联系人
        m_insert = new ContactInsert();      //导入联系人

        m_etInfo = (EditText) findViewById(R.id.et_filepath);
        m_btnHelp = (Button) findViewById(R.id.btn_help);
        m_btnHelp.setOnClickListener(this);
        m_btnInsert = (Button) findViewById(R.id.btn_insert);
        m_btnInsert.setOnClickListener(this);
        m_btnOutput = (Button) findViewById(R.id.btn_output);
        m_btnOutput.setOnClickListener(this);
        m_tvResult = (TextView) findViewById(R.id.tv_result);
        m_tvOs = (TextView)findViewById(R.id.tv_os);
        m_rbtnArrOs[0] = (RadioButton) findViewById(R.id.rbtn_win);
        // set gbk default
        m_rbtnArrOs[0].setChecked(true);
        m_rbtnArrOs[1] = (RadioButton) findViewById(R.id.rbtn_linux);
        m_rbtnArrMode[0] = (RadioButton) findViewById(R.id.rbtn_insert);
        m_rbtnArrMode[0].setOnClickListener(this);
        m_rbtnArrMode[1] = (RadioButton) findViewById(R.id.rbtn_output);
        m_rbtnArrMode[1].setOnClickListener(this);

        //启动时选中导出联系人
        m_rbtnArrMode[1].setChecked(true);
        setInsertWidgetEnabled(false);
        setOutputWidgetEnabled(true);

        //处理动态权限申请。权限不足，就进入申请权限死循环
        int iHasWriteStoragePermission = -11;
        int iHasReadContacts = -11;
        while (iHasWriteStoragePermission != PackageManager.PERMISSION_GRANTED || iHasReadContacts != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "权限不足。需要读写联系人权限、读写外部存储权限！", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS}, 1);
            iHasWriteStoragePermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            iHasReadContacts = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_CONTACTS);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_help:
                createDialog(this, ContactStrings.HELP_DIALOG_TITLE, ContactStrings.HELP_MESSAGE,
                        false, ContactStrings.DIALOG_TYPE_HELP);
                break;
            case R.id.btn_insert:
                insertContact();
                break;
            case R.id.btn_output:
                outputContact();
                break;
            case R.id.rbtn_insert:
                setInsertWidgetEnabled(true);
                setOutputWidgetEnabled(false);
                break;
            case R.id.rbtn_output:
                setInsertWidgetEnabled(false);
                setOutputWidgetEnabled(true);
                break;
        }
    }

    //为导出联系人、导出联系人设置控件状态
    private void setInsertWidgetEnabled(boolean bEnable) {
        m_rbtnArrOs[0].setEnabled(bEnable);
        m_rbtnArrOs[1].setEnabled(bEnable);
        m_btnInsert.setEnabled(bEnable);
        m_etInfo.setEnabled(bEnable);
        int iVisable = bEnable ? View.VISIBLE : View.INVISIBLE;
        m_rbtnArrOs[0].setVisibility(iVisable);
        m_rbtnArrOs[1].setVisibility(iVisable);
        m_tvOs.setVisibility(iVisable);
        if(!bEnable){
            m_tvResult.setText(ContactStrings.NO_TEXT);
        }
    }

    private void setOutputWidgetEnabled(boolean bEnable) {
        m_btnOutput.setEnabled(bEnable);
        if(!bEnable){
            m_tvResult.setText(ContactStrings.NO_TEXT);
        }
    }

    //弹出警告对话框
    public void createDialog(Context context, String title, String message,
                             boolean hasCancel, final int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(ContactStrings.DIALOG_OK,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        switch (type) {
                            case ContactStrings.DIALOG_TYPE_HELP:
                                dialog.cancel();
                                break;
                            case ContactStrings.DIALOG_TYPE_INSERT:
                                doInsertContact();
                                break;
                            case ContactStrings.DIALOG_TYPE_OUTPUT:
                                doOutputContact();
                                break;
                        }
                    }
                });
        if (hasCancel) {
            builder.setNeutralButton(ContactStrings.DIALOG_CANCEL,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });
        }
        builder.show();
    }

    //导入联系人
    private void insertContact() {
        String sPath = m_etInfo.getText().toString();
        if (sPath == null || sPath.equals(ContactStrings.NO_TEXT)) {
            ContactUtils.showToast(this, ContactStrings.FAIL_EDITTEXT_NOT_INPUT);
            m_tvResult.setText(ContactStrings.FAIL_EDITTEXT_NOT_INPUT);
            return;
        }
        sPath = ContactStrings.FILE_NAME_PARENT + sPath;
        if (!new File(sPath).exists()) {
            ContactUtils.showToast(this, ContactStrings.FAIL_FIRE_NOT_EXIST);
            m_tvResult.setText(ContactStrings.FAIL_FIRE_NOT_EXIST);
            return;
        }
        if (m_threadInsert != null) {
            m_threadInsert.interrupt();
            m_threadInsert = null;
        }
        String sCharset = m_rbtnArrOs[0].isChecked() ? ContactStrings.CHARSET_GBK : ContactStrings.CHARSET_UTF8;
        m_threadInsert = new Thread(new InsertRunnable(this, sPath, sCharset));
        createDialog(this, ContactStrings.WARNDIALOG_TITLE, ContactStrings.INSERT_WARNDIALOG_MESSAGE,
                true, ContactStrings.DIALOG_TYPE_INSERT);
    }

    //处理导入联系人线程的代码 Begin
    private void doInsertContact() {
        setInsertWidgetEnabled(false);
        m_tvResult.setText(ContactStrings.STATUS_INSERTING);
        if (m_threadInsert != null) {
            m_threadInsert.start();
        }
    }

    private void endInsertContact() {
        m_etInfo.setText(ContactStrings.NO_TEXT);
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
            boolean bResult = ContactInsert.insertIntoContact(m_context, m_sPath, m_sCharset);
            if (bResult) {
                m_handler.sendEmptyMessage(ContactStrings.INSERT_SUCCESS);
            } else {
                m_handler.sendEmptyMessage(ContactStrings.INSERT_FAIL);
            }
        }
    }
    //处理导入联系人线程的代码 End

    //导出联系人
    private void outputContact(){
        //File file = new File(ContactStrings.OUTPUT_PATH);
        File file = m_Fun.GetNewFile(m_sPathDownloads, ContactStrings.OUTPUT_FILENAME, 0);
        m_sFilePath = file.getAbsolutePath();
        if(file.exists()){
            createDialog(this, ContactStrings.WARNDIALOG_TITLE,
                    ContactStrings.OUTPUT_WARNDIALOG_MESSAGE, true,
                    ContactStrings.DIALOG_TYPE_OUTPUT);
        }else {
            doOutputContact();
        }
    }

    //处理导出联系人线程的代码 Begin
    private void doOutputContact(){
        setOutputWidgetEnabled(false);
        m_tvResult.setText(ContactStrings.STATUS_OUTPUTING);
        if (m_threadOutput != null) {
            m_threadOutput.interrupt();
            m_threadOutput = null;
        }
        m_threadOutput = new Thread(new OutputRunnable(this));
        if (m_threadOutput != null) {
            m_threadOutput.start();
        }
    }

    private void endOutputContact() {
        setOutputWidgetEnabled(true);
    }

    private Thread m_threadOutput;

    class OutputRunnable implements Runnable {
        private Context m_context;

        public OutputRunnable(Context context) {
            m_context = context;
        }

        @Override
        public void run() {
            boolean result = m_output.outputAllContacts(m_context, m_sFilePath);
            if (result) {
                m_handler.sendEmptyMessage(ContactStrings.OUTPUT_SUCCESS);
            } else {
                m_handler.sendEmptyMessage(ContactStrings.OUTPUT_FAIL);
            }
        }
    }
    //处理导出联系人线程的代码 End

    //动态访问权限回调函数
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
}
