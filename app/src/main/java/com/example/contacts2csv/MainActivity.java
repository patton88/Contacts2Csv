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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    public static MainActivity m_MA;
    private boolean m_bInserting;    // 正在处理过程中的标志
    private boolean m_bOutputing;    // 正在处理过程中的标志
    private boolean m_bDeling;       // 正在处理过程中的标志

    private boolean m_bDealGroup;    // 是否处理群组信息

    private EditText m_etFilePath;
    private Button m_btnHelp;
    private Button m_btnInsert;
    private Button m_btnOutput;
    private Button m_btnDelAll;
    private Button m_btnBrowse;

    private TextView m_tvPhote;
    private TextView m_tvQuality;
    private EditText m_etQuality;
    public boolean m_bDealPhoto;
    private CheckBox m_chkDealPhoto;

    public boolean m_bFilterNameOnly;   // 剔除 jsonSource 中只有用户名、没有任何其他信息的联系人记录
    private CheckBox m_chkNameOnly;

    public TextView m_tvResult;

    private RadioButton[] m_rbtnArrPhoto = new RadioButton[2];
    private RadioButton[] m_rbtnArrMode = new RadioButton[2];
    public static String m_sPathDownloads;    //存储数据的默认路径
    public String m_sFilePath;              //目录绝对路径，末尾不含斜杠、不含文件名。Import输入信息的文件路径
    private String m_sFileName;             //前面不含目录和斜杠的单纯文件名。Import输入信息的文件名
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
                case ExtraStrings.INSERT_FAIL:
                    m_tvResult.setText(ExtraStrings.FAIL_INSERT);
                    endInsertContact();
                    break;
                case ExtraStrings.INSERT_SUCCESS:
                    m_tvResult.setText(String.format(ExtraStrings.SUCCESS_INSERT,
                            m_insert.getSuccessCount(), m_insert.getFailCount(), m_insert.getCurTime()));
                    endInsertContact();
                    break;
                case ExtraStrings.INSERT_COUNTING:
                    m_tvResult.setText(String.format(ExtraStrings.INSERT_COUNT_UPDATE, m_insert.getSum(),
                            m_insert.getSuccessCount(), m_insert.getFailCount(), m_insert.getCurTime()));
                    break;
                case ExtraStrings.OUTPUT_FAIL:
                    m_tvResult.setText(ExtraStrings.FAIL_OUTPUT);
                    endOutputContact();
                    break;
                case ExtraStrings.OUTPUT_SUCCESS:
                    m_tvResult.setText(String.format(ExtraStrings.SUCCESS_OUTPUT,
                            m_output.getSuccessCount(), m_output.getFailCount(), m_output.getCurTime()));
                    endOutputContact();
                    break;
                case ExtraStrings.OUTPUT_COUNTING:
                    m_tvResult.setText(String.format(ExtraStrings.OUTPUT_COUNT_UPDATE, m_output.getSum(),
                            m_output.getSuccessCount(), m_output.getFailCount(), m_output.getCurTime()));
                    break;
                case ExtraStrings.DEL_FAIL:
                    m_tvResult.setText(ExtraStrings.FAIL_DEL);
                    endDelContact();
                    break;
                case ExtraStrings.DEL_SUCCESS:
                    m_tvResult.setText(String.format(ExtraStrings.SUCCESS_DEL,
                            m_del.m_iSuccess, m_del.m_iFail, m_del.getCurTime()));
                    endDelContact();
                    break;
                case ExtraStrings.DEL_COUNTING:
                    m_tvResult.setText(String.format(ExtraStrings.DEL_COUNT_UPDATE,
                            m_del.m_iSum, m_del.m_iSuccess, m_del.m_iFail, m_del.getCurTime()));
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
        m_sFileName = "";
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
        m_btnBrowse = (Button) findViewById(R.id.btn_browse_file);
        m_btnBrowse.setOnClickListener(this);

        m_chkNameOnly = findViewById(R.id.chk_filter_name_only);
        m_chkNameOnly.setOnClickListener(this);
        m_bFilterNameOnly = false;   // 默认不选。剔除 jsonSource 中只有用户名、没有任何其他信息的联系人记录
        m_chkNameOnly.setChecked(m_bFilterNameOnly);

        m_tvResult = (TextView) findViewById(R.id.tv_result);
        m_tvPhote = (TextView) findViewById(R.id.chk_deal_photo);
        m_rbtnArrPhoto[0] = (RadioButton) findViewById(R.id.rbtn_png);
        // set png default
        m_rbtnArrPhoto[0].setChecked(true);
        m_rbtnArrPhoto[1] = (RadioButton) findViewById(R.id.rbtn_jpg);
        m_rbtnArrMode[0] = (RadioButton) findViewById(R.id.rbtn_insert);
        m_rbtnArrMode[0].setOnClickListener(this);
        m_rbtnArrMode[1] = (RadioButton) findViewById(R.id.rbtn_output);
        m_rbtnArrMode[1].setOnClickListener(this);

        m_chkDealPhoto = findViewById(R.id.chk_deal_photo);
        m_chkDealPhoto.setOnClickListener(this);
        m_tvQuality = findViewById(R.id.tv_quality);
        m_tvQuality.setOnClickListener(this);
        m_etQuality = findViewById(R.id.et_quality);
        m_etQuality.setOnClickListener(this);
        m_bDealPhoto = false;
        //默认不选中
        doCheck(m_chkDealPhoto, m_bDealPhoto);
        //m_chkDealPhoto.setChecked(false);
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
        //doCheck(m_rbtnArrMode[1], true);
        doCheck(m_rbtnArrMode[0], true);
        m_etFilePath.setText("/storage/sdcard/Android/data/com.example.contacts2csv/files/Download/Contacts_4.txt");

        //m_rbtnArrMode[1].setChecked(true);
        //setWidgetsEnable(false);
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
        switch (v.getId()) {
            case R.id.btn_help:
                createDialog(this, ExtraStrings.HELP_DIALOG_TITLE, ExtraStrings.HELP_MESSAGE,
                        false, ExtraStrings.DIALOG_TYPE_HELP);
                if (m_bOutputing || m_bInserting || m_bDeling) {
                    setWidgetsEnable(false);
                }
                break;
            case R.id.btn_insert:
                //m_del.delAllContacts(this);
                //delContact();
                createDialog(this, ExtraStrings.WARNDIALOG_TITLE,
                        ExtraStrings.INSERT_WARNDIALOG_MESSAGE + "\n\n" + m_etFilePath.getText().toString(),
                        true, ExtraStrings.DIALOG_TYPE_INSERT);
                break;
            case R.id.btn_output:
                File file = m_Fun.GetNewFile(m_sPathDownloads, ExtraStrings.OUTPUT_FILENAME, 0);
                m_sFilePath = file.getAbsolutePath();
                createDialog(this, ExtraStrings.WARNDIALOG_TITLE,
                        ExtraStrings.OUTPUT_WARNDIALOG_MESSAGE + "\n\n" + m_sFilePath,
                        true, ExtraStrings.DIALOG_TYPE_OUTPUT);
                break;
            case R.id.btn_del_all:
                createDialog(this, ExtraStrings.WARNDIALOG_TITLE, ExtraStrings.DEL_ALL_WARNDIALOG_MESSAGE,
                        true, ExtraStrings.DIALOG_TYPE_DEL_ALL);
                //m_del.delAllContacts(this);
                //m_insertGroup.delAllGroup(this);
                //delContact();
                break;
            case R.id.rbtn_insert:
//                setWidgetsEnable(true);
//                setOutputWidgetEnabled(false);
                file = m_Fun.GetNewFile(m_sPathDownloads, ExtraStrings.OUTPUT_FILENAME, 1);
                m_etFilePath.setText(file.getAbsolutePath());
                break;
            case R.id.rbtn_output:
//                setWidgetsEnable(false);
//                setOutputWidgetEnabled(true);
                file = m_Fun.GetNewFile(m_sPathDownloads, ExtraStrings.OUTPUT_FILENAME, 0);
                m_etFilePath.setText(file.getAbsolutePath());
                break;
            case R.id.chk_deal_photo:
                m_bDealPhoto = m_chkDealPhoto.isChecked();
                setPhotoWidgetEnabled(m_bDealPhoto);
            case R.id.chk_filter_name_only:
                m_bFilterNameOnly = m_chkNameOnly.isChecked();
                break;
            case R.id.btn_browse_file:     //浏览文件
                //Android调用系统自带的文件管理器进行文件选择并获得路径
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");  //设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                //回调函数 onActivityResult 响应了选择文件的操作，Android中调用文件管理器并返回选中文件的路径。
                //该语句将调用回调函数 onActivityResult，不再返回，其后面的语句不会被执行
                startActivityForResult(intent, 1);
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

    //为导出联系人、导出、删除联系人设置控件状态
    private void setWidgetsEnable(boolean bEnable) {
        m_btnInsert.setEnabled(bEnable);
        m_btnOutput.setEnabled(bEnable);
        m_btnBrowse.setEnabled(bEnable);
        m_btnDelAll.setEnabled(bEnable);

        m_chkDealPhoto.setEnabled(bEnable);
        m_chkNameOnly.setEnabled(bEnable);
        setPhotoWidgetEnabled(bEnable && m_chkDealPhoto.isChecked());

        m_rbtnArrMode[0].setEnabled(bEnable);
        m_rbtnArrMode[1].setEnabled(bEnable);

        m_etFilePath.setEnabled(bEnable);
    }

    // 带可选项的对话框
    public void createDialog(Context context, String title, String message, boolean hasCancel, final int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // 为对话框设置标题，会自动折行，setTitle只能显示2行(setMessage可显示多行)。添加该行可以显示对话框多选项
        builder.setTitle(title);
        //builder.setMessage(message);  // 为对话框设置内容，会自动折行。添加该行不能显示对话框多选项
        builder.setIcon(android.R.drawable.ic_dialog_info);     // 设置图标

        // 在带可选项的对话框中不能使用 setMessage 。可以用下面方法为对话框添加文本标签，用于显示多行信息
        TextView TvDlg = new TextView(this);
        TvDlg.setText("    " + message);
        builder.setView(TvDlg);

        //为对话框设置多选按钮
        final String[] items = {"处理联系人群组信息"};
        final boolean[] checkeds = {true};
        if (type == ExtraStrings.DIALOG_TYPE_INSERT || type == ExtraStrings.DIALOG_TYPE_DEL_ALL ) {
            builder.setMultiChoiceItems(items, checkeds, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    //m_bDealGroup = checkeds[which];
                }
            });
        }

        // 为对话框设置确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                switch (type) {
                    case ExtraStrings.DIALOG_TYPE_HELP:
                        dialog.cancel();
                        break;
                    case ExtraStrings.DIALOG_TYPE_INSERT:
                        if (checkeds[0]) {
                            m_insertGroup.delAllGroup(m_MA);
                            m_Fun.logString(checkeds[0]);
                        }
                        delContact();
                        insertContact();
                        break;
                    case ExtraStrings.DIALOG_TYPE_OUTPUT:
                        outputContact();
                        break;
                    case ExtraStrings.DIALOG_TYPE_DEL_ALL:
                        if (checkeds[0]) {
                            m_insertGroup.delAllGroup(m_MA);
                            //m_Fun.logString(checkeds[0]);
                        }
                        delContact();
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

        // 为对话框设置中立按钮
        //builder.setNeutralButton(getString(R.string.dlg_save_btn_browse), null);

        builder.show();         // 使用show()方法显示对话框
    }

    //弹出警告对话框
    public void createDialog0(Context context, String title, String message, boolean hasCancel, final int type) {
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
                        m_insertGroup.delAllGroup(m_MA);
                        delContact();
                        insertContact();
                        break;
                    case ExtraStrings.DIALOG_TYPE_OUTPUT:
                        outputContact();
                        break;
                    case ExtraStrings.DIALOG_TYPE_DEL_ALL:
                        m_insertGroup.delAllGroup(m_MA);
                        delContact();
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
    private Thread m_threadInsert;

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
        doInsertContact();
        //System.out.println("insertContact_" + n++);
    }

    //处理导入联系人线程的代码 Begin
    private void doInsertContact() {
        if (m_threadInsert != null) {
            m_bInserting = true;
            setWidgetsEnable(false);
            m_threadInsert.start();
        }
    }

    private void endInsertContact() {
        m_bInserting = false;
        if (!(m_bOutputing || m_bInserting || m_bDeling)) {
            setWidgetsEnable(true);
        }
    }

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
    private Thread m_threadOutput;

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
        if (m_threadOutput != null) {
            m_threadOutput.interrupt();
            m_threadOutput = null;
        }
        m_threadOutput = new Thread(new OutputRunnable(this));
        if (m_threadOutput != null) {
            m_bOutputing = true;
            setWidgetsEnable(false);
            m_threadOutput.start();
        }
    }

    private void endOutputContact() {
        m_bOutputing = false;
        if (!(m_bOutputing || m_bInserting || m_bDeling)) {
            setWidgetsEnable(true);
        }
    }

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

    // 导出联系人 End
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 删除联系人 Begin
    private Thread m_threadDel;

    private void delContact() {
        if (m_threadDel != null) {   //若已经启动线程，先终止清空
            m_threadDel.interrupt();
            m_threadDel = null;
        }

        m_threadDel = new Thread(new DelRunnable(this));
        if (m_threadDel != null) {
            m_bDeling = true;
            setWidgetsEnable(false);
            m_threadDel.start();
        }
    }

    private void endDelContact() {
        m_bDeling = false;
        if (!(m_bOutputing || m_bInserting || m_bDeling)) {
            setWidgetsEnable(true);
        }
    }

    class DelRunnable implements Runnable {
        private Context m_context;

        public DelRunnable(Context context) {
            m_context = context;
        }

        @Override
        public void run() {
            boolean bResult = m_del.delAllContacts(m_context);
            if (bResult) {
                m_handler.sendEmptyMessage(ExtraStrings.DEL_SUCCESS);
            } else {
                m_handler.sendEmptyMessage(ExtraStrings.DEL_FAIL);
            }
        }
    }

    // 删除联系人 End
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Begin 调用系统文件浏览组件，返回文件路径

    // startActivityForResult 函数的回调函数 onActivityResult 响应了选择文件的操作。
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //高版本onActivityResult必须调用父类的onActivityResult，否则报错：overriding should call super.onActivityResult
        super.onActivityResult(requestCode, resultCode, data);
        String pathAll = "";

        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) { //使用第三方应用打开
                pathAll = uri.getPath();
                return;
            }

            pathAll = FileUriUtils.getRealPathFromUri(this, uri);

            //若选择文件后缀不是txt，则不做任何操作
            if (m_Fun.getFileSuffix(pathAll).equals("txt")) {
                m_sFilePath = m_Fun.getFilePath(pathAll);
                m_sFileName = m_Fun.getFileName(pathAll);
                m_etFilePath.setText(m_sFilePath + "/" + m_sFileName);
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.dlg_info_1), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // End 调用系统文件浏览组件，返回文件路径
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
