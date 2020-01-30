package com.example.contacts2csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.net.Uri;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

public class ContactInsert {
    private final String m_sTAG = getClass().getSimpleName();
    private int m_iSuccessCount = 0;
    private int m_iFailCount = 0;
    private String m_sHead[];
    private ArrayList<ContactInfo> m_contactArrayList;

    private void init() {
        m_iSuccessCount = 0;
        m_iFailCount = 0;
    }

    public boolean insertContacts(Context context, String sPath) {
        init();
        try {
            ArrayList<String> arrList = readFile(sPath);        //从文件读取联系人信息存入arrList
            m_contactArrayList = handleReadStrings(arrList);    //获得联系人信息存入m_contactArrayList

            for (ContactInfo contact : m_contactArrayList) {
                if (doInsertContact(context, contact)) {
                    m_iSuccessCount++;
                }
            }

        } catch (Exception e) {
            Log.e(m_sTAG, "Error in insertContacts result : " + e.getMessage());
        }
        return true;
    }

    // insert into database
    private boolean doInsertContact(Context context, ContactInfo contactInfo) {
        Log.d(m_sTAG, "in doInsertIntoContact contactInfo = null? " + (contactInfo == null));
        try {
            ContentValues contentValues = new ContentValues();
            Uri uri = context.getContentResolver().insert(RawContacts.CONTENT_URI, contentValues);
            long rowId = ContentUris.parseId(uri);

            //insert name
            if (contactInfo.displayName != null) {
                contentValues.clear();
                contentValues.put(Data.RAW_CONTACT_ID, rowId);
                contentValues.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
                contentValues.put(StructuredName.DISPLAY_NAME, contactInfo.displayName);
                contentValues.put(StructuredName.GIVEN_NAME, contactInfo.lastName);
                contentValues.put(StructuredName.FAMILY_NAME, contactInfo.firstName);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
            }

            if (contactInfo.mobileNum.size() > 0) {
                //insert phone
                for (String s : contactInfo.mobileNum) {
                    contentValues.clear();
                    contentValues.put(Data.RAW_CONTACT_ID, rowId);
                    contentValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                    contentValues.put(Phone.NUMBER, s);
                    contentValues.put(Phone.TYPE, Phone.TYPE_MOBILE);
                    context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
                }
            }

            if (contactInfo.homeNum.size() > 0) {
                //insert phone
                for (String s : contactInfo.homeNum) {
                    contentValues.clear();
                    contentValues.put(Data.RAW_CONTACT_ID, rowId);
                    contentValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                    contentValues.put(Phone.NUMBER, s);
                    contentValues.put(Phone.TYPE, Phone.TYPE_MOBILE);
                    context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
                }
            }

            if (contactInfo.Email.size() > 0) {
                //insert phone
                for (String s : contactInfo.Email) {
                    contentValues.clear();
                    contentValues.put(Data.RAW_CONTACT_ID, rowId);
                    contentValues.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
                    contentValues.put(Email.DATA, s);
                    contentValues.put(Email.TYPE, Email.TYPE_HOME);
                    context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
                }
            }

            if (contactInfo.Im.size() > 0) {
                //insert phone
                for (String s : contactInfo.Im) {
                    contentValues.clear();
                    contentValues.put(Data.RAW_CONTACT_ID, rowId);
                    contentValues.put(Data.MIMETYPE, Im.CONTENT_ITEM_TYPE);
                    contentValues.put(Im.DATA, s);
                    contentValues.put(Im.TYPE, Im.TYPE_HOME);
                    context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
                }
            }

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private ArrayList<ContactInfo> handleReadStrings(ArrayList<String> arrayList) {
        ArrayList<ContactInfo> contactArrayList = new ArrayList<ContactInfo>();
        Iterator<String> it = arrayList.iterator();
        m_sHead = (it.next()).split(",");    //得到表头存入 m_sHead

        while (it.hasNext()) {
            ContactInfo contactInfo = new ContactInfo();
            String str = it.next();
            String[] sArr = str.split(",");

            for (int i = 0; i < m_sHead.length; i++) {
                if (m_sHead[i].equals("displayName")) {
                    contactInfo.displayName = sArr[i];
                } else if (m_sHead[i].equals("lastName")) {
                    contactInfo.lastName = sArr[i];
                } else if (m_sHead[i].equals("firstName")) {
                    contactInfo.firstName = sArr[i];
                } else if (m_sHead[i].indexOf("mobileEmail") == -1 && m_sHead[i].indexOf("mobile") != -1) {
                    contactInfo.mobileNum.add(sArr[i]);
                } else if (m_sHead[i].indexOf("Email") != -1) {
                    contactInfo.Email.add(sArr[i]);
                } else if (m_sHead[i].indexOf("Im") != -1) {
                    contactInfo.Im.add(sArr[i]);
                }
            }

            contactArrayList.add(contactInfo);
        }
        return contactArrayList;
    }

    //sPath 是文件的绝对路径
    public ArrayList<String> readFile(String sPath) {
        ArrayList<String> arrList = new ArrayList<String>();
        File file = new File(sPath); //注意：必须提供文件绝对路径
        String sCodeFormat = com.example.OctopusMessage.FileEncodeUtil.getFileEncode(file.getAbsolutePath(), false);

        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, sCodeFormat));

            String strTemp = "";
            //一次读入一行，直到读入null为文件结束
            while ((strTemp = reader.readLine()) != null) {
                strTemp = strTemp.replace("，", ",");    //将所有中文逗号全部替换为英文逗号
                arrList.add(strTemp);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return arrList;
    }

    public int getSuccessCount() {
        return m_iSuccessCount;
    }

    public int getFailCount() {
        return m_iFailCount;
    }
}
