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
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;



import org.json.JSONException;
import org.json.JSONObject;

public class ContactInsert {
    private JSONObject m_jsonInsertContact;        //用于存放获取的所有记录数据
    private ContactHeader m_InsertContactHeader;         //用于存放通讯录所有记录的表头信息
    private final String m_sTAG = getClass().getSimpleName();
    private int m_iSuccessCount = 0;
    private int m_iFailCount = 0;
    private String m_sHead[];
    private ArrayList<ContactInfo> m_contactArrayList;

    private void init() {
        m_jsonInsertContact = new JSONObject(new LinkedHashMap());
        m_InsertContactHeader = new ContactHeader();
        m_iSuccessCount = 0;
        m_iFailCount = 0;
    }

    //E/ContactInsert: Error in insertContacts result : length=13; index=30
    public boolean insertContacts(Context context, String sPath) {
        init();
        //int n = 1;
        //System.out.println("insertContacts" + n++);
        try {
            ArrayList<String> arrList = readFile(sPath);        //从文件读取联系人信息存入arrList
            //System.out.println("insertContacts" + n++);

            aryList2json(arrList, m_jsonInsertContact);         // 将 arrayList 转储到 json 中

            Iterator<String> it = m_jsonInsertContact.keys();
            while (it.hasNext()) {
                String key = it.next();
                if (doInsertContact(m_jsonInsertContact.getJSONObject(key), MainActivity.m_MA)) {
                    m_iSuccessCount++;
                }

            }

        } catch (Exception e) {
            Log.e(m_sTAG, "Error in insertContacts result : " + e.getMessage());
        }
        return true;
    }

