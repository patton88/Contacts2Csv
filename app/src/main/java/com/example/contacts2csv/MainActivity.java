package com.example.contacts2csv;

import java.io.File;
import java.io.FileOutputStream;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    public static MainActivity m_MA;
    private boolean m_bWaitInser;    // 等待处理插入联系人的标志
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
    private Button m_btnGetLastFile;
    private Button m_btnGetUniqueFile;

    private TextView m_tvPhote;
    private TextView m_tvQuality;
    public EditText m_etQuality;
    public boolean m_bDealPhoto;
    private CheckBox m_chkDealPhoto;

    public boolean m_bDealGalaxyS10;    // 修正三星 GalaxyS10 导出少8条记录问题
    private CheckBox m_chkGalaxyS10;

    public boolean m_bFilterNameOnly;   // 剔除 jsonSource 中只有用户名、没有任何其他信息的联系人记录
    private CheckBox m_chkNameOnly;

    public int m_iAggregateSameName;            // 聚合同名联系人信息 : 0 完全相同；1 头部相同；2 尾部相同；3 任何位置相同
    public int m_iSameName;                     // 同名联系人记录计数器
    private CheckBox m_chkAggregateSameName;
    public boolean m_bAggregateSameData;        // 聚合同名联系人同样内容的数据
    private CheckBox m_chkAggregateSameData;

    public boolean m_bAggregateMimeSameData;    // 聚合所有联系人同类型同样内容的数据
    private CheckBox m_chkAggregateMimeSameData;
    public boolean m_bAggregateAllSameData;     // 聚合所有联系人同样内容的数据
    private CheckBox m_chkAggregateAllSameData;

    public TextView m_tvResult;

    public static String m_sPathDownloads;    //存储数据的默认路径，无文件名
    public String m_sFileAbsolutePath;        //文件绝对路径，包括完整的目录和文件名
    public static CommonFun m_Fun;    //通用函数类

    public ContactOutput m_output;      //导出联系人
    public ContactInsert m_insert;      //导入联系人
    public ContactDel m_del;            //删除联系人
    public GroupInsert m_insertGroup;   //导入群组

    private AlertDialog m_DlgCheck;     // 对话框。控件命名以m_大写字母开头,类型用m_小写字母开头
    private TextView m_tvDlg;

    private String m_sAppName;

    Spinner m_spinnerPhotoType;

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
                    m_tvResult.setText(String.format(ExtraStrings.SUCCESS_INSERT, m_insert.getSuccessCount(),
                            m_insert.getFailCount(), m_insert.getCurTime()));
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
                    m_tvResult.setText(String.format(ExtraStrings.SUCCESS_OUTPUT, m_output.getSuccessCount() - m_MA.m_iSameName,
                            m_output.getFailCount(),  m_MA.m_iSameName, m_output.getCurTime()));
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

    // Android再次激活Activity时触发事件用于列表重新读取载入，重载 onResume() 方法
    @Override
    protected void onResume(){
        super.onResume();
        setWidgetsEnable(false);
        if (!(m_bOutputing || m_bInserting || m_bDeling)) {
            setWidgetsEnable(true);
        }
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

        m_bDealGalaxyS10 = true;            // 处理三星 GalaxyS10

        m_sFileAbsolutePath = "";
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
        m_btnGetLastFile = (Button) findViewById(R.id.btn_get_last_file);
        m_btnGetLastFile.setOnClickListener(this);
        m_btnGetUniqueFile = (Button) findViewById(R.id.btn_get_unique_file);
        m_btnGetUniqueFile.setOnClickListener(this);

        m_tvResult = (TextView) findViewById(R.id.tv_result);

        m_tvPhote = (TextView) findViewById(R.id.chk_deal_photo);
        m_spinnerPhotoType = (Spinner) this.findViewById(R.id.spinner_photo_type);  //获取mSpinnerGaps控件句柄，并设置参数
        ArrayAdapter adapterGaps = new ArrayAdapter(this, android.R.layout.simple_spinner_item, new String[]{"png", "jpg"});
        adapterGaps.setDropDownViewResource(android.R.layout.simple_list_item_1);   //设置Spinner控件下拉样式
        m_spinnerPhotoType.setAdapter(adapterGaps);

        m_iAggregateSameName = 3;           // 聚合同名联系人信息 : 0 完全相同；1 头部相同；2 尾部相同；3 任何位置相同
        m_bAggregateSameData = true;        // 聚合同名联系人同样内容的数据
        m_bAggregateAllSameData = true;     // 聚合所有联系人同样内容的数据
        m_bAggregateMimeSameData = true;    // 聚合所有联系人同类型同样内容的数据

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

        m_chkGalaxyS10 = findViewById(R.id.chk_adapt_galaxy_s10);
        m_chkGalaxyS10.setOnClickListener(this);
        m_bDealGalaxyS10 = false;    // 默认不处理，修正三星 GalaxyS10 导出少8条记录问题

        m_chkNameOnly = findViewById(R.id.chk_filter_name_only);
        m_chkNameOnly.setOnClickListener(this);
        m_bFilterNameOnly = false;   // 默认不选。剔除 jsonSource 中只有用户名、没有任何其他信息的联系人记录

        m_bWaitInser = false;    // 等待处理插入联系人的标志

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
        //doCheck(m_rbtnArrMode[0], true);
        //m_etFilePath.setText("/storage/sdcard/Android/data/com.example.contacts2csv/files/Download/Contacts_4.txt");
        m_sFileAbsolutePath = m_Fun.getNewAbsolutePath(m_sPathDownloads, ExtraStrings.OUTPUT_FILENAME, 1);
        m_etFilePath.setText(m_sFileAbsolutePath);
        m_Fun.putinClipboard(m_sFileAbsolutePath);

        //m_rbtnArrMode[1].setChecked(true);
        //setWidgetsEnable(false);
        //setOutputWidgetEnabled(true);

        //android获取项目名称
        m_sAppName = getApplicationInfo().loadLabel(getPackageManager()).toString();   //m_sAppName = "章鱼短信"
        //m_Fun.logString(m_sAppName);
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
                //createDialog(this, ExtraStrings.HELP_DIALOG_TITLE, ExtraStrings.HELP_MESSAGE,
                //        false, ExtraStrings.DIALOG_TYPE_HELP);
                createHelpDialog(this, getString(R.string.dlg_help_title), ExtraStrings.HELP_MESSAGE, true);
                if (m_bOutputing || m_bInserting || m_bDeling) {
                    setWidgetsEnable(false);
                }
                break;
            case R.id.btn_insert:
                //m_del.delAllContacts(this);
                //delAndInsertContact();
                m_sFileAbsolutePath = m_etFilePath.getText().toString();
                createDialog(this, ExtraStrings.WARNDIALOG_TITLE,
                        ExtraStrings.INSERT_WARNDIALOG_MESSAGE + "\n\n" + m_sFileAbsolutePath,
                        true, ExtraStrings.DIALOG_TYPE_INSERT);
                break;
            case R.id.btn_output:
                m_sFileAbsolutePath = m_Fun.getNewAbsolutePath(m_sPathDownloads, ExtraStrings.OUTPUT_FILENAME, 0);
                createDialog(this, ExtraStrings.WARNDIALOG_TITLE,
                        ExtraStrings.OUTPUT_WARNDIALOG_MESSAGE + "\n\n" + m_sFileAbsolutePath,
                        true, ExtraStrings.DIALOG_TYPE_OUTPUT);
                break;
            case R.id.btn_del_all:
                createDialog(this, ExtraStrings.WARNDIALOG_TITLE, ExtraStrings.DEL_ALL_WARNDIALOG_MESSAGE,
                        true, ExtraStrings.DIALOG_TYPE_DEL_ALL);
                //m_del.delAllContacts(this);
                //m_insertGroup.delAllGroup(this);
                //delAndInsertContact();
                break;
            case R.id.btn_get_last_file:
                m_sFileAbsolutePath = m_Fun.getNewAbsolutePath(m_sPathDownloads, ExtraStrings.OUTPUT_FILENAME, 1);
                m_etFilePath.setText(m_sFileAbsolutePath);
                m_Fun.putinClipboard(m_sFileAbsolutePath);
                Toast.makeText(m_MA, m_sFileAbsolutePath + "\n已经复制到剪贴板", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_get_unique_file:
                m_sFileAbsolutePath = m_Fun.getNewAbsolutePath(m_sPathDownloads, ExtraStrings.OUTPUT_FILENAME, 0);
                m_etFilePath.setText(m_sFileAbsolutePath);
                m_Fun.putinClipboard(m_sFileAbsolutePath);
                Toast.makeText(m_MA, m_sFileAbsolutePath + "\n已经复制到剪贴板", Toast.LENGTH_SHORT).show();
                break;
            case R.id.chk_deal_photo:
                m_bDealPhoto = m_chkDealPhoto.isChecked();
                setPhotoWidgetEnabled(m_bDealPhoto);
                break;
            case R.id.chk_adapt_galaxy_s10:
                m_bDealGalaxyS10 = m_chkGalaxyS10.isChecked();    // 修正三星 GalaxyS10 导出少8条记录问题
                break;
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
        m_spinnerPhotoType.setEnabled(bEnable);
        m_tvQuality.setEnabled(bEnable);
        m_etQuality.setEnabled(bEnable);
    }

    //为导出联系人、导出、删除联系人设置控件状态
    private void setWidgetsEnable(boolean bEnable) {
        m_btnInsert.setEnabled(bEnable);
        m_btnOutput.setEnabled(bEnable);
        m_btnBrowse.setEnabled(bEnable);
        m_btnDelAll.setEnabled(bEnable);
        m_btnGetLastFile.setEnabled(bEnable);
        m_btnGetUniqueFile.setEnabled(bEnable);

        m_chkDealPhoto.setEnabled(bEnable);
        m_chkGalaxyS10.setEnabled(bEnable);
        m_chkNameOnly.setEnabled(bEnable);
        setPhotoWidgetEnabled(bEnable && m_chkDealPhoto.isChecked());

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
        m_tvDlg = new TextView(this);
        //为对话框设置多选按钮
        final String[] items = {"处理联系人群组信息"};
        final boolean[] checkeds = {true};
        if (type == ExtraStrings.DIALOG_TYPE_INSERT || type == ExtraStrings.DIALOG_TYPE_DEL_ALL ) {
            builder.setMultiChoiceItems(items, checkeds, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    //m_bDealGroup = checkeds[which];
                    String mes = "";
                    if (type == ExtraStrings.DIALOG_TYPE_INSERT) {
                        mes = checkeds[0] ? ExtraStrings.INSERT_WARNDIALOG_MESSAGE : ExtraStrings.INSERT_WARNDIALOG_MESSAGE_NoGroup;
                        mes += "\n\n" + m_sFileAbsolutePath;
                    } else if (type == ExtraStrings.DIALOG_TYPE_DEL_ALL) {
                        mes = checkeds[0] ? ExtraStrings.DEL_ALL_WARNDIALOG_MESSAGE : ExtraStrings.DEL_ALL_WARNDIALOG_MESSAGE_NoGroup;
                    }
                    m_tvDlg.setText("    " + mes);
                }
            });
        }
        m_tvDlg.setText("    " + message);
        builder.setView(m_tvDlg);

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
                        // 该函数是为了避免 delAndInsertContact() 处理上千条记录用时太长，对话框进程已经被清除，导致无法执行后面的 insertContact()
                        m_bWaitInser = true;    // 等待处理插入联系人的标志
                        InsertAfterDel();
                        break;
                    case ExtraStrings.DIALOG_TYPE_OUTPUT:
                        outputContact();
                        break;
                    case ExtraStrings.DIALOG_TYPE_DEL_ALL:
                        if (checkeds[0]) {
                            m_insertGroup.delAllGroup(m_MA);
                            //m_Fun.logString(checkeds[0]);
                        }
                        delAndInsertContact();
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

    public void createHelpDialog(Context context, String title, String message, boolean hasCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.dlg_help_btn_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

        // 为对话框设置中立按钮，复制按钮，点击后对话框不会关闭
        builder.setNeutralButton(getString(R.string.dlg_help_copy), null);

        if (hasCancel) {
            //为对话框设置取消按钮，但是不添加监听
            builder.setNegativeButton(getString(R.string.dlg_save_btn_save), null);
        }

        final AlertDialog dlgSave = builder.create();
        //这里必须要先调show()方法，后面的getButton才有效
        dlgSave.show();         // 使用show()方法显示对话框

        dlgSave.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Android 内容复制到剪贴板
                ClipboardManager cm;
                ClipData m_ClipData;
                //获取剪贴板管理器：
                cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                m_ClipData = ClipData.newPlainText(m_sAppName, ExtraStrings.HELP_MESSAGE);
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(m_ClipData);
                Toast.makeText(MainActivity.this, getString(R.string.dlg_help_copy_info), Toast.LENGTH_SHORT).show();
            }
        });

        if (hasCancel) {
            // 获取到对话框上的取消按钮，然后对该按钮添加普通的View.OnClickListener。
            dlgSave.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_Fun.SaveFile2(m_MA.m_sPathDownloads, getString(R.string.file_txt_help), ExtraStrings.HELP_MESSAGE);
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 导入联系人 Begin
    private Thread m_threadInsert;

    private void insertContact() {
        // m_sFileAbsolutePath 中已经存放导入文件绝对路径
        if (TextUtils.isEmpty(m_sFileAbsolutePath)) {
            m_Fun.showToast(this, ExtraStrings.FAIL_EDITTEXT_NOT_INPUT);
            m_tvResult.setText(ExtraStrings.FAIL_EDITTEXT_NOT_INPUT);
            return;
        }
        if (!new File(m_sFileAbsolutePath).exists()) {
            m_Fun.showToast(this, ExtraStrings.FAIL_FIRE_NOT_EXIST);
            m_tvResult.setText(ExtraStrings.FAIL_FIRE_NOT_EXIST);
            return;
        }

        if (m_threadInsert != null) {   //若已经启动线程，先终止清空
            m_threadInsert.interrupt();
            m_threadInsert = null;
        }
        m_threadInsert = new Thread(new InsertRunnable(this, m_sFileAbsolutePath));
        //createDialog(this, ExtraStrings.WARNDIALOG_TITLE, ExtraStrings.INSERT_WARNDIALOG_MESSAGE, true, ExtraStrings.DIALOG_TYPE_INSERT);
        doInsertContact();
        //System.out.println("insertContact_" + n++);
    }

    //处理导入联系人线程的代码 Begin
    private void doInsertContact() {
        if (m_threadInsert != null) {
            m_bInserting = true;
            setWidgetsEnable(false);
            m_etFilePath.setText(m_sFileAbsolutePath);
            m_Fun.putinClipboard(m_sFileAbsolutePath);
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
            boolean bResult = m_insert.insertAllContacts(m_context, m_sPath);
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
        // m_sFileAbsolutePath 中已经存放导出文件绝对路径
        File file = new File(m_sFileAbsolutePath);
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
            m_etFilePath.setText(m_sFileAbsolutePath);
            m_Fun.putinClipboard(m_sFileAbsolutePath);
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
            boolean result = m_output.outputAllContacts(m_context, m_sFileAbsolutePath);
            if (result) {
                m_handler.sendEmptyMessage(ExtraStrings.OUTPUT_SUCCESS);
            } else {
                m_handler.sendEmptyMessage(ExtraStrings.OUTPUT_FAIL);
            }
        }
    }

    // 导出联系人 End
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // 该函数是为了避免 delAndInsertContact() 处理上千条记录用时太长，对话框进程已经被清除，导致无法执行后面的 insertContact()
    private void InsertAfterDel() {
        // 删除记录前先判断要导入的文件是否存在更为稳妥。避免出现不存在要导入的文件，又已经删除记录的情况
        // m_sFileAbsolutePath 中已经存放导入文件绝对路径
        if (TextUtils.isEmpty(m_sFileAbsolutePath)) {
            m_Fun.showToast(this, ExtraStrings.FAIL_EDITTEXT_NOT_INPUT);
            m_tvResult.setText(ExtraStrings.FAIL_EDITTEXT_NOT_INPUT);
            return;
        }
        if (!new File(m_sFileAbsolutePath).exists()) {
            m_Fun.showToast(this, ExtraStrings.FAIL_FIRE_NOT_EXIST);
            m_tvResult.setText(ExtraStrings.FAIL_FIRE_NOT_EXIST);
            return;
        }
        // 由于该操作在启动一个新线程开始删除操作后便立即返回，然后紧接着便会启动下面的insertContact()，
        // 所以将导致删除操作和插入操作同时进行，这会导致许多问题。为避免这种情况，进行如下改进
        // 设置 m_bWaitInser，在需要删除后进行插入操作时设置为 true；将 delContact() 改为 delAndInsertContact()，
        // 等删除操作完成后，在 endDelContact() 函数中判断 m_bWaitInser == true，再启动插入操作 insertContact()
        delAndInsertContact();
        // insertContact();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 删除联系人完成后处理插入操作 Begin
    private Thread m_threadDel;

    private void delAndInsertContact() {
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
        if (m_bWaitInser) {    // 等待处理插入联系人的标志
            insertContact();
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
                m_sFileAbsolutePath = pathAll;
                m_etFilePath.setText(m_sFileAbsolutePath);
                m_Fun.putinClipboard(m_sFileAbsolutePath);
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.dlg_info_1), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // End 调用系统文件浏览组件，返回文件路径
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
