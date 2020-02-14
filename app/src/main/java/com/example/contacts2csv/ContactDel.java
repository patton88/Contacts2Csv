package com.example.contacts2csv;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.widget.Toast;

import java.text.DecimalFormat;

import static com.example.contacts2csv.MainActivity.m_MA;

public class ContactDel {
    //android 删除所有联系人代码： getContentResolver().delete() 2013-09-25
    //getContentResolver().delete()这个函数有三个参数，要删除所有联系人，参数怎么写呢？
    //getContentResolver().delete()需要填入三个参数，才能删除全部联系人，代码如下：
    private long m_lStartTimer;
    public int m_iSum;
    public int m_iSuccess;
    public int m_iFail;

    public boolean delAllContacts(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Contacts.CONTENT_URI, null, null, null, null);

        m_iSum = cursor.getCount();
        m_lStartTimer = SystemClock.elapsedRealtime();      // 计时器起始时间
        m_iSuccess = 0;
        m_iFail = 0;
        while (cursor.moveToNext()) {
            try {
                String lookupKey = cursor.getString(cursor.getColumnIndex(Contacts.LOOKUP_KEY));
                Uri uri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);
                resolver.delete(uri, null, null);    //删除所有的联系人
                m_iSuccess++;
            } catch (Exception e) {
                e.printStackTrace();
                m_iFail++;
            }
            //m_MA.m_tvResult.setText(String.format(ExtraStrings.DEL_COUNT_UPDATE, m_iSum, m_iSuccess, m_iFail, getCurTime()));
            m_MA.m_handler.sendEmptyMessage(ExtraStrings.DEL_COUNTING);          // 更新删除联系人计数
        }
        //Toast.makeText(m_MA, "成功删除 " + m_iSuccess + " 记录，" + m_iFail + " 条记录删除失败", Toast.LENGTH_SHORT).show();
        return true;
    }

    public String getCurTime () {
        int time = (int)((SystemClock.elapsedRealtime() - m_lStartTimer) / 1000);
        //String hh = new DecimalFormat("00").format(time / 3600);
        String mm = new DecimalFormat("00").format(time % 3600 / 60);
        String ss = new DecimalFormat("00").format(time % 60);
        //String timeFormat = new String(hh + ":" + mm + ":" + ss);
        String timeFormat = new String(mm + "分" + ss + "秒");

        return timeFormat;
    }

    public void delAllContacts2(){
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        m_MA.getContentResolver().delete(uri, "_id!=-1", null);
    }

}
