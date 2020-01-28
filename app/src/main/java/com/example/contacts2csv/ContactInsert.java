package com.example.contacts2csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.util.Pair;

public class ContactInsert {
    private final String m_sTAG = "ContactInsert";
    private int m_iSuccessCount = 0;
    private int m_iFailCount = 0;
    private boolean m_bGbk = false;

    public boolean insertContacts(Context context, String sPath, String sCharset) {
        init(sCharset);
        try {
            ArrayList<String> arrList = readFile(sPath);
            if (arrList == null) {
                Log.e(m_sTAG, "Error in insertContacts arrayList == null");
                return false;
            }
            Iterator<String> it = arrList.iterator();
            it.hasNext();   //跳过表头行
            while (it.hasNext()) {
                String sContact = it.next();
                if (doInsertToContact(context, sContact)) {
                    m_iSuccessCount++;
                }
            }
        } catch (Exception e) {
            Log.e(m_sTAG, "Error in insertContacts result : " + e.getMessage());
        }
        return true;
    }

    /**
     * insert into database
     */
    private boolean doInsertToContact(Context context, String sContact) {
        String sArr[] = sContact.split(",");
        // 1.判断是否为空
        Log.d(m_sTAG, "in doInsertToContact contactInfo = null? " + (sArr == null));


        for (int i = 0; i < sArr.length; i ++) {

        }


        ContactInfo contactInfo = null;
        // 1.判断是否为空
        Log.d(m_sTAG, "in doInsertToContact contactInfo = null? " + (contactInfo == null));
        try {
            ContentValues contentValues = new ContentValues();
            Uri uri = context.getContentResolver().insert(RawContacts.CONTENT_URI, contentValues);
            long rowId = ContentUris.parseId(uri);

            String sName = contactInfo.getDisplayName();
            String sMobileNum = contactInfo.getMobileNum();
            String sHomeNum = contactInfo.getHomeNum();

            //insert sName
            if (sName != null) {
                contentValues.clear();
                contentValues.put(Data.RAW_CONTACT_ID, rowId);
                contentValues.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
                int iIndex = sName.length() / 2;
                String sDisplayName = sName;
                //check sName if english sName
                String sGivenName = null;
                String sFamilyName = null;
                if (checkEnglishName(sDisplayName) == false) {
                    sGivenName = sName.substring(iIndex);
                    sFamilyName = sName.substring(0, iIndex);
                } else {
                    sGivenName = sFamilyName = sDisplayName;
                }
                contentValues.put(StructuredName.DISPLAY_NAME, sDisplayName);
                contentValues.put(StructuredName.GIVEN_NAME, sGivenName);
                contentValues.put(StructuredName.FAMILY_NAME, sFamilyName);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
            }

            if (sMobileNum != null) {
                //insert phone
                contentValues.clear();
                contentValues.put(Data.RAW_CONTACT_ID, rowId);
                contentValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                contentValues.put(Phone.NUMBER, sMobileNum);
                contentValues.put(Phone.TYPE, Phone.TYPE_MOBILE);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
            }

            if (sHomeNum != null) {
                //insert houseNum
                contentValues.clear();
                contentValues.put(Data.RAW_CONTACT_ID, rowId);
                contentValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                contentValues.put(Phone.NUMBER, sHomeNum);
                contentValues.put(Phone.TYPE, Phone.TYPE_HOME);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void init(String charset) {
        m_iSuccessCount = 0;
        m_iFailCount = 0;
        m_bGbk = charset.equals(ExtraStrings.CHARSET_GBK);
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

    /**
     * read txt file
     */
    private ArrayList<ContactInfo> readFile0(String sPath) {
        //read from file
        ArrayList<String> sArrList = doReadFile(sPath);
        if (sArrList == null) {
            Log.e(m_sTAG, "Error in readFile sArrList == null");
            return null;
        }
        ArrayList<ContactInfo> arrListContactInfo = readArrList(sArrList);
        return arrListContactInfo;
    }

    private ArrayList<String> doReadFile(String sPath) {
        FileInputStream inStream = null;
        ArrayList<String> arrList = new ArrayList<String>();
        try {
            byte[] byteArr = new byte[ExtraStrings.BUFFER_SIZE];
            inStream = new FileInputStream(sPath);
            while (inStream.read(byteArr) != -1) {
                int iLen = 0;
                int iFirst = iLen;
                for (int i = 0; i < byteArr.length; i++) {
                    if (byteArr[i] == ExtraStrings.ENTER_CHAR_LINUX) {
                        iLen = i;
                        byte[] nowBytes = new byte[iLen - iFirst];
                        System.arraycopy(byteArr, iFirst, nowBytes, 0, iLen - iFirst);
                        if (m_bGbk) {
                            arrList.add(new String(nowBytes, ExtraStrings.CHARSET_GBK).trim());
                        } else {
                            arrList.add(new String(nowBytes, ExtraStrings.CHARSET_UTF8).trim());
                        }
                        iFirst = i + 1;
                    }
                }

            }
        } catch (Exception e1) {
            return null;
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e1) {
                    return null;
                }
            }
        }
        return arrList;
    }

    private ArrayList<ContactInfo> readArrList(ArrayList<String> arrList) {
        ArrayList<ContactInfo> arrListContactInfo = new ArrayList<ContactInfo>();
        Iterator<String> it = arrList.iterator();
        while (it.hasNext()) {
            String sInfo = it.next();
            String[] sArrInfo = sInfo.split(ExtraStrings.SPACE_REGULAR);
            String sDisplayName = null;
            String sMobileNum = null;
            String sHomeNum = null;
            switch (sArrInfo.length) {
                case 0:
                    //do nothing
                    continue;
                case 1:
                    sDisplayName = sArrInfo[0];
                    break;
                case 2:
                    sDisplayName = sArrInfo[0];
                    if (sArrInfo[1].length() == ExtraStrings.MOBILE_NUM_LENGTH) {
                        sMobileNum = sArrInfo[1];
                    } else {
                        sHomeNum = sArrInfo[1];
                    }
                    break;
                default:
                    //length >= 3
                    sDisplayName = sArrInfo[0];
                    sMobileNum = sArrInfo[1];
                    sHomeNum = sArrInfo[2];
            }
            //check sDisplayName sMobileNum and sHomeNum
            if (sDisplayName == null || sDisplayName.matches(ExtraStrings.NUM_REGULAR)) {
                Log.e(m_sTAG, "Error in readArrList sDisplayName == null");
                m_iFailCount++;
                continue;
            }
            if (sMobileNum != null && (sMobileNum.length() != ExtraStrings.MOBILE_NUM_LENGTH
                    || !sMobileNum.matches(ExtraStrings.NUM_REGULAR))) {
                Log.e(m_sTAG, "Error in readArrList sMobileNum is not all num or sMobileNum == null");
                m_iFailCount++;
                continue;
            }
            if (sHomeNum != null && !(sHomeNum.matches(ExtraStrings.HOME_REGULAR_01) ||
                    sHomeNum.matches(ExtraStrings.HOME_REGULAR_02))) {
                Log.e(m_sTAG, "Error in readArrList sHomeNum is not all num");
                m_iFailCount++;
                continue;
            }
            arrListContactInfo.add(new ContactInfo(sDisplayName, sMobileNum, sHomeNum));
        }
        return arrListContactInfo;
    }

    /**
     * insert into database
     */
    private boolean doInsertToContact0(Context context, ContactInfo contactInfo) {
        // 1.判断是否为空
        Log.d(m_sTAG, "in doInsertToContact contactInfo = null? " + (contactInfo == null));
        try {
            ContentValues contentValues = new ContentValues();
            Uri uri = context.getContentResolver().insert(RawContacts.CONTENT_URI, contentValues);
            long rowId = ContentUris.parseId(uri);

            String sName = contactInfo.getDisplayName();
            String sMobileNum = contactInfo.getMobileNum();
            String sHomeNum = contactInfo.getHomeNum();

            //insert sName
            if (sName != null) {
                contentValues.clear();
                contentValues.put(Data.RAW_CONTACT_ID, rowId);
                contentValues.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
                int iIndex = sName.length() / 2;
                String sDisplayName = sName;
                //check sName if english sName
                String sGivenName = null;
                String sFamilyName = null;
                if (checkEnglishName(sDisplayName) == false) {
                    sGivenName = sName.substring(iIndex);
                    sFamilyName = sName.substring(0, iIndex);
                } else {
                    sGivenName = sFamilyName = sDisplayName;
                }
                contentValues.put(StructuredName.DISPLAY_NAME, sDisplayName);
                contentValues.put(StructuredName.GIVEN_NAME, sGivenName);
                contentValues.put(StructuredName.FAMILY_NAME, sFamilyName);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
            }

            if (sMobileNum != null) {
                //insert phone
                contentValues.clear();
                contentValues.put(Data.RAW_CONTACT_ID, rowId);
                contentValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                contentValues.put(Phone.NUMBER, sMobileNum);
                contentValues.put(Phone.TYPE, Phone.TYPE_MOBILE);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
            }

            if (sHomeNum != null) {
                //insert houseNum
                contentValues.clear();
                contentValues.put(Data.RAW_CONTACT_ID, rowId);
                contentValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                contentValues.put(Phone.NUMBER, sHomeNum);
                contentValues.put(Phone.TYPE, Phone.TYPE_HOME);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean checkEnglishName(String sName) {
        char[] charsName = sName.toCharArray();
        for (int i = 0; i < charsName.length; i++) {
            if ((charsName[i] >= 'a' && charsName[i] <= 'z') ||
                    (charsName[i] >= 'A' && charsName[i] <= 'Z')) {
                continue;
            }
            return false;
        }
        return true;
    }

    public int getSuccessCount() {
        return m_iSuccessCount;
    }

    public int getFailCount() {
        return m_iFailCount;
    }
}