    // 插入单个联系人信息到 database
    private boolean doInsertContact(JSONObject jsonItem, Context context) {
        try {
            //String mimetype = m_InsertContactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first").trim();
            Iterator<String> it1 = m_InsertContactHeader.m_jsonHeader.keys();
            while (it1.hasNext()) {
                String key1 = it1.next();       //key1: "jsonG00StructName"、"jsonG01Phone"、...
                Iterator<String> it2 = m_InsertContactHeader.m_jsonHeader.getJSONObject(key1).keys();

                //while (it2.hasNext()) {
                //String key2 = it2.next();   //key2: "__mimetype_x"、"displayName"、"lastName"、...
                //Iterator<String> it3 = m_InsertContactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).keys();
                //String key3 = it3.next();   //4层结构key3: "__first"、"__second"；5层结构key3：不会是这些值
                //处理第一类型有4层结构，mJsonG00到mJsonG03、mJsonG05到mJsonG07、mJsonG09、mJsonG11到mJsonG13

                JSONObject jsonMime = m_InsertContactHeader.m_jsonHeader.getJSONObject(key1);
                String mime = jsonMime.getJSONObject("__mimetype_item").getString("__first").trim();
                long contactId = -1;
                switch (mime) {
                    //mimetype数据字段                       //存储json变量
                    case StructuredName.CONTENT_ITEM_TYPE:   //jsonG00StructName
                        contactId = addMimeItem00(mime, jsonItem, jsonMime, context);
                        if (-1 == contactId){
                            return false; // 若 "displayName"、"lastName"、"firstName" 3个字段的值都为空，则为无名记录，便不处理该记录
                        }
                        break;
                    case Phone.CONTENT_ITEM_TYPE:            //jsonG01Phone
                    case Email.CONTENT_ITEM_TYPE:            //jsonG02Email
                    case Organization.CONTENT_ITEM_TYPE:     //jsonG04OrgSet
                    case Im.CONTENT_ITEM_TYPE:               //jsonG05ImSet
                    case Nickname.CONTENT_ITEM_TYPE:         //jsonG06NickName
                    case Note.CONTENT_ITEM_TYPE:             //jsonG07Note
                    case StructuredPostal.CONTENT_ITEM_TYPE: //jsonG08PostalSet
                    case GroupMembership.CONTENT_ITEM_TYPE:  //jsonG09GroupMember
                    case Website.CONTENT_ITEM_TYPE:          //jsonG10WebSet
                    case Event.CONTENT_ITEM_TYPE:            //jsonG11Event
                    case Relation.CONTENT_ITEM_TYPE:         //jsonG12Relation
                    case SipAddress.CONTENT_ITEM_TYPE:       //jsonG13SipAddress
                        addMimeItem01(mime, jsonItem, jsonMime, context, contactId);
                        break;
                    case Photo.CONTENT_ITEM_TYPE:            //jsonG03Photo
                        break;
                    default:
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private long addMimeItem00(String mime, JSONObject jsonItem, JSONObject jsonMime, Context context) {
        long contactId = -1;
        try {
            ContentValues cv = new ContentValues();
            Uri uri = context.getContentResolver().insert(RawContacts.CONTENT_URI, cv);  //生成一个新的联系人Uri
            contactId = ContentUris.parseId(uri);  //得到新生成的联系人Uri的 contactID

            cv.clear();
            cv.put(Data.RAW_CONTACT_ID, contactId);
            cv.put(Data.MIMETYPE, mime);             //StructuredName.CONTENT_ITEM_TYPE

            if (addValues(jsonItem, jsonMime, cv)) {
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);
            } else {
                contactId = -1;   // 若 "displayName"、"lastName"、"firstName" 3个字段的值都为空，则为无名记录，便不处理该记录
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contactId;
    }

    private long addMimeItem01(String mime, JSONObject jsonItem, JSONObject jsonMime, Context context, long contactId) {
        try {
            ContentValues cv = new ContentValues();
            cv.clear();
            cv.put(Data.RAW_CONTACT_ID, contactId);
            cv.put(Data.MIMETYPE, mime);

            if (addValues(jsonItem, jsonMime, cv)) {
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);
            } else {
                contactId = -1;   // 若 "displayName"、"lastName"、"firstName" 3个字段的值都为空，则为无名记录，便不处理该记录
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contactId;
    }

    // 以 key1 为索引字段，将 jsonMime 中的 MimeType 值和 jsonItem 中的用户数据，写入到 cv 中
    private boolean addValues(JSONObject jsonItem, JSONObject jsonMime, ContentValues cv) {
        boolean ret = true;
        int i = 0;

        Iterator<String> it1 = jsonItem.keys();
        while (it1.hasNext()) {
            String key1 = it1.next();       //key1: "__mimetype_x"、"displayName"、"lastName"、...
            // 跳过前面的元素 "__mimetype_xxx"
            if (key1.length() > "__mimetype_".length() && key1.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                continue;
            }

            String mime = "";
            String val = "";
            try {
                mime = jsonMime.getJSONObject(findKey(jsonMime, key1)).getString("__first");
                val = jsonItem.getString(key1);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!TextUtils.isEmpty(val)) {  // 非空便加入
                cv.put(mime, val);
            } else if (key1.equals("displayName") || key1.equals("lastName") || key1.equals("firstName")) {
                i++;
            }
        }

        if (3 == i) {   // 若 "displayName"、"lastName"、"firstName" 3个字段的值都为空，则为无名记录，便不处理该记录
            ret = false;
        }

        return ret;
    }

    // key1 为：contact592、contact593、...
    // keyNew: "otherQqIm"、"otherQqIm2"、"otherQqIm3"、...
    // 遍历 jsonSource 的 keys ，若找到 keyNew 的末尾含有 key 子串，则返回找到的 keyNew2 = key; 否则返回 keyNew2 = keyNew
    private String findKey(JSONObject jsonSource, String keyNew) {
        String keyNew2 = keyNew;

        Iterator<String> it = jsonSource.keys();
        while (it.hasNext()) {
            String key = it.next();        //key 为："QqIm"、"QqIm2"、"QqIm3"、...
            // 去 keyNew 的头查找法。优化算法
            if (keyNew.length() > key.length()) {
                String keyTemp = keyNew.substring(keyNew.length() - key.length());   // 取 keyNew 末尾子串
                if (keyTemp.equals(key)) {
                    keyNew2 = key;
                    break;
                }
            }
        }
        return keyNew2;
    }

    // 将 arrList 转储到 json 中
    private void aryList2json(ArrayList<String> arrList, JSONObject json) {
        for (int i = 0; i < arrList.size(); i++) {
            String str = arrList.get(i);
            String[] sArr = str.split(",", -1); //添加后面的参数，保证结尾空字符串不会被丢弃。

            if (0 == i) {
                m_sHead = sArr;    //得到表头存入 m_sHead
                continue;
            }

            String contactId = "contactId_" + String.valueOf(i - 1);
            try {
                json.put(contactId, new JSONObject(new LinkedHashMap()));
                for (int j = 0; j < m_sHead.length; j++) {
                    json.getJSONObject(contactId).put(m_sHead[j], sArr[j]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
                //System.out.println(strTemp);
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
