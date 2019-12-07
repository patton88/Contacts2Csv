package com.example.contacts2csv;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
    private int iSum;
    private EditText mEditText;
    private Button mHelpButton;
    private Button mInsertButton;
    private Button mOutputButton;
    private TextView mResultTextView;
    private TextView mOsTextView;
    //private RadioButton[] mOsSetButtons = new RadioButton[2];
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
        //mOsTextView = (TextView)findViewById(R.id.os_text);
        //mOsSetButtons[0] = (RadioButton) findViewById(R.id.radio_button_win);
        // set gbk default
        //mOsSetButtons[0].setChecked(true);
        //mOsSetButtons[1] = (RadioButton) findViewById(R.id.radio_button_linux);
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
                createDialog(this, ContactContant.HELP_DIALOG_TITLE, ContactContant.HELP_MESSAGE,false,
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
//        mOsSetButtons[0].setEnabled(enable);
//        mOsSetButtons[1].setEnabled(enable);
        mInsertButton.setEnabled(enable);
        mEditText.setEnabled(enable);
        int visable = enable ? View.VISIBLE : View.INVISIBLE;
//        mOsSetButtons[0].setVisibility(visable);
//        mOsSetButtons[1].setVisibility(visable);
        //mOsTextView.setVisibility(visable);
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
//        String charset = mOsSetButtons[0].isChecked() ? ContactContant.CHARSET_GBK
//                : ContactContant.CHARSET_UTF8;
//        mInsertThread = new Thread(new InsertRunnable(this, path, charset));
//        createDialog(this, ContactContant.WARNDIALOG_TITLE,
//                ContactContant.INSERT_WARNDIALOG_MESSAGE, true,
//                ContactContant.DIALOG_TYPE_INSERT);
    }

    private void doInsertContact() {
        //setInsertWidgetEnabled(false);
        mResultTextView.setText(ContactContant.STATUS_INSERTING);
        if (mInsertThread != null) {
            mInsertThread.start();
        }
    }

    private void endInsertContact() {
        mEditText.setText(ContactContant.NO_TEXT);
        //setInsertWidgetEnabled(true);
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

    private void outputContact(){
        File file = new File(ContactContant.OUTPUT_PATH);
        if(file.exists()){
            createDialog(this, ContactContant.WARNDIALOG_TITLE,
                    ContactContant.OUTPUT_WARNDIALOG_MESSAGE, true,
                    ContactContant.DIALOG_TYPE_OUTPUT);
        }else {
            doOutputContact();
        }
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
