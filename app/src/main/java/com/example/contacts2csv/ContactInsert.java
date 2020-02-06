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

import android.database.Cursor;
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
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import static com.example.contacts2csv.MainActivity.m_Fun;
import static com.example.contacts2csv.MainActivity.m_MA;

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
            ArrayList<String> arrList = readFile(sPath);        //从文件读取联系人信息存入arrList
            //System.out.println("insertContacts" + n++);

            aryList2json(arrList, m_jsonInsertContact);         // 将 arrayList 转储到 json 中

            //m_Fun.logJson(m_jsonInsertContact); // Logcat 输出 JSONObject 完整结构

            Iterator<String> it = m_jsonInsertContact.keys();
            int n = 0;
            while (it.hasNext()) {
                String key = it.next();
                //System.out.println("key_" + (n++) + " = " + key);
                //I/System.out: key_0 = contactId_0
                //I/System.out: key_1 = contactId_1
                //I/System.out: key_2 = contactId_2
                //I/System.out: key_3 = contactId_3

                try {
                    if (doInsertContact(m_jsonInsertContact.getJSONObject(key), MainActivity.m_MA)) {
                        m_iSuccessCount++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        return true;
    }

    // 插入单个联系人信息到 database
    private boolean doInsertContact(JSONObject jsonItem, Context context) {
        try {
            //m_Fun.logJson(jsonItem);  //有联系人数据
            //String mimetype = m_InsertContactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first").trim();
            Iterator<String> it1 = m_InsertContactHeader.m_jsonHeader.keys();
            long contactId = -1;
            int n = 0;
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
                        addMimeItem02(mime, jsonItem, jsonMime, context, contactId);
                        break;
                    case Im.CONTENT_ITEM_TYPE:               //jsonG05ImSet
                        addMimeItem03(mime, jsonItem, jsonMime, context, contactId);
                        break;
                    case Nickname.CONTENT_ITEM_TYPE:         //jsonG06NickName
                    case Note.CONTENT_ITEM_TYPE:             //jsonG07Note
                    case StructuredPostal.CONTENT_ITEM_TYPE: //jsonG08PostalSet
                    //case GroupMembership.CONTENT_ITEM_TYPE:  //jsonG09GroupMember
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
                //System.out.println("contactId_" + (n++) + " = " + contactId);
                //I/System.out: contactId_0 = 605
                //I/System.out: contactId_1 = 605
                //I/System.out: contactId_8 = 605
                //I/System.out: contactId_5 = 607
                //I/System.out: contactId_12 = 607
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private long addMimeItem03(String mimeItem, JSONObject jsonItem, JSONObject jsonMime, Context context, long contactId) {
        ContentValues cv = new ContentValues();

        Iterator<String> it1 = jsonItem.keys();
        while (it1.hasNext()) {
            String key1 = it1.next();       //key1: "__mimetype_x"、"displayName"、"lastName"、...
            //arr[0] = "QqIm"; arr[1] = "other"
            String [] arr = findKey03(jsonMime, key1);  // 返回数组 {keyNew2, keyHead}
            try {
                if(TextUtils.isEmpty(jsonItem.getString(key1)) || !jsonMime.has(arr[0])) {
                    //break;    //若没有该 key 便终止循环，后面的联系人数据便无法导入
                    continue;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

            String mime = "";
            String val = "";
            String mimeData = "";
            String valData = "";

            //必须每次清空，否则可能会有重复数据
            cv.clear();
            cv.put(Data.RAW_CONTACT_ID, contactId);
            cv.put(Data.MIMETYPE, mimeItem);

            //if (contactInfo.Im.size() > 0) {
            //    //insert phone
            //    for (String data : contactInfo.Im) {
            //        contentValues.clear();
            //        contentValues.put(Data.RAW_CONTACT_ID, rowId);
            //        contentValues.put(Data.MIMETYPE, Im.CONTENT_ITEM_TYPE);
            //        contentValues.put(Im.TYPE, Im.TYPE_OTHER);        //Im数据类型（目前手机中读取出来的值都为3，Im.TYPE_OTHER，自定义类型）

            //        //contentValues.put(Im.LABEL, Im.PROTOCOL_QQ);    //Im真正的类型，对应在源码中的Im.PROTOCOL
            //        contentValues.put(Im.PROTOCOL, Im.PROTOCOL_QQ);   //Im真正的类型，对应在源码中的Im.PROTOCOL

            //        contentValues.put(Im.DATA, data);                 //用户填写的Im数据
            //        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
            //    }
            //}

            //{"__mimetype_type", Im.TYPE},                                   //Im.TYPE = "data2";
            //{"__mimetype_protocal", Im.PROTOCOL},                           //Im.PROTOCOL = "data5";
            //{"__mimetype_subtype_other", String.valueOf(Im.TYPE_OTHER)},    //Im.TYPE_WORK = 3;
            //{"QqIm", String.valueOf(Im.PROTOCOL_QQ)},                       //Im.PROTOCOL_QQ = 4;

            //arr[0] = "QqIm"; arr[1] = "other"
            String __mimetype_subtype_ = "__mimetype_subtype_" + arr[1]; //"__mimetype_subtype_custom"、"__mimetype_subtype_home"、...
            try {
                if(jsonMime.has("__mimetype_type") || jsonMime.has("__mimetype_data")) {
                    //contentValues.put(Im.TYPE, Im.TYPE_OTHER);        //Im数据类型（目前手机中读取出来的值都为3，Im.TYPE_OTHER，自定义类型）
                    mime = jsonMime.getJSONObject("__mimetype_type").getString("__first");
                    val = jsonMime.getJSONObject(__mimetype_subtype_).getString("__first");
                    if (!TextUtils.isEmpty(val)) {  // 非空便加入
                        cv.put(mime, val);
                    }

                    //contentValues.put(Im.PROTOCOL, Im.PROTOCOL_QQ);   //Im真正的类型，对应在源码中的Im.PROTOCOL
                    mime = jsonMime.getJSONObject("__mimetype_protocal").getString("__first");
                    val = jsonMime.getJSONObject(arr[0]).getString("__first");
                    if (!TextUtils.isEmpty(val)) {  // 非空便加入
                        cv.put(mime, val);
                    }

                    //contentValues.put(Im.DATA, data);                 //用户填写的Im数据
                    mime = jsonMime.getJSONObject("__mimetype_data").getString("__first");
                    val = jsonItem.getString(key1);
                    if (!TextUtils.isEmpty(val)) {  // 非空便加入
                        cv.put(mime, val);
                        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return contactId;
    }

    // keyNew: "otherQqIm"、"otherQqIm2"、"otherQqIm3"、...
    // 返回数组 {keyNew2, keyHead}
    // 遍历 jsonMime 的 keys ，若找到 keyNew 的末尾含有 key 子串，则返回找到的 key、keyHead 组成的数组; 否则返回 keyNew2、keyHead 组成的数组
    private String [] findKey03(JSONObject jsonMime, String keyNew) {
        String keyNew2 = keyNew.replaceAll("\\d+$",""); // java 正则去掉字符串后面的数字
        String keyHead = "";
        String keyTail = "";
        Iterator<String> it = jsonMime.keys();
        while (it.hasNext()) {
            String key = it.next();        //key 为："QqIm"、...
            // 去 keyNew 的头查找法。优化算法
            if (keyNew2.length() > key.length()) {
                keyHead = keyNew2.substring(0, keyNew2.length() - key.length());   // 取 keyNew 头部子串
                keyTail = keyNew2.substring(keyNew2.length() - key.length());      // 取 keyNew 末尾子串
                if (keyTail.equals(key)) {
                    keyNew2 = key;
                    break;
                }
            }
        }
        return new String[] {keyNew2.trim(), keyHead.trim()};
    }

    private long addMimeItem02(String mimeItem, JSONObject jsonItem, JSONObject jsonMime, Context context, long contactId) {
        ContentValues cv = new ContentValues();

        Iterator<String> it1 = jsonItem.keys();
        while (it1.hasNext()) {
            String key1 = it1.next();       //key1: "__mimetype_x"、"displayName"、"lastName"、...
            String mime = "";
            String val = "";
            String mimeData = "";
            String valData = "";

            //必须每次清空，否则可能会有重复数据
            cv.clear();
            cv.put(Data.RAW_CONTACT_ID, contactId);
            cv.put(Data.MIMETYPE, mimeItem);

            if(!jsonMime.has(findKey(jsonMime, key1))) {
                //break;    //若没有该 key 便终止循环，后面的联系人数据便无法导入
                continue;
            }

            try {
                if(jsonMime.has("__mimetype_type") || jsonMime.has("__mimetype_data")) {
                    mime = jsonMime.getJSONObject("__mimetype_type").getString("__first");
                    val = jsonMime.getJSONObject(findKey(jsonMime, key1)).getString("__first");

                    mimeData = jsonMime.getJSONObject("__mimetype_data").getString("__first");
                    valData = jsonItem.getString(key1);
                } else {
                    mime = jsonMime.getJSONObject(findKey(jsonMime, key1)).getString("__first");
                    val = jsonItem.getString(key1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!TextUtils.isEmpty(valData)) {  // 非空便加入
                cv.put(mimeData, valData);
            }
            if (!TextUtils.isEmpty(val)) {  // 非空便加入
                cv.put(mime, val);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);
            }
        }

        return contactId;
    }

    private void logContentValues(ContentValues cv) {
        System.out.println("ContentValues :\n{");
        for (Map.Entry<String, Object> item : cv.valueSet()) {
            System.out.println("\t" + item.getKey().toString() + "," + item.getValue().toString());
        }
        System.out.println("}");
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

            if (addValues00(jsonItem, jsonMime, cv)) {
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
    private boolean addValues00(JSONObject jsonItem, JSONObject jsonMime, ContentValues cv) {
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

            if(!jsonMime.has(findKey(jsonMime, key1))) {    //若没有该 key 便终止循环
                break;
            }

            try {
                //System.out.println("findKey(jsonMime, key1) = " + findKey(jsonMime, key1));
                //I/System.out: findKey(jsonMime, key1) = mobile3
                //W/System.err: org.json.JSONException: No value for customNum
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

    private long addMimeItemPhone(String mime, JSONObject jsonItem, JSONObject jsonMime, Context context, long contactId) {
        try {
            ContentValues cv = new ContentValues();
            cv.clear();
            cv.put(Data.RAW_CONTACT_ID, contactId);
            cv.put(Data.MIMETYPE, mime);
            cv.put(Phone.NUMBER, jsonItem.getString("mobile"));
            cv.put(Phone.TYPE, Phone.TYPE_MOBILE);
            context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contactId;
    }

    private long addMimeItem01(String mime, JSONObject jsonItem, JSONObject jsonMime, Context context, long contactId) {
        try {
            ContentValues cv = new ContentValues();
            cv.clear();
            //W/System.err: java.lang.IllegalArgumentException: you must set exactly one of
            // GroupMembership.GROUP_SOURCE_ID and GroupMembership.GROUP_ROW_ID
            cv.put(Data.RAW_CONTACT_ID, contactId);
            cv.put(Data.MIMETYPE, mime);

            // cv.put(Data.RAW_CONTACT_ID, contactId);              //已有
            // cv.put(Data.MIMETYPE, mime);                         //已有
            // cv.put(Phone.NUMBER, jsonItem.getString("mobile"));
            // cv.put(Phone.TYPE, Phone.TYPE_MOBILE);               //已有

            if (addValues(jsonItem, jsonMime, cv)) {
                //W/System.err: java.lang.IllegalArgumentException: you must set exactly one of GroupMembership.GROUP_SOURCE_ID and GroupMembership.GROUP_ROW_ID
                //System.out.println("cv :\n" + cv.toString());
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
            String mime = "";
            String val = "";
            String mimeData = "";
            String valData = "";

            if(!jsonMime.has(findKey(jsonMime, key1))) {
                //break;    //若没有该 key 便终止循环，后面的联系人数据便无法导入
                continue;
            }

            try {
                //System.out.println("findKey(jsonMime, key1) = " + findKey(jsonMime, key1));
                //I/System.out: findKey(jsonMime, key1) = mobile3
                //W/System.err: org.json.JSONException: No value for customNum
                //mime = jsonMime.getJSONObject(findKey(jsonMime, key1)).getString("__first");
                //val = jsonItem.getString(key1);

                if(jsonMime.has("__mimetype_type") || jsonMime.has("__mimetype_data")) {
                    //必须加入下面数据，否则无法为联系人添加新的数据
                    // cv.put(Phone.TYPE, Phone.TYPE_MOBILE);
                    //No value for __mimetype_type
                    mime = jsonMime.getJSONObject("__mimetype_type").getString("__first");
                    val = jsonMime.getJSONObject(findKey(jsonMime, key1)).getString("__first");

                    //必须加入下面数据，否则无法为联系人添加新的数据
                    // cv.put(Phone.NUMBER, jsonItem.getString("mobile"));
                    //cv.put(jsonMime.getJSONObject("__mimetype_data").getString("__first"), );
                    mimeData = jsonMime.getJSONObject("__mimetype_data").getString("__first");
                    valData = jsonItem.getString(key1);
                } else {
                    mime = jsonMime.getJSONObject(findKey(jsonMime, key1)).getString("__first");
                    val = jsonItem.getString(key1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!TextUtils.isEmpty(val)) {  // 非空便加入
                cv.put(mime, val);
                cv.put(mimeData, valData);
                //System.out.println("mime = " + mime + "; val = " + val);
            } else if (key1.equals("displayName") || key1.equals("lastName") || key1.equals("firstName")) {
                i++;
            }
        }

        if (3 == i) {   // 若 "displayName"、"lastName"、"firstName" 3个字段的值都为空，则为无名记录，便不处理该记录
            ret = false;
            //System.out.println("ret = " + ret);
        }

        //必须加入下面数据，否则无法为联系人添加新的数据
        // cv.put(Phone.NUMBER, jsonItem.getString("mobile"));
//        if(jsonMime.has("__mimetype_type") || jsonMime.has("__mimetype_data")) {
//            try {
//                System.out.println("jsonMime.getJSONObject(\"__mimetype_type\").getString(\"__first\") = " + jsonMime.getJSONObject("__mimetype_type").getString("__first"));
//                //W/System.err: org.json.JSONException: No value for __mimetype_type
//                cv.put(jsonMime.getJSONObject("__mimetype_type").getString("__first"));
//                cv.put(jsonMime.getJSONObject("__mimetype_data").getString("__first"), );
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }

        return ret;
    }
    //I/System.out: mime = data1; val = Zhang San
    //I/System.out: mime = data2; val = Zhang
    //I/System.out: mime = data3; val = San
    //I/System.out: mime = 2; val = 13389789833
    //I/System.out: mime = 2; val = 1328973983
    //I/System.out: mime = 2; val = 13929789734
    //I/System.out: mime = data1; val = Zhang San2
    //I/System.out: mime = data2; val = Zhang
    //I/System.out: mime = data3; val = San2
    //I/System.out: mime = 2; val = 13389789833
    //I/System.out: mime = 2; val = 1328973983
    //I/System.out: mime = 2; val = 13929789734
    //I/System.out: mime = 1; val = em1@qq.com
    //I/System.out: mime = 1; val = em2@qq.com
    //I/System.out: mime = 4; val = 435435
    //I/System.out: mime = data1; val = Li Si
    //I/System.out: mime = data2; val = Li
    //I/System.out: mime = data3; val = Si
    //I/System.out: mime = 2; val = 13287935454
    //I/System.out: mime = 2; val = 13244879878
    //I/System.out: mime = data1; val = Wang Wu
    //I/System.out: mime = data2; val = Wang
    //I/System.out: mime = data3; val = Wu
    //I/System.out: mime = 2; val = 13998789745

    // key1 为：contact592、contact593、...
    // keyNew: "otherQqIm"、"otherQqIm2"、"otherQqIm3"、...
    // 遍历 jsonMime 的 keys ，若找到 keyNew 的末尾含有 key 子串，则返回找到的 key; 否则返回 keyNew
    private String findKey(JSONObject jsonMime, String keyNew) {
        //I/System.out: findKey(jsonMime, key1) = mobile3
        String keyNew2 = keyNew.replaceAll("\\d+$",""); // java 正则去掉字符串后面的数字

        //System.out.println("findKey(jsonMime, key1) = " + findKey(jsonMime, key1));
        //I/System.out: findKey(jsonMime, key1) = mobile3

        //关于正则去掉字符串后面的数字
        //str.replaceAll("\\d+$","");
        //抓住关键就可以了。末尾=>$，数字=>d。
        //拆分关键字照着正则表查元字符，拼一个正则。虽然这样可能得出的不是最优解，但是不失为一种解决问题的方法。
        //'6月7号666'.replace(/\d+$/,''); //"6月7号"
        //'1号房间777'.replace(/\d+$/,''); //"1号房间"
        //'6月7号999r'.replace(/\d+$/,''); //"6月7号999r"

        Iterator<String> it = jsonMime.keys();
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
