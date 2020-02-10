package com.example.contacts2csv;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.provider.Contacts;
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
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;


import org.json.JSONException;
import org.json.JSONObject;

import static com.example.contacts2csv.MainActivity.m_Fun;
import static com.example.contacts2csv.MainActivity.m_MA;
import static com.example.contacts2csv.MainActivity.m_sPathDownloads;

public class ContactInsert {
    private JSONObject m_jsonInsertContact;         //用于存放获取的所有记录数据
    private ContactHeader m_InsertContactHeader;    //用于存放通讯录所有记录的表头信息
    private GroupInsert m_GroupInsert;              //用于处理导入联系人群组
    private final String m_sTAG = getClass().getSimpleName();
    private int m_iSuccessCount = 0;
    private int m_iFailCount = 0;
    private ArrayList<ContactInfo> m_contactArrayList;

    private void init() {
        m_jsonInsertContact = new JSONObject(new LinkedHashMap());
        m_InsertContactHeader = new ContactHeader();
        m_GroupInsert = new GroupInsert();
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
                if (doInsertContact(m_jsonInsertContact.getJSONObject(key), m_MA)) {   //插入一条联系人信息
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
                        contactId = fun00_addMimeItem(mime, jsonItem, jsonMime, context);   // 专门处理 jsonG00StructName，用 fun00_addMimeItem() 处理
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
                    case Website.CONTENT_ITEM_TYPE:          //jsonG10WebSet
                    case Event.CONTENT_ITEM_TYPE:            //jsonG11Event
                    case Relation.CONTENT_ITEM_TYPE:         //jsonG12Relation
                    case SipAddress.CONTENT_ITEM_TYPE:       //jsonG13SipAddress
                        fun01_addMimeItem(mime, jsonItem, jsonMime, context, contactId);    // 默认需要处理 xxx.TYPE_CUSTOM，用 fun01_addMimeItem() 处理
                        break;
                    case GroupMembership.CONTENT_ITEM_TYPE:  //jsonG09GroupMember           //AS3.5的AVD无分组功能，只能用实体手机测试太麻烦，暂未实现
                        fun02_addMimeGroup(mime, jsonItem, jsonMime, context, contactId);   // 默认需要处理 xxx.TYPE_CUSTOM，用 fun01_addMimeItem() 处理
                        break;
                    case Photo.CONTENT_ITEM_TYPE:            //jsonG03Photo
                        fun03_addMimePhoto(mime, jsonItem, jsonMime, context, contactId);   // 专门处理 jsonG03Photo，用 fun03_addMimePhoto() 处理
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 处理导入头像 Begin

    // 专门处理 jsonG09GroupMember，用 fun02_addMimeGroup() 处理
    private long fun02_addMimeGroup(String mimeItem, JSONObject jsonItem, JSONObject jsonMime, Context context, long contactId) {
        String groupTitle = "";  // 群组名称

        Iterator<String> it = jsonItem.keys();
        while (it.hasNext()) {
            String key = it.next();       //key: "displayName"、"lastName"、...、"groupTitle"、"groupTitle2"、"groupTitle3"、...
            String[] arr = findKey(jsonMime, key);      // 返回数组 {keyNew2, keyHead}。arr[0] = "QqIm"; arr[1] = "other"。注意 arr[0] 已经去除末尾数字
            try {   // 数据为空、或者数据不属于 jsonMime 中的类型，便跳过处理，继续循环
                groupTitle = jsonItem.getString(key);   // 从 jsonItem 的 key ="groupTitleDDD" 中取群组名称
                //System.out.println("groupTitle = " + groupTitle);
                //if (TextUtils.isEmpty(groupTitle) || !jsonMime.has(arr[0])) {
                if (TextUtils.isEmpty(groupTitle) || !"groupTitle".equals(arr[0])) {     // 只处理 "groupTitle"
                    continue;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            //System.out.println("arr[0] = " + arr[0]);

            groupTitle = groupTitle.replace(" ", "_");                 // 空格全部替换为"_"
            m_GroupInsert.addContactToGroup(String.valueOf(contactId), groupTitle, m_MA);   // android将联系人加入群组
            System.out.println("createGroup Success, " + "groupTitle : " + groupTitle);
        }

        return contactId;
    }

    // 专门处理 jsonG03Photo，用 fun03_addMimePhoto() 处理
    private long fun03_addMimePhoto(String mimeItem, JSONObject jsonItem, JSONObject jsonMime, Context context, long contactId) {
        String photoFilePure = "";  // 头像文件名，无后缀
        try {
            photoFilePure = jsonItem.getString("displayName");  // 从 jsonItem 的 key ="displayName" 中取头像文件名
            if (TextUtils.isEmpty(photoFilePure)) {
                photoFilePure = jsonItem.getString("photo");    // 若为空，则从 jsonItem 的 key ="photo" 中取头像文件名
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        photoFilePure = photoFilePure.replace(" ", "_");  // 文件名空格全部替换为"_"
        String photoFile = getPhotoPath(m_sPathDownloads, photoFilePure);// 获得头像文件绝对路径。filepath 为文件路径，filename 为无后缀文件名
        //System.out.println("photoFile = " + photoFile);
        //I/System.out: photoFile = /storage/emulated/0/Android/data/com.example.contacts2csv/files/Download/Photo/Wang_Wu_9.png
        ///sdcard/                                      Android/data/com.example.contacts2csv/files/Download/Photo/Wang_Wu_9.png

        FileInputStream fs = null;
        Bitmap bmpPhoto = null;
        if (!TextUtils.isEmpty(photoFile)) {
            try {
                fs = new FileInputStream(photoFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (null == fs) {
                return -1;
            }
            bmpPhoto = BitmapFactory.decodeStream(fs);
        } else {
            return -1;
        }

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        bmpPhoto.compress(Bitmap.CompressFormat.PNG, 100, os);  // 将Bitmap压缩成PNG编码，质量为100%存储
        byte[] avatarPhoto = os.toByteArray();

        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put(Data.RAW_CONTACT_ID, contactId);
        cv.put(Data.MIMETYPE, mimeItem);                        //Photo.CONTENT_ITEM_TYPE
        cv.put(getMime(jsonMime, "photo"), avatarPhoto);  //cv.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
        //System.out.println("photoFile = " + photoFile);         //cv.put(Photo.PHOTO, avatarPhoto);
        //context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, cv, null, null);   //已有头像才能用 update 更新
        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);                 //没有头像只能用 insert 插入

        return contactId;
    }

    // 获得头像文件绝对路径。filepath 为文件路径，filename 为无后缀文件名
    private String getPhotoPath(String filepath, String filenamePure) {
        String photoPath = "";

        // 查找最新图片文件名。filePath 为目录绝对路径，filenamePure 不含后缀的文件名称
        // iFlag：0，不重名的新文件名称；iFlag：1，最新文件 Wang_Wu_19 名称
        String filename = m_Fun.GetNewPhotoFileName(filepath + "/Photo", filenamePure, 1);

        File filePhoto = new File(filepath + "/Photo", filename);
        if (filePhoto.exists()) {
            photoPath = filePhoto.getAbsolutePath();
        }

        //System.out.println("photoPath = " + photoPath);
        //I/System.out: photoPath = /storage/emulated/0/Android/data/com.example.contacts2csv/files/Download/Photo/Wang_Wu_9.png
        return photoPath;
    }

    // 处理导入头像 Eng
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //java.lang.SecurityException: Permission Denial: writing com.android.providers.contacts.ContactsProvider2
    // uri content://com.android.contacts/raw_contacts from pid=5503, uid=10137
    // requires android.permission.WRITE_CONTACTS, or grantUriPermission()
    // 专门处理 jsonG00StructName，用 fun00_addMimeItem() 处理
    private long fun00_addMimeItem(String mimeItem, JSONObject jsonItem, JSONObject jsonMime, Context context) {
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

    // 默认需要处理 xxx.TYPE_CUSTOM，用 fun01_addMimeItem() 处理
    private long fun01_addMimeItem(String mimeItem, JSONObject jsonItem, JSONObject jsonMime, Context context, long contactId) {
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
    // 返回数组 {keyNew2, keyHead}，注意 keyNew2 已经去除末尾数字
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

    // keyOld: "groupTitle"
    // 返回数组 {keyNew2, keyHead}
    // 遍历 jsonItem，若找到 key 去除末尾数字后等于 keyOld，则返回找到的 key 对应的 value 值
    private String getStringJson2(JSONObject jsonItem, String keyOld) {
        String value = "";
        Iterator<String> it = jsonItem.keys();
        while (it.hasNext()) {
            String key = it.next().trim();                 //key 为："groupTitle"、"groupTitle2"、"groupTitle3"、...
            String key2 = key.replaceAll("\\d+$", "");    // java 正则去掉字符串末尾数字
            if (key2.equals(keyOld.trim())) {
                try {
                    System.out.println("groupTitle key : " + key);
                    value = jsonItem.getString(key);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return value;
    }

    // keyOld: "groupTitle"
    // 返回数组 {keyNew2, keyHead}
    // 遍历 jsonItem，若找到 key 去除末尾数字后等于 keyOld，则返回找到的 key
    private String findKey2(JSONObject jsonItem, String keyOld) {
        String keyRet = "";
        Iterator<String> it = jsonItem.keys();
        while (it.hasNext()) {
            String key = it.next().trim();                 //key 为："groupTitle"、"groupTitle2"、"groupTitle3"、...
            String key2 = key.replaceAll("\\d+$", "");    // java 正则去掉字符串末尾数字
            if (key2.equals(keyOld.trim())) {
                keyRet = key;
                break;
            }
        }
        return keyRet;
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
