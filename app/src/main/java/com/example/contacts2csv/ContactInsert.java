package com.example.contacts2csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import android.net.Uri;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;

import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
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
    private ArrayList<ContactInfo> m_contactArrayList;

    private void init() {
        m_jsonInsertContact = new JSONObject(new LinkedHashMap());
        m_InsertContactHeader = new ContactHeader();
        m_iSuccessCount = 0;
        m_iFailCount = 0;
    }

    // 从文件读取全部联系人信息，并插入到 database
    public boolean insertContacts(Context context, String sPath) {
        init();
        ArrayList<String> arrList = readFile(sPath);        //从文件读取联系人信息存入arrList
        aryList2json(arrList, m_jsonInsertContact);         // 将 arrayList 转储到 json 中

        Iterator<String> it = m_jsonInsertContact.keys();
        int n = 0;
        while (it.hasNext()) {
            String key = it.next();
            try {
                if (doInsertContact(m_jsonInsertContact.getJSONObject(key), MainActivity.m_MA)) {   //插入一条联系人信息
                    m_iSuccessCount++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    // 插入单个联系人信息到 database。jsonItem 为联系人数据
    private boolean doInsertContact(JSONObject jsonItem, Context context) {
        try {
            Iterator<String> it1 = m_InsertContactHeader.m_jsonHeader.keys();
            long contactId = -1;
            int n = 0;
            while (it1.hasNext()) {
                String key1 = it1.next();   //key1: "jsonG00StructName"、"jsonG01Phone"、...
                JSONObject jsonMime = m_InsertContactHeader.m_jsonHeader.getJSONObject(key1);
                String mime = getMime(jsonMime, "__mimetype_item");
                switch (mime) {
                    //mimetype数据字段                       //存储json变量
                    case StructuredName.CONTENT_ITEM_TYPE:   //jsonG00StructName
                        contactId = addMimeItem00(mime, jsonItem, jsonMime, context);   // 专门处理 jsonG00StructName，用 addMimeItem00() 处理
                        if (-1 == contactId) {
                            return false;   // 若 "displayName"、"lastName"、"firstName" 3个字段的值都为空，则为无名记录、不做处理
                        }
                        break;
                    case Phone.CONTENT_ITEM_TYPE:            //jsonG01Phone
                    case Email.CONTENT_ITEM_TYPE:            //jsonG02Email
                    case Organization.CONTENT_ITEM_TYPE:     //jsonG04OrgSet
                    case Im.CONTENT_ITEM_TYPE:               //jsonG05ImSet
                    case Nickname.CONTENT_ITEM_TYPE:         //jsonG06NickName
                    case Note.CONTENT_ITEM_TYPE:             //jsonG07Note
                    case StructuredPostal.CONTENT_ITEM_TYPE: //jsonG08PostalSet
                  //case GroupMembership.CONTENT_ITEM_TYPE:  //jsonG09GroupMember       //暂未实现
                    case Website.CONTENT_ITEM_TYPE:          //jsonG10WebSet
                    case Event.CONTENT_ITEM_TYPE:            //jsonG11Event
                    case Relation.CONTENT_ITEM_TYPE:         //jsonG12Relation
                    case SipAddress.CONTENT_ITEM_TYPE:       //jsonG13SipAddress
                        addMimeItem01(mime, jsonItem, jsonMime, context, contactId);    // 默认需要处理 xxx.TYPE_CUSTOM，用 addMimeItem01() 处理
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

    // 专门处理 jsonG00StructName，用 addMimeItem00() 处理
    private long addMimeItem00(String mimeItem, JSONObject jsonItem, JSONObject jsonMime, Context context) {
        long contactId = -1;
        ContentValues cv = new ContentValues();
        Uri uri = context.getContentResolver().insert(RawContacts.CONTENT_URI, cv);  // 新建一个新的联系人 Uri
        contactId = ContentUris.parseId(uri);           // 得到新建联系人 Uri 的 contactID

        cv.clear();
        cv.put(Data.RAW_CONTACT_ID, contactId);
        cv.put(Data.MIMETYPE, mimeItem);                //StructuredName.CONTENT_ITEM_TYPE

        int i = 0;
        Iterator<String> it = jsonItem.keys();
        while (it.hasNext()) {
            String key1 = it.next();                    //key1: "__mimetype_x"、"displayName"、"lastName"、...
            String mime = "";
            String val = "";

            String[] arr = findKey(jsonMime, key1);   // 返回数组 {keyNew2, keyHead}。arr[0] = "QqIm"; arr[1] = "other"
            try {   // 数据为空、或者数据不属于 jsonMime 中的类型，便跳过处理，继续循环
                if (TextUtils.isEmpty(jsonItem.getString(key1)) || !jsonMime.has(arr[0])) {
                    continue;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

            try {
                mime = getMime(jsonMime, arr[0]);
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

        if (3 == i) {
            contactId = -1;   // 若 "displayName"、"lastName"、"firstName" 3个字段的值都为空，则为无名记录，便不处理该记录
        } else {
            context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);
        }

        return contactId;
    }

    // 默认需要处理 xxx.TYPE_CUSTOM，用 addMimeItem01() 处理
    private long addMimeItem01(String mimeItem, JSONObject jsonItem, JSONObject jsonMime, Context context, long contactId) {
        ContentValues cv = new ContentValues();

        Iterator<String> it1 = jsonItem.keys();
        while (it1.hasNext()) {
            String key1 = it1.next();       //key1: "__mimetype_x"、"displayName"、"lastName"、...
            String[] arr = findKey(jsonMime, key1);  // 返回数组 {keyNew2, keyHead}。arr[0] = "QqIm"; arr[1] = "other"
            try {   // 数据为空、或者数据不属于 jsonMime 中的类型，便跳过处理，继续循环
                if (TextUtils.isEmpty(jsonItem.getString(key1)) || !jsonMime.has(arr[0])) {
                    continue;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

            String mime = "";
            String val = "";

            //必须每次清空，否则可能会有重复数据
            cv.clear();                                     //   对jsonItem中每条有内容的用户数据处理步骤
            cv.put(Data.RAW_CONTACT_ID, contactId);         //1、contactID
            cv.put(Data.MIMETYPE, mimeItem);                //2、MIMETYPE

            try {
                // 默认需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                if (jsonMime.has("__mimetype_type") || jsonMime.has("__mimetype_data")) {
                    //contentValues.put(Im.PROTOCOL, Im.PROTOCOL_QQ);               //3、是否包含 "__mimetype_protocal"，处理 jsonG05ImSet
                    // 处理 jsonG05ImSet
                    String protocal = getMime(jsonMime, "__mimetype_protocal");
                    if (!TextUtils.isEmpty(protocal)) {  //若存在 "__mimetype_protocal" 字段，便取出 xxx.PROTOCOL(data5) 的值，作为子类型
                        val = getMime(jsonMime, arr[0]);
                        if (!TextUtils.isEmpty(val)) {  // 非空便加入
                            cv.put(protocal, val);
                        }
                        //contentValues.put(Im.TYPE, Im.TYPE_OTHER);                //4、处理数据TYPE。Im数据类型（目前手机中读取出来的值都为3，Im.TYPE_OTHER，自定义类型）
                        mime = getMime(jsonMime, "__mimetype_type");
                        val = getMime(jsonMime, "__mimetype_subtype_" + arr[1]);//"__mimetype_subtype_custom"、"__mimetype_subtype_home"、...
                        if (!TextUtils.isEmpty(val)) {  // 非空便加入
                            cv.put(mime, val);
                        }
                    } else {    //contentValues.put(Phone.TYPE, Phone.TYPE_MOBILE); //4、处理数据TYPE
                        mime = getMime(jsonMime, "__mimetype_type");
                        val = getMime(jsonMime, arr[0]);    //Phone.TYPE_CUSTOM、Phone.TYPE_HOME、...
                        //System.out.println("mime = " + mime + ", val = " + val);
                        if (!TextUtils.isEmpty(val)) {  // 非空便加入
                            cv.put(mime, val);
                        }
                    }

                    //contentValues.put(Im.DATA, data);                             //5、处理用户数据
                    mime = getMime(jsonMime, "__mimetype_data");
                    val = jsonItem.getString(key1);
                    if (!TextUtils.isEmpty(val)) {  // 非空便加入
                        cv.put(mime, val);
                        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);
                    }
                } else {    //无需处理 xxx.TYPE_CUSTOM，用 fun02_dumpJson4layAll() 处理

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return contactId;
    }

    private String getMime(JSONObject json, String key) {
        String mime = "";
        try {
            if (json.has(key)) {
                mime = json.getJSONObject(key).getString("__first").trim();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mime;
    }

    // keyNew: "otherQqIm"、"otherQqIm2"、"otherQqIm3"、...
    // 返回数组 {keyNew2, keyHead}
    // 遍历 jsonMime，若找到 keyNew 末尾含有 key 子串，则返回找到的 key、keyHead 组成的数组; 否则返回 keyNew2、keyHead 组成的数组
    private String[] findKey(JSONObject jsonMime, String keyNew) {
        String keyNew2 = keyNew.replaceAll("\\d+$", "");    // java 正则去掉字符串末尾数字
        String keyHead = "";
        String keyTail = "";
        Iterator<String> it = jsonMime.keys();
        while (it.hasNext()) {
            String key = it.next();                 //key 为："QqIm"、...
            if (keyNew2.length() > key.length()) {  // 去 keyNew2 的头查找法。优化算法
                keyHead = keyNew2.substring(0, keyNew2.length() - key.length());    // 取 keyNew 头部子串
                keyTail = keyNew2.substring(keyNew2.length() - key.length());       // 取 keyNew 末尾子串
                if (keyTail.equals(key)) {
                    keyNew2 = key;
                    break;
                }
            }
        }
        return new String[]{keyNew2.trim(), keyHead.trim()};
    }

    // 将 arrList 转储到 json 中
    private void aryList2json(ArrayList<String> arrList, JSONObject json) {
        String sHead[] = {};
        for (int i = 0; i < arrList.size(); i++) {
            String str = arrList.get(i);
            String[] sArr = str.split(",", -1); //添加后面的参数，保证结尾空字符串不会被丢弃。

            if (0 == i) {
                sHead = sArr;    //得到表头存入 sHead
                continue;
            }

            String contactId = "contactId_" + String.valueOf(i - 1);
            try {
                json.put(contactId, new JSONObject(new LinkedHashMap()));
                for (int j = 0; j < sHead.length; j++) {
                    json.getJSONObject(contactId).put(sHead[j], sArr[j]);
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

    private void logContentValues(ContentValues cv) {
        System.out.println("ContentValues :\n{");
        for (Map.Entry<String, Object> item : cv.valueSet()) {
            System.out.println("\t" + item.getKey().toString() + "," + item.getValue().toString());
        }
        System.out.println("}");
    }
}
