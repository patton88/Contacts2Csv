package com.example.contacts2csv;

import java.io.File;

import android.Manifest;
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
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    public static MainActivity m_MA;
    private EditText m_etFilePath;
    private Button m_btnHelp;
    private Button m_btnInsert;
    private Button m_btnOutput;
    private Button m_btnDelAll;
    private CheckBox m_chkPhoto;
    public TextView m_tvResult;
    private TextView m_tvOs;
    private TextView m_tvQuality;
    private EditText m_etQuality;

    private RadioButton[] m_rbtnArrPhoto = new RadioButton[2];
    private RadioButton[] m_rbtnArrMode = new RadioButton[2];
    public static String m_sPathDownloads;    //存储数据的默认路径
    public String m_sFilePath;              //文件路径
    public String m_sInsertFilePath;        //导入文件路径
    public static CommonFun m_Fun;    //通用函数类

    public ContactOutput m_output;      //导出联系人
    public ContactInsert m_insert;      //导入联系人
    public ContactDel m_del;            //删除联系人
    public GroupInsert m_insertGroup;   //导入群组

    //线程消息处理对象
    public Handler m_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ExtraStrings.INSERT_COUNTING:
                    m_tvResult.setText(String.format(ExtraStrings.INSERT_COUNT_UPDATE, m_insert.getSum(),
                            m_insert.getSuccessCount(), m_insert.getFailCount(), m_insert.getCurTime()));
                    break;
                case ExtraStrings.INSERT_FAIL:
                    m_tvResult.setText(ExtraStrings.FAIL_INSERT);
                    endInsertContact();
                    break;
                case ExtraStrings.INSERT_SUCCESS:
                    m_tvResult.setText(String.format(ExtraStrings.SUCCESS_INSERT, m_insert.getSuccessCount(), m_insert.getFailCount()));
                    endInsertContact();
                    break;
                case ExtraStrings.OUTPUT_FAIL:
                    m_tvResult.setText(ExtraStrings.FAIL_OUTPUT);
                    endOutputContact();
                    break;
                case ExtraStrings.OUTPUT_SUCCESS:
                    m_tvResult.setText((String.format(ExtraStrings.SUCCESS_OUTPUT + "到：\n" + m_sFilePath, m_output.getSum())));
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

    private String getUserPath() {
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
        m_sInsertFilePath = m_sPathDownloads + "/" + ExtraStrings.OUTPUT_FILENAME;
        m_output = new ContactOutput();     //导出联系人
        m_insert = new ContactInsert();     //导入联系人
        m_del = new ContactDel();           //删除联系人
        m_insertGroup = new GroupInsert();  //导入群组

        m_etFilePath = (EditText) findViewById(R.id.et_filepath);
        m_btnHelp = (Button) findViewById(R.id.btn_help);
        m_btnHelp.setOnClickListener(this);
        m_btnInsert = (Button) findViewById(R.id.btn_insert);
        m_btnInsert.setOnClickListener(this);
        m_btnOutput = (Button) findViewById(R.id.btn_output);
        m_btnOutput.setOnClickListener(this);
        m_btnDelAll = (Button) findViewById(R.id.btn_del_all);
        m_btnDelAll.setOnClickListener(this);
        m_tvResult = (TextView) findViewById(R.id.tv_result);
        m_tvOs = (TextView) findViewById(R.id.chk_photo);
        m_rbtnArrPhoto[0] = (RadioButton) findViewById(R.id.rbtn_png);
        // set png default
        m_rbtnArrPhoto[0].setChecked(true);
        m_rbtnArrPhoto[1] = (RadioButton) findViewById(R.id.rbtn_jpg);
        m_rbtnArrMode[0] = (RadioButton) findViewById(R.id.rbtn_insert);
        m_rbtnArrMode[0].setOnClickListener(this);
        m_rbtnArrMode[1] = (RadioButton) findViewById(R.id.rbtn_output);
        m_rbtnArrMode[1].setOnClickListener(this);

        m_chkPhoto = findViewById(R.id.chk_photo);
        m_chkPhoto.setOnClickListener(this);
        m_tvQuality = findViewById(R.id.tv_quality);
        m_tvQuality.setOnClickListener(this);
        m_etQuality = findViewById(R.id.et_quality);
        m_etQuality.setOnClickListener(this);
        //默认不选中
        doCheck(m_chkPhoto, false);
        //m_chkPhoto.setChecked(false);
        //setPhotoWidgetEnabled(false);
        m_etQuality.setText("100"); // 默认100
        m_etQuality.setFilters(new InputFilter[]{new InputFilterMinMax(1,100)});    //设置监听

        //处理动态权限申请。权限不足，就进入申请权限死循环
        int iHasWriteStoragePermission = -11;
        int iHasWriteContacts = -11;
        int iHasReadContacts = -11;
        while (iHasWriteStoragePermission != PackageManager.PERMISSION_GRANTED ||
                iHasWriteContacts != PackageManager.PERMISSION_GRANTED ||
                iHasReadContacts != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "权限不足。需要读写联系人权限、读写外部存储权限！", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 1);
            iHasWriteStoragePermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            iHasWriteContacts = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.WRITE_CONTACTS);
            iHasReadContacts = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_CONTACTS);
        }

        //启动时选中导出联系人
        doCheck(m_rbtnArrMode[1], true);
        //doCheck(m_rbtnArrMode[0], true);

        //m_rbtnArrMode[1].setChecked(true);
        //setInsertWidgetEnabled(false);
        //setOutputWidgetEnabled(true);
    }

    // 实现 CheckBox 选择事件的对应操作
    private void doCheck(Button chk, boolean check) {
        if (RadioButton.class.isInstance(chk)){
            ((RadioButton)chk).setChecked(check);
        } else if (CheckBox.class.isInstance(chk)){
            ((CheckBox)chk).setChecked(check);
        }
        View v = new View(this);
        v.setId(chk.getId());
        onClick(v);
    }

    @Override
    public void onClick(View v) {
        File file;
        switch (v.getId()) {
            case R.id.btn_help:
                createDialog(this, ExtraStrings.HELP_DIALOG_TITLE, ExtraStrings.HELP_MESSAGE,
                        false, ExtraStrings.DIALOG_TYPE_HELP);
                break;
            case R.id.btn_insert:
                m_del.delAllContacts();
                //m_insertGroup.delAllGroup(m_MA);
                insertContact();
                break;
            case R.id.btn_output:
                outputContact();
                break;
            case R.id.btn_del_all:
                m_del.delAllContacts();
                m_insertGroup.delAllGroup(m_MA);
                break;
            case R.id.rbtn_insert:
//                setInsertWidgetEnabled(true);
//                setOutputWidgetEnabled(false);
                file = m_Fun.GetNewFile(m_sPathDownloads, ExtraStrings.OUTPUT_FILENAME, 1);
                m_etFilePath.setText(file.getAbsolutePath());
                break;
            case R.id.rbtn_output:
//                setInsertWidgetEnabled(false);
//                setOutputWidgetEnabled(true);
                file = m_Fun.GetNewFile(m_sPathDownloads, ExtraStrings.OUTPUT_FILENAME, 0);
                m_etFilePath.setText(file.getAbsolutePath());
                break;
            case R.id.chk_photo:
                setPhotoWidgetEnabled(m_chkPhoto.isChecked());
                break;
        }
    }

    public class InputFilterMinMax implements InputFilter {
        private float min, max;

        public InputFilterMinMax(float min, float max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Float.valueOf(min);
            this.max = Float.valueOf(max);
        }

        //Android Edittext 限制输入的最大值和最小值以及小数点值位数。原创莎莉mm 2019-06-27
        //原文链接：https://blog.csdn.net/qq_35936174/article/details/93885088

        //1、首先设置Edittext的输入类型
        //两种方法：（XML布局）
        //android:inputType="numberDecimal"
        //或者
        //edit.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_CLASS_NUMBER);
        //2、重写 InputFilter
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                //限制小数点位数
                if (source.equals(".") && dest.toString().length() == 0) {
                    return "0.";
                }
                if (dest.toString().contains(".")) {
                    int index = dest.toString().indexOf(".");
                    int mlength = dest.toString().substring(index).length();
                    if (mlength == 3) {
                        return "";
                    }
                }
                //限制大小
                float input = Float.valueOf(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (Exception nfe) { }
            return "";
        }
        //3 添加监听：
        // edit.setFilters(new InputFilter[]{new InputFilterMinMax(0,100)});

        private boolean isInRange(float a, float b, float c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

    //为处理联系人 Photo 设置控件状态
    private void setPhotoWidgetEnabled(boolean bEnable) {
        m_rbtnArrPhoto[0].setEnabled(bEnable);
        m_rbtnArrPhoto[1].setEnabled(bEnable);
        m_tvQuality.setEnabled(bEnable);
        m_etQuality.setEnabled(bEnable);
    }

    //为导出联系人、导出联系人设置控件状态
    private void setInsertWidgetEnabled(boolean bEnable) {
        m_rbtnArrPhoto[0].setEnabled(bEnable);
        m_rbtnArrPhoto[1].setEnabled(bEnable);
        m_btnInsert.setEnabled(bEnable);
        m_etFilePath.setEnabled(bEnable);
        int iVisable = bEnable ? View.VISIBLE : View.INVISIBLE;
        m_rbtnArrPhoto[0].setVisibility(iVisable);
        m_rbtnArrPhoto[1].setVisibility(iVisable);
        m_tvOs.setVisibility(iVisable);
        if (!bEnable) {
            m_tvResult.setText(ExtraStrings.NO_TEXT);
        }
    }

    private void setOutputWidgetEnabled(boolean bEnable) {
        m_btnOutput.setEnabled(bEnable);
        if (!bEnable) {
            m_tvResult.setText(ExtraStrings.NO_TEXT);
        }
    }

    //弹出警告对话框
    public void createDialog(Context context, String title, String message, boolean hasCancel, final int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(ExtraStrings.DIALOG_OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                switch (type) {
                    case ExtraStrings.DIALOG_TYPE_HELP:
                        dialog.cancel();
                        break;
                    case ExtraStrings.DIALOG_TYPE_INSERT:
                        doInsertContact();
                        break;
                    case ExtraStrings.DIALOG_TYPE_OUTPUT:
                        doOutputContact();
                        break;
                }
            }
        });
        if (hasCancel) {
            builder.setNeutralButton(ExtraStrings.DIALOG_CANCEL, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            });
        }
        builder.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 导入联系人 Begin
    private void insertContact() {
        //int n = 1;
        //System.out.println("insertContact_" + n++);
        String sPath = m_etFilePath.getText().toString();
        if (TextUtils.isEmpty(sPath)) {
            m_Fun.showToast(this, ExtraStrings.FAIL_EDITTEXT_NOT_INPUT);
            m_tvResult.setText(ExtraStrings.FAIL_EDITTEXT_NOT_INPUT);
            return;
        }
        //sPath = ExtraStrings.FILE_NAME_PARENT + sPath;
        if (!new File(sPath).exists()) {
            m_Fun.showToast(this, ExtraStrings.FAIL_FIRE_NOT_EXIST);
            m_tvResult.setText(ExtraStrings.FAIL_FIRE_NOT_EXIST);
            return;
        }
        if (m_threadInsert != null) {   //若已经启动线程，先终止清空
            m_threadInsert.interrupt();
            m_threadInsert = null;
        }
        m_threadInsert = new Thread(new InsertRunnable(this, sPath));
        //createDialog(this, ExtraStrings.WARNDIALOG_TITLE, ExtraStrings.INSERT_WARNDIALOG_MESSAGE, true, ExtraStrings.DIALOG_TYPE_INSERT);
        doInsertContact();  //测试用
        //System.out.println("insertContact_" + n++);
    }

    //处理导入联系人线程的代码 Begin
    private void doInsertContact() {
        setInsertWidgetEnabled(false);
        m_tvResult.setText(ExtraStrings.STATUS_INSERTING);
        if (m_threadInsert != null) {
            m_threadInsert.start();
            //System.out.println("doInsertContact");
        }
    }

    private void endInsertContact() {
        m_etFilePath.setText(ExtraStrings.NO_TEXT);
        setInsertWidgetEnabled(true);
    }

    private Thread m_threadInsert;

    class InsertRunnable implements Runnable {
        private Context m_context;
        private String m_sPath;

        public InsertRunnable(Context context, String sPath) {
            m_sPath = sPath;
            m_context = context;
        }

        @Override
        public void run() {
            boolean bResult = m_insert.insertContacts(m_context, m_sPath);
            if (bResult) {
                m_handler.sendEmptyMessage(ExtraStrings.INSERT_SUCCESS);
            } else {
                m_handler.sendEmptyMessage(ExtraStrings.INSERT_FAIL);
            }
        }
    }
    //处理导入联系人线程的代码 End

    // 导入联系人 End
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 导出联系人 Begin
    private void outputContact() {
        //File file = new File(ExtraStrings.OUTPUT_PATH);
        File file = m_Fun.GetNewFile(m_sPathDownloads, ExtraStrings.OUTPUT_FILENAME, 0);
        m_sFilePath = file.getAbsolutePath();
        if (file.exists()) {
            createDialog(this, ExtraStrings.WARNDIALOG_TITLE, ExtraStrings.OUTPUT_WARNDIALOG_MESSAGE,
                    true, ExtraStrings.DIALOG_TYPE_OUTPUT);
        } else {
            doOutputContact();
        }
    }

    //处理导出联系人线程的代码 Begin
    private void doOutputContact() {
        setOutputWidgetEnabled(false);
        m_tvResult.setText(ExtraStrings.STATUS_OUTPUTING);
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
                m_handler.sendEmptyMessage(ExtraStrings.OUTPUT_SUCCESS);
            } else {
                m_handler.sendEmptyMessage(ExtraStrings.OUTPUT_FAIL);
            }
        }
    }
    //处理导出联系人线程的代码 End

    // 导出联系人 End
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
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
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, sPermissions[i])) {//用户选择了禁止不再询问

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
                    } else {//选择禁止
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
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
