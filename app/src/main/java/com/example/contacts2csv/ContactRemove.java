package com.example.contacts2csv;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.Toast;

public class ContactRemove {
    //android 删除所有联系人代码： getContentResolver().delete() 2013-09-25
    //getContentResolver().delete()这个函数有三个参数，要删除所有联系人，参数怎么写呢？
    //getContentResolver().delete()需要填入三个参数，才能删除全部联系人，代码如下：

    public void delAllContacts() {
        ContentResolver resolver = MainActivity.m_MA.getContentResolver();
        Cursor cur = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cur.moveToNext()) {
            try {
                String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                System.out.println("The uri is " + uri.toString());
                resolver.delete(uri, null, null);    //删除所有的联系人
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
        }
    }

    public void delAllContacts2(){
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        MainActivity.m_MA.getContentResolver().delete(uri, "_id!=-1", null);
    }

}
