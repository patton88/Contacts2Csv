//获取通讯录所有字段
//2012年01月02日 14:48:01 yemh111 阅读数：1368
package com.example.contacts2csv;
// 编译error: unmappable character for encoding UTF-8。
// 解决办法-OK，用文本编辑器打开文件，将文件另存为UTF-8格式
// 有警告，没关系：uses unchecked or unsafe operations. Recompile with -Xlint:unchecked for details.

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Groups;

import static com.example.contacts2csv.MainActivity.m_Fun;
import static com.example.contacts2csv.MainActivity.m_MA;
import static com.example.contacts2csv.MainActivity.m_sPathDownloads;

//android中获取包名、类名。转载weixin_34405925 最后发布于2018-06-27 11:10:00 阅读数 1005  收藏
//LogUtil.i("getPackageName()=" + getPackageName()); //Context类
//LogUtil.i("getClass().getName()=" + getClass().getName());//Class类
//LogUtil.i("getClass().getSimpleName()=" + getClass().getSimpleName());//Class类
//
//控制台打印结果：
//(L:175): getPackageName()=com.xx.xx
//(L:176): getClass().getName()=com.xx.xx.controls.activitys.MainActivity
//(L:177): getClass().getSimpleName()=MainActivity

public class ContactOutput {
    private GroupOutput m_GroupOutput;             //处理导出群组信息

    private JSONObject m_jsonContactData;            //用于存放获取的所有记录中间数据
    private JSONObject m_jsonContactData2;           //用于存放获取的所有记录最终结果
    private ContactHeader m_contactHeader;         //用于存放通讯录所有记录的表头信息
    private ContactHeader m_contactHeaderCount;    //用于存放获取的每条记录每一列的计数器

    public int getSum() {
        return m_jsonContactData2.length();
    }

    public ContactOutput() {
        m_GroupOutput = new GroupOutput();
        m_contactHeader = new ContactHeader();
        //        try { // 实现 Logcat 输出 m_contactHeader 完整结构
        //            System.out.println("m_contactHeader.m_contactHeader : \n" + m_contactHeader.m_jsonHeader.toString(4));
        //        } catch (JSONException e) {
        //            e.printStackTrace();
        //        }

        //混乱无用
        //System.out.println("m_contactHeader.m_lhmapFields : \n" + m_contactHeader.m_lhmapFields.toString());
        //2020-01-28 15:03:43.698 8739-8739/com.example.contacts2csv I/System.out: m_contactHeader.m_lhmapFields :
        //2020-01-28 15:03:43.698 8739-8739/com.example.contacts2csv I/System.out: {displayName=data1, lastName=data2, firstName=data3, prefix=data4, middleName=data5, suffix=data6, phoneticLastName=data7, phoneticFirstName=data9, phoneticMiddleName=data8, telNum=data1, mobile=data1, workNum=data1, workFax=data1, homeFax=data1, pager=data1, otherNum=data1, callbackNum=data1, carNum=data1, compMainTel=data1, isdn=data1, mainTel=data1, otherFax=data1, wirelessDev=data1, telegram=data1, tty_tdd=data1, workMobile=data1, workPager=data1, assistantNum=data1, mms=data1, homeEmail=data1, workEmail=data1, otherEmail=data1, mobileEmail=data1, anniversary=data1, otherday=data1, birthday=data1, homeMsg=data1, workMsg=data1, otherMsg=data1, customIm=data1, aimIm=data1, msnIm=data1, yahooIm=data1, skypeIm=data1, qqIm=data1, googleTalkIm=data1, icqIm=data1, jabberIm=data1, netmeetingIm=data1, remark=data1, defaultNickName=data1, otherNickName=data1, maindenNickName=data1, shortNickName=data1, initialsNickName=data1, workCompany=data1, workJobTitle=data4, workDepartment=data5, workJobDescription=data5, workSymbol=data5, workPhoneticName=data5, workOfficeLocation=data5, otherCompany=data1, otherJobTitle=data4, otherDepartment=data5, otherJobDescription=data5, otherSymbol=data5, otherPhoneticName=data5, otherOfficeLocation=data5, homepage=data1, blog=data1, profile=data1, home=data1, workPage=data1, ftpPage=data1, otherPage=data1, workFormattedAddress=data1, workStreet=data4, workBox=data5, workArea=data6, workCity=data7, workState=data8, workZip=data9, workCountry=data10, homeFormattedAddress=data1, homeStreet=data4, homeBox=data5, homeArea=data6, homeCity=data7, homeState=data8, homeZip=data9, homeCountry=data10, otherFormattedAddress=data1, otherStreet=data4, otherBox=data5, otherArea=data6, otherCity=data7, otherState=data8, otherZip=data9, otherCountry=data10}
    }

    //导出导入联系人时，应该先处理组信息
    //导出联系人时，先导出组信息到 Groups_xxx.txt，不用管该组有多少组成员；然后再导出全部联系人，包含联系人属于哪些组的信息
    //导入联系人时，先导入组信息，不用管该组有多少组成员；然后再导入联系人，导入联系人时根据属于哪些组的信息，将该联系人加入这些组即可
    public boolean outputAllContacts(Context context, String sPath) {
        // 1、先导出组信息到 Groups_xxx.txt，不用管该组有多少组成员
        //m_GroupOutput.getAllGroupInfo(m_MA);
        String sGroups = m_GroupOutput.getContactsGroups2();    //显示分组信息测试
        m_GroupOutput.saveGroupinfo2File(sGroups);              //将分组信息写入文件

        // 2、导出全部联系人，包含联系人属于哪些组的信息
        String sContacts = getAllContacts();
        m_Fun.writeFile(sPath, sContacts);
        return true;
    }

    /*
    JsonObject数据排序（顺序）问题    2018年01月22日 10:18:21 中二涛 阅读数：14043
    JsonObject内部是用Hashmap来存储的，所以输出是按key的排序来的，如果要让JsonObject按固定顺序（put的顺序）排列，
    可以修改JsonObject的定义HashMap改为LinkedHashMap。
    public JSONObject() {
            this.map = new LinkedHashMap();  //new HashMap();
    }
    即定义JsonObject可以这样：JSONObject jsonObj = new JSONObject(new LinkedHashMap());
    或者 JSONObject jsonObj = new JSONObject(true);
    */

    public String getAllContacts() {
        // iFlag：0，不重名的新文件名称；iFlag：1，最新回执信息文件 Receipt_x.txt 名称
        //String sPath = m_Fun.GetNewFile(m_sPathDownloads, "AllContactsLog_1.txt", 0).getAbsolutePath();

        // 获得通讯录信息 ，URI是ContactsContract.Contacts.CONTENT_URI
        m_jsonContactData = new JSONObject(new LinkedHashMap());  //解决JsonObject数据固定顺序
        String sMimetype = "";
        int iOldId = -11;
        int iContactId = -11;
        String contactIdKey = "";
        Cursor cursor = m_MA.getContentResolver().query(Data.CONTENT_URI, null, null, null, Data.RAW_CONTACT_ID);
      //Cursor cursor = m_MA.getContentResolver().query(Groups.CONTENT_URI, null, null, null, null);

        //// 默认情况下查询所有的分组
        //public void getContactsGroups2() {
        //    Cursor cursor = m_MA.getContentResolver().query(Groups.CONTENT_URI, null, null, null, null);
        //    while (cursor.moveToNext()) {
        //        int id = cursor.getInt(cursor.getColumnIndex(Groups._ID));
        //        String title = cursor.getString(cursor.getColumnIndex(Groups.TITLE));
        //        int count = getCountOfGroup(id);
        //        System.out.println("MainActivity" + id + " : " + title + "  " + count);
        //    }
        //    cursor.close();
        //}
        //I/System.out: 组ID：1，组名称：Family，成员数：0
        //I/System.out: 组ID：2，组名称：Friends，成员数：0
        //I/System.out: 组ID：3，组名称：Coworkers，成员数：0
        //I/System.out: 组ID：4，组名称：ICE，成员数：0
        //I/System.out: 组ID：5，组名称：group1，成员数：1
        //I/System.out: 组ID：6，组名称：group2，成员数：1


        //遍历通讯录中所有联系人记录，查询data表中的所有数据。具体查询data表中的3个字段
        //raw_contact_id		联系人ID，iContactId
        //mimetype_id		    数据类型，MimeTypeId
        //data1			        数据，data
        while (cursor.moveToNext()) {
            // 获得通讯录中每个联系人的ID
            iContactId = cursor.getInt(cursor.getColumnIndex(Data.RAW_CONTACT_ID));

            //log2file(m_MA.getContentResolver(), String.valueOf(iContactId), sPath);//将联系人的所有字段及其值输出到文件中

            //保证查询不同contactId的记录时时进行处理，相同contactId记录不处理
            if (iOldId != iContactId) {

                // 理解群组数据：将一个联系人加入某个群组后，该联系人便会增加两项数据"groupId"、"groupSourceId"，当然 "groupSourceId" 可以为空
                /*if (null != m_contactHeaderCount) {
                    // 输出 JSONObject 完整结构到文件，path 为文件绝对路径
                    m_Fun.Json2File(m_contactHeader.m_jsonHeader, m_sPathDownloads, "m_contactHeader.m_jsonHeader_" + iContactId + "_1.txt");
                }*/

                m_contactHeaderCount = new ContactHeader();        //用于存放获取的每条记录每一列的计数器

                //这里放入的jsonObject是一个对象(引用或指针)，放了之后还可以进行操作
                contactIdKey = "contact" + iContactId;
                try {
                    m_jsonContactData.put(contactIdKey, new JSONObject(new LinkedHashMap()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                iOldId = iContactId;
            }

            // 取得 MIMETYPE 类型
            sMimetype = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
            //System.out.println("sMimetype = " + sMimetype);
            // 获取 cursor 中联系人数据最终存入 m_jsonContactData。用该函数可以取代一系列判断代码块
            getContactsData(contactIdKey, sMimetype, cursor);
        }
        cursor.close();

        //由于mContactsHeader中联系人的某种数据(比如mobile手机号)的最大值可能会不断增加，导致mJsonResult中数据长短不一
        //所以，最后再以mContactsHeader中各种数据大小的最终值为标准，再次填充将mJsonContactData.mJsonResult的所有字段填充到mJsonContactData2.mJsonResult中
        //private JSONObject m_jsonContactData2;        //用于存放获取的所有记录
        m_jsonContactData2 = new JSONObject(new LinkedHashMap());  //解决JsonObject数据固定顺序
        dumpJsonContactData(m_jsonContactData, m_jsonContactData2);

        // 输出 JSONObject 完整结构到文件，path 为文件绝对路径
        //m_Fun.Json2File(m_contactHeader.m_jsonHeader, m_sPathDownloads, "m_contactHeader.m_jsonHeader_1.txt");

        return traverseJSON(m_jsonContactData2);
    }

    // 获取 cursor 中联系人数据最终存入 m_jsonContactData。用该函数可以取代一系列判断代码块
    private void getContactsData(String sContactId, String sMimetype, Cursor cursor) {
        try {
            Iterator<String> it1 = m_contactHeader.m_jsonHeader.keys();
            while (it1.hasNext()) {
                String key1 = it1.next();           //key1: "jsonG00StructName"、"jsonG01Phone"、...
                String mimetype = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject("__mimetype_item").getString("__first").trim();
                if (sMimetype.equals(mimetype)) {   // 根据 sMimetype 类型确定 key1
                    switch (getMimetype4lay(key1, "__mimetype_fun")) {
                        case "fun00":   // 默认需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                        case "fun04":   // Set 数据需要分类处理，包括 jsonG04OrgSet、jsonG05ImSet、jsonG08PostalSet
                            fun00_dumpJson4lay(sContactId, key1, cursor, 0);
                            break;
                        case "fun01":   // "jsonG01Phone"，需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                            fun00_dumpJson4lay(sContactId, key1, cursor, 1);
                            break;
                        case "fun02":   // 无需处理 xxx.TYPE_CUSTOM，用 fun02_dumpJson4layAll() 处理。如：jsonG00StructName、jsonG07Note、jsonG09GroupMember
                            fun02_dumpJson4layAll(sContactId, key1, cursor);
                            break;
                        case "fun03":   // "jsonG03Photo"，单独用 fun03_dumpPhoto() 处理
                            fun03_dumpPhoto(sContactId, key1, cursor);
                            break;
                        default:
                            break;
                    }
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getMimetype4lay(String key1, String key2) {
        String mimetype = "";
        try {   //W/System.err: org.json.JSONException: No value for __mimetype_protocal
            if (m_contactHeader.m_jsonHeader.getJSONObject(key1).has(key2)) {
                mimetype = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first").trim();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mimetype;
    }

    //1、第一类型有4层结构，mJsonG00到mJsonG06、mJsonG08
    //m_jsonHeader->jsonG00StructName->displayName->__first
    //2、第二类型有5层结构，mJsonG07、mJsonG09
    //m_jsonHeader->jsonG04OrgSet->jsonG04_00WorkOrgType->workCompany->__first

    //                     ->it1、key1     ->it2、key2          ->it3、key3                 ->it4、key4
    //class ContactHeader  ->JSONObject    ->JSONObject         ->JSONObject                ->JSONObject    ->JSONObject
    //class ContactHeader  ->m_jsonHeader  ->jsonG04OrgSet      ->jsonG04_00WorkOrgType     ->workCompany   ->__first
    //class ContactHeader  ->m_jsonHeader  ->jsonG00StructName  ->displayName               ->__first

    //由于mContactsHeader中联系人的某种数据(比如mobile手机号)的最大值可能会不断增加，导致mJsonResult中数据长短不一，
    // 并且各信息字段的顺序和位置也不一致。所以，最后再以mContactsHeader中各种数据大小的最终值为标准，
    // 再次填充将mJsonContactData.mJsonResult的所有字段填充到mJsonContactData2.mJsonResult中
    //private JSONObject m_jsonContactData2;        //用于存放获取的所有记录的最终结果
    private void dumpJsonContactData(JSONObject jsonSource, JSONObject jsonTarget) {
        //JSONObject属性遍历
        try {
            Iterator<String> it = jsonSource.keys();
            while (it.hasNext()) {
                String key = it.next(); //contact592、contact593、...
                jsonTarget.put(key, new JSONObject(new LinkedHashMap()));
                dumpJsonAllFields(key, jsonSource, jsonTarget); // 一次处理一条联系人记录
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 一次处理一条联系人记录，key 为：contact592、contact593、...
    private void dumpJsonAllFields(String key, JSONObject jsonSource, JSONObject jsonTarget) {
        int n = 0;
        //JSONObject属性遍历
        try {
            Iterator<String> it1 = m_contactHeader.m_jsonHeader.keys();
            while (it1.hasNext()) {
                String key1 = it1.next();       //key1: "jsonG00StructName"、"jsonG01Phone"、...
                Iterator<String> it2 = m_contactHeader.m_jsonHeader.getJSONObject(key1).keys();
                while (it2.hasNext()) {
                    String key2 = it2.next();   //key2: "homeNum"、"mobile"、...
                    Iterator<String> it3 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).keys();
                    String key3 = it3.next();   //key3: "__first"、"__second"

                    //不能显示 "QqIm" 问题的原因。因为前面已经添加前缀变为 "otherQqIm"、"otherQqIm2"、...，而 m_contactHeader 中的原始字段名仍然是 "QqIm"，
                    //  所以遍历 m_contactHeader 中的字段，无法存储已经改名的 "otherQqImx" 字段的值

                    //处理4层结构
                    if (key3.equals("__first")) {
                        // 跳过前面的元素 "__mimetype_x"
                        if (key2.length() > "__mimetype_".length() && key2.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                            continue;
                        }
                        n = Integer.valueOf(m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__second"));
                        Dump2Json(key, key2, n, jsonSource, jsonTarget);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //将mJsonObject的内容转储到mJsonResult中
    //private void Dump2Json(String key2, int n, String key1, JSONObject jsonSource, JSONObject jsonTarget) throws JSONException {
    // key1 为：contact592、contact593、...
    // key2 为："homeNum"、"mobile"、...
    // keyNew 为："homeNum1"、"homeNum2"、...、"mobile1"、"mobile2"、...
    private void Dump2Json(String key1, String key2, int n, JSONObject jsonSource, JSONObject jsonTarget) {
        for (int i = 0; i <= n; i++) {
            String keyNew = key2;
            if (i > 1) {
                keyNew = key2 + i;
            }
            try {
                //不能显示 "QqIm" 问题的原因。因为前面已经添加前缀变为 "otherQqIm"、"otherQqIm2"、...，而 m_contactHeader 中的原始字段名仍然是 "QqIm"，
                //  所以遍历 m_contactHeader 中的字段，无法存储已经改名的 "otherQqImx" 字段的值

                //所以，判断 if (jsonSource.getJSONObject(key1).has(keyNew)) 时，必须进行特殊处理。
                // keyNew 为："QqIm"、"QqIm2"、"QqIm3"、...
                // 想在 jsonTarget 中应查找的 key1 为："otherQqIm"、"otherQqIm2"、"otherQqIm3"、...

                // 遍历 jsonSource 的 key3s ，若找到 key3 的末尾含有 keyNew 子串，则返回找到的 keyNew2 = key; 否则返回 keyNew2 = keyNew
                keyNew = findKey(jsonSource.getJSONObject(key1), keyNew);

                if (jsonSource.getJSONObject(key1).has(keyNew)) {
                    jsonTarget.getJSONObject(key1).put(keyNew, jsonSource.getJSONObject(key1).getString(keyNew));
                } else {
                    jsonTarget.getJSONObject(key1).put(keyNew, "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // keyNew 为："QqIm"、"QqIm2"、"QqIm3"、...
    // 遍历 jsonSource 的 keys ，若找到 key 的末尾含有 keyNew 子串，则返回找到的 keyNew2 = key; 否则返回 keyNew2 = keyNew
    private String findKey(JSONObject jsonSource, String keyNew) {    //该函数有隐藏问题
        String keyNew2 = keyNew;
        Iterator<String> it = jsonSource.keys();
        while (it.hasNext()) {
            String key = it.next();        //key: "otherQqIm"、"otherQqIm2"、"otherQqIm3"、...
            if (key.length() > keyNew.length()) {
                String keyTail = key.substring(key.length() - keyNew.length());   // 取 key 末尾子串
                if (keyTail.equals(keyNew)) {
                    keyNew2 = key;
                    break;
                }
            }
        }
        return keyNew2;
    }

    // keyNew: "otherQqIm"、"otherQqIm2"、"otherQqIm3"、...
    // 返回数组 {keyNew2, keyHead}
    // 遍历 jsonMime，若找到 keyNew 末尾含有 key 子串，则返回找到的 key、keyHead 组成的数组; 否则返回 keyNew2、keyHead 组成的数组
    private String[] findKey2(JSONObject jsonMime, String keyNew) {
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

    // key1 为：contact592、contact593、...
    // keyNew 为："QqIm"、"QqIm2"、"QqIm3"、...
    // 遍历 jsonSource 的 key3s ，若找到 key3 的末尾含有 keyNew 子串，则返回找到的 keyNew2 = key; 否则返回 keyNew2 = keyNew
    private String findKey0(JSONObject jsonSource, String key1, String keyNew) {    //该函数有隐藏问题
        String keyNew2 = keyNew;

        try {
            Iterator<String> it = jsonSource.getJSONObject(key1).keys();
            while (it.hasNext()) {
                String key3 = it.next();        //key3: "otherQqIm"、"otherQqIm2"、"otherQqIm3"、...

                // 去头查找法。优化算法
                if (key3.length() > keyNew.length()) {
                    String keyTemp = key3.substring(key3.length() - keyNew.length());   // 取 key3 末尾子串
                    if (keyTemp.equals(keyNew)) {
                        keyNew2 = key3;
                        break;
                    }
                }
                //                while(keyTemp.length() > keyNew.length()) { //效率较低
                //                    keyTemp = keyTemp.substring(1);   //去掉头一个字符
                //                    if (keyTemp.equals(keyNew)) {
                //                        keyNew2 = key3;
                //                        break;
                //                    }
                //                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 输出字段中没有 home 字段，是因为在 jsonG10Website 的如下结构中查找 "home" 时，
        // if (key3.indexOf(keyNew) != -1) 首先查找到 "homepage" 便返回 "homepage"，导致没有 home 字段
        // 改为 if (key3.lastIndexOf(keyNew) != -1) ，从尾部开始查问题一样。必须改变查找方式，改为去头查找法问题解决

        //String arr2jsonG10Website[][] = {
        //  {"homepage", String.valueOf(Website.TYPE_HOMEPAGE)},    //Website.TYPE_HOMEPAGE = 1;
        //  ...
        //  {"home", String.valueOf(Website.TYPE_HOME)},            //Website.TYPE_HOME = 4;
        //}

        return keyNew2;
    }


    //这里放入的jsonObject是一个对象(引用或指针)，放了之后还可以进行操作
    //m_jsonContactData.put("contact" + mIntSum, mJsonResult);
    //mIntSum++;
    //...
    //return traverseJSON(m_jsonContactData);
    //JSON(JavaScript Object Notation，JavaScript对象容器)
    public String traverseJSON(JSONObject json) {
        //pair: key0,key1,..,keyN  , value0,value1,...,valueN
        Pair<String, String> pair = new Pair<String, String>("", "");
        String values = "";             //存储：value0,value1,...,valueN
        //JSONObject属性遍历
        Iterator<String> it = json.keys();
        while (it.hasNext()) {
            String key = it.next();
            if ("" != values) {     //第一次时values = ""，跳过
                values += "\n";
            }

            try {
                //这里的json.getJSONObject(key)，实际上对应一条mJsonResult结构的联系人记录
                //pair: key0,key1,..,keyN  , value0,value1,...,valueN
                pair = traverseJSON2pair(json.getJSONObject(key), pair.first);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            values += pair.second;
        }
        return pair.first + "\n" + values;
    }

    /*
     * mJsonResult:
     * key0, value0
     * key1, value1
     * ...
     * keyN, valueN
     *
     *
     * pair:
     * key0,key1,..,keyN
     * value0,value1,...,valueN
     *
     * */
    //这里参数json，实际上对应一条mJsonResult结构的联系人记录
    //traverseJSON3函数作用：将mJsonResult中的键值对转换为两个字符串构成的Pair
    //pair: key0,key1,..,keyN  , value0,value1,...,valueN
    //默认导出信息的顺序是保存在手机中的顺序
    public Pair<String, String> traverseJSON2pair(JSONObject json, String keys) {
        String keysNew = "";            //存储：key0,key1,..,keyN
        String values = "";             //存储：value0,value1,...,valueN
        String str = "";

        //JSONObject键值遍历
        Iterator<String> it = json.keys();
        while (it.hasNext()) {
            String key = it.next();
            if ("" != keysNew) {        //第一次时keysNew = ""，跳过
                keysNew += ",";
                values += ",";          //values的正确处理方式
            }
            keysNew += key;

            //存在下面无名联系人时，这种处理方式会出错，会少一些字段。values的正确处理方式见上面
            //I/System.out: contact3 = {"displayName":"","lastName":"","firstName":"","prefix":"","middleName":"","suffix":"","phoneticLastName":"","phoneticFirstName":"","phoneticMiddleName":"","telNum":"13944444444","mobile":"13655555555","mobile2":"","mobile3":"","mobile4":"","workNum":"","workNum2":"","workFax":"","homeFax":"","pager":"","otherNum":"","callbackNum":"","carNum":"","compMainTel":"","isdn":"","mainTel":"","otherFax":"","wirelessDev":"","telegram":"","tty_tdd":"","workMobile":"","workPager":"","assistantNum":"","mms":"","homeEmail":"","workEmail":"","otherEmail":"","mobileEmail":"","anniversary":"","otherday":"","birthday":"","homeMsg":"","workMsg":"","otherMsg":"","customIm":"","aimIm":"","msnIm":"","yahooIm":"","skypeIm":"","qqIm":"","googleTalkIm":"","icqIm":"","jabberIm":"","netmeetingIm":"","remark":"","defaultNickName":"","otherNickName":"","maindenNickName":"","shortNickName":"","initialsNickName":"","workCompany":"","workJobTitle":"","workDepartment":"","workJobDescription":"","workSymbol":"","workPhoneticName":"","workOfficeLocation":"","otherCompany":"","otherJobTitle":"","otherDepartment":"","otherJobDescription":"","otherSymbol":"","otherPhoneticName":"","otherOfficeLocation":"","homepage":"","blog":"","profile":"","home":"","workPage":"","ftpPage":"","otherPage":"","workFormattedAddress":"","workStreet":"","workBox":"","workArea":"","workCity":"","workState":"","workZip":"","workCountry":"","homeFormattedAddress":"","homeStreet":"","homeBox":"","homeArea":"","homeCity":"","homeState":"","homeZip":"","homeCountry":"","otherFormattedAddress":"","otherStreet":"","otherBox":"","otherArea":"","otherCity":"","otherState":"","otherZip":"","otherCountry":""}
            //if ("" != values) {         //第一次时values = ""，跳过
            //    values += ",";
            //}
            try {
                str = json.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            str = str.replace(",", "__");//将字段中的所有英文逗号全部替换为双下划线，以免混淆出错
            values += str;
        }

        //若原来的keys长度大于keysNew，返回原来的keys
        if (keys.length() > keysNew.length()) {
            keysNew = keys;
        }

        return new Pair<String, String>(keysNew, values);
    }

    private String funRemove(String str) {
        if (!TextUtils.isEmpty(str)) {
            str = str.replaceAll("^(0+)", "");  //java正则去掉字符串前导0
            str = str.replace(" ", "");
            str = str.replace("-", "");
            str = str.replace("+86", "");
            str = str.replace("+", "");
            str = str.replace("\\", "");
            str = str.replace("(", "");
            str = str.replace(")", "");
        }
        return str;
    }

    ////////////////////////////////////////////////////////////////////////
    //转储联系人各个字段数据的函数组 Begin

    //JSONObject中根据value获取key值，必须value值不重复
    //原创将心666666于2014-10-01，https://blog.csdn.net/jiangxindu1/article/details/39720481
    public String getKey(String val, String key1) {
        String key2 = "";
        try {
            JSONObject json = m_contactHeader.m_jsonHeader.getJSONObject(key1);
            Iterator<String> it = json.keys();
            while (it.hasNext()) {
                key2 = it.next();
                if (json.getJSONObject(key2).getString("__first").equals(val)) {
                    break;
                } else {
                    key2 = "";  // 不匹配必须将key2清空。否则当全部不匹配时，key2将等于最后一个测试值，导致明显的查找错误
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return key2;
    }

    //String substring(int beginIndex, int endIndex)，返回的字串长度为 endIndex-beginIndex。
    //endIndex(不包括)可以指向末尾字符之后的数值("emptiness".length())："emptiness".substring(9) returns "" (an empty string)

    // 根据 subtype 值，取得类型前缀。处理 __mimetype_subtype_ 字段
    private String getPrefix(String subtype, String key1) {
        String prefix = getKey(subtype, key1); //JSONObject中根据value获取key值，必须value值不重复
        //System.out.println("key1 = " + key1 + "；subtype = " + subtype + "；prefix = " + prefix);
        if (prefix.indexOf("__mimetype_subtype_") != -1) {
            prefix = prefix.substring(prefix.lastIndexOf("_") + 1); // 截取 prefix 最后一个 "_" 后面的字串，"custom"、"home"、"work"、"other"
        } else {
            prefix = "";
        }
        //System.out.println("key1 = " + key1 + "；subtype = " + subtype + "；prefix = " + prefix);
        return prefix;
    }

    //    前面 getContactsData() 函数中已经根据 sMimetype 类型确定 key1
    // A、type 为大类型(data2)：Phone.TYPE、Email.TYPE、...
    // B、根据大类型 type 取出子类型 xxx.TYPE_HOME、xxx.TYPE_WORK、...；
    // C、若 data2 中取出的是自定义类型 xxx.TYPE_CUSTOM(0)，就要取 xxx.LABEL(data3) 中的值，作为子类型
    // D、若存在 "__mimetype_protocal" 字段，便取出 xxx.PROTOCOL(data5) 的值，作为子类型
    // E、若 data5 中取出的是自定义协议 PROTOCOL_CUSTOM(0)，就要取 CUSTOM_PROTOCOL(data6) 中的值，作为子类型

    // 取出4层 JSONObject 结构对应的信息转储到 m_jsonContactData 中。每次转储该种类的指定子类型的字段
    // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标；int iPhone : 0 非电话号码；1 电话号码
    private void fun00_dumpJson4lay(String idKey, String key1, Cursor cursor, int iPhone) {
        // 正确用法！取当前 cursor 对应的信息子类型
        String type = getMimetype4lay(key1, "__mimetype_type").trim();  // type 为大类型(data2)：Phone.TYPE、Email.TYPE、...
        int iSubtype = cursor.getInt(cursor.getColumnIndex(type));      // subtype 为子类型，比如 xxx.TYPE_HOME、xxx.TYPE_WORK、...
        String prefix = getPrefix(String.valueOf(iSubtype), key1);      // 根据 subtype 值，取得类型前缀。处理 __mimetype_subtype_ 字段

        // 处理 jsonG05ImSet
        String protocal = getMimetype4lay(key1, "__mimetype_protocal").trim();
        if (!TextUtils.isEmpty(protocal)) {  //若存在 "__mimetype_protocal" 字段，便取出 xxx.PROTOCOL(data5) 的值，作为子类型
            iSubtype = cursor.getInt(cursor.getColumnIndex(protocal));
        }

        String subtype = String.valueOf(iSubtype).trim();  // 默认处理
        // 注意：下面用法不对！会出现把字符的Ascii值当做数值，然后再转换为数字字符串返回。比如，原应返回数字 0，这种方式可能会返回数字字符串 30
        //String subtype = cursor.getString(cursor.getColumnIndex(subtype)).trim();

        try {
            Iterator<String> it = m_contactHeader.m_jsonHeader.getJSONObject(key1).keys();
            while (it.hasNext()) {
                //key1 : jsonG01Phone、jsonG02Email、...
                //key2 : homeNum、mobile、__mimetype_x、...
                String key2 = it.next();                                        // 获得key
                // 前面已经处理完 "__mimetype_" ，所以后续处理跳过前面的元素 "__mimetype_x"
                if (key2.length() > "__mimetype_".length() && key2.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                    //System.out.println("key2.substring(0, \"__mimetype_\".length()) = " + key2.substring(0, "__mimetype_".length()));
                    continue;
                }
                //subtype : 当前cursor对应的信息子类型
                //subtype2 : 0、1、2、...
                String subtype2 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first");

                //System.out.println("subtype = " + subtype + "; subtype1 = " + subtype2);

                if (subtype.equals(subtype2)) {
                    String col = getMimetype4lay(key1, "__mimetype_data").trim();   // 取 __mimetype_data 字段值，Phone.DATA(data1)等
                    int iCol = cursor.getColumnIndex(col);                          // 获取该类信息在数据表中的列号(字段号)
                    String val = "";
                    if (iCol > -1) {
                        val = cursor.getString(iCol);   // 获取数据表中的数据
                        //System.out.println("subtype = " + subtype + "; subtype1 = " + subtype2 + "; val = " + val);
                        if (1 == iPhone) {
                            val = funRemove(val);       // 电话号码才处理
                        }
                    }
                    put2json4lay(idKey, key1, key2, val, prefix);   // 将获取的数据存入 m_jsonContactData
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 取出4层 JSONObject 结构对应的所有信息转储到 m_jsonContactData 中，比如 jsonG00StructName
    // idKey : contactIdKey，如：contact1、contact2、...；
    // key1 : m_jsonHeader的key1，如：jsonG00StructName、jsonG07Note、jsonG09GroupMember；
    // cursor : 查询游标
    private void fun02_dumpJson4layAll(String idKey, String key1, Cursor cursor) {
        try {
            Iterator<String> it = m_contactHeader.m_jsonHeader.getJSONObject(key1).keys();
            while (it.hasNext()) {
                String key2 = it.next();                                    // key2："groupId"、"groupSourceId"、"__mimetype_xxx"、...
                // 跳过前面的元素 "__mimetype_xxx"
                if (key2.length() > "__mimetype_".length() && key2.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                    continue;
                }

                String data = "";
                //专门处理 jsonG09GroupMember 的 {"groupTitle", Groups.TITLE}    //Group.TITLE = "title";
                //由于新建联系人群组时一般是输入群组名称，所以在联系人信息中保存联系人群组名称
                if (key2.equals("groupTitle") && m_jsonContactData.getJSONObject(idKey).has("groupId")){
                    String groupId = m_jsonContactData.getJSONObject(idKey).getString("groupId");
                    if (!TextUtils.isEmpty(groupId)){
                        data = m_GroupOutput.getGroupsName(groupId);
                    }
                } else {
                    String col = get4layColumnName(key1, key2);             // 获取该类信息的在数据表中的列号(字段号)
                    data = cursor.getString(cursor.getColumnIndex(col));    // 获取数据表中的数据
                }
                put2json4lay(idKey, key1, key2, data, "");          // 将获取的数据存入 m_jsonContactData
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 将获取的数据存入 m_jsonContactData
    // idKey : contactIdKey，如：contact1、contact2、...；
    // key1 : m_jsonHeader的key1，如：jsonG00StructName、jsonG07Note、jsonG09GroupMember；
    // key2："groupId"、"groupSourceId"、"__mimetype_xxx"、...
    // val: 存入的数据 data
    // prefix: 子类型前缀，home、work、other、...
    // cursor : 查询游标
    private void put2json4lay(String idKey, String key1, String key2, String val, String prefix) {
        String keyNew = prefix + key2;
        try {
            int n = Integer.valueOf(m_contactHeaderCount.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__second"));
            n++;
            m_contactHeaderCount.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).put("__second", String.valueOf(n));
            if (n > 1) {
                keyNew += n;
            }
            m_jsonContactData.getJSONObject(idKey).put(keyNew, val);

            //没有下面两行就只能获得某种数据的第一个值。比如只能获得第一个手机号，其他手机号丢失
            n = java.lang.Math.max(n, Integer.valueOf(m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__second")));
            m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).put("__second", String.valueOf(n));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String get4layColumnName(String key1, String key2) {
        String col = "";
        try {
            //E/CursorWindow: Failed to read row 0, column -1 from a CursorWindow which has 10 rows, 82 columns.
            //return m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first");
            col = String.valueOf(m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return col;
    }

    //转储联系人各个字段数据的函数组 End
    ////////////////////////////////////////////////////////////////////////

    //将联系人的所有字段及其值输出到文件中，sPath为文件的绝对路径
    //调用方式
    // iFlag：0，不重名的新文件名称；iFlag：1，最新回执信息文件 Receipt_x.txt 名称
    // 在循环外获得sPath
    // String sPath = m_Fun.GetNewFile(m_sPathDownloads, "AllContactsLog_1.txt", 0).getAbsolutePath();
    // 在循环内调用
    // log2file(m_MA.getContentResolver(), String.valueOf(iContactId), sPath);//将联系人的所有字段及其值输出到文件中
    private void log2file(final ContentResolver contentResolver, String contactId, String sPath) {
        Cursor dataCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.CONTACT_ID + "=?",
                new String[]{String.valueOf(contactId)}, null);
        if (dataCursor != null) {
            if (dataCursor.getCount() > 0) {
                //System.out.println("---------------------- start ------------------------");
                AppendFile(sPath, "---------------------- start ------------------------");
                //System.out.println("数量:" + dataCursor.getCount() + " 列数:" + dataCursor.getColumnCount());
                AppendFile(sPath, "数量:" + dataCursor.getCount() + " 列数:" + dataCursor.getColumnCount());
                if (dataCursor.moveToFirst()) {
                    do {
                        for (int i = 0; i < dataCursor.getColumnCount(); i++) {
                            final String columnName = dataCursor.getColumnName(i);
                            final int columnIndex = dataCursor.getColumnIndex(columnName);
                            final int type = dataCursor.getType(columnIndex);
                            String data = "", ty = "";
                            if (type == Cursor.FIELD_TYPE_NULL) {
                                ty = "NULL";
                                data = "空值";
                            } else if (type == Cursor.FIELD_TYPE_BLOB) {
                                ty = "BLOB";
                                data = String.valueOf(dataCursor.getBlob(columnIndex));
                            } else if (type == Cursor.FIELD_TYPE_FLOAT) {
                                ty = "FLOAT";
                                data = String.valueOf(dataCursor.getFloat(columnIndex));
                            } else if (type == Cursor.FIELD_TYPE_INTEGER) {
                                ty = "INTEGER";
                                data = String.valueOf(dataCursor.getInt(columnIndex));
                            } else if (type == Cursor.FIELD_TYPE_STRING) {
                                ty = "STRING";
                                data = dataCursor.getString(columnIndex);
                            }
                            //System.out.println("第" + i + "列->名称:" + columnName + " 索引:" + columnIndex + " 类型:" + ty + " 值:" + data);
                            //AppendFile(sPath, "第" + i + "列->名称:" + columnName + " 索引:" + columnIndex + " 类型:" + ty + " 值:" + data);

                            //java中int转成String位数不足前面补零。String.format自带补零方法，
                            //String.format("%06d",12);//其中0表示补零而不是补空格，6表示至少6位，d表示参数为整数类型
                            AppendFile(sPath, "第" + String.format("%02d", i) + "列->名称:" + columnName + " 索引:" + String.format("%02d", columnIndex) + " 类型:" + ty + " 值:" + data);
                        }
                        AppendFile(sPath, "\n");
                    } while (dataCursor.moveToNext());
                }
                //System.out.println("------------------------ end ------------------------");
                AppendFile(sPath, "------------------------ end ------------------------\n");
            }
            dataCursor.close();
        }
    }

    //java追加写入文件内容，sPath为文件的绝对路径。https://www.cnblogs.com/zhenxiangyue/p/10900319.html
    public void AppendFile(String sPath, String str) {
        FileWriter fw = null;
        try {   //如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f = new File(sPath);
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println(str);
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //打印联系人的所有字段及其值。会输出太多信息导致溢出，输出不全
    //调用：logData(m_MA.getContentResolver(), String.valueOf(iContactId));
    private void logData(final ContentResolver contentResolver, String contactId) {
        Cursor dataCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.CONTACT_ID + "=?",
                new String[]{String.valueOf(contactId)}, null);
        if (dataCursor != null) {
            if (dataCursor.getCount() > 0) {
                System.out.println("---------------------- start ------------------------");
                System.out.println("数量:" + dataCursor.getCount() + " 列数:" + dataCursor.getColumnCount());
                if (dataCursor.moveToFirst()) {
                    do {
                        for (int i = 0; i < dataCursor.getColumnCount(); i++) {
                            final String columnName = dataCursor.getColumnName(i);
                            final int columnIndex = dataCursor.getColumnIndex(columnName);
                            final int type = dataCursor.getType(columnIndex);
                            String data = "", ty = "";
                            if (type == Cursor.FIELD_TYPE_NULL) {
                                ty = "NULL";
                                data = "空值";
                            } else if (type == Cursor.FIELD_TYPE_BLOB) {
                                ty = "BLOB";
                                data = String.valueOf(dataCursor.getBlob(columnIndex));
                            } else if (type == Cursor.FIELD_TYPE_FLOAT) {
                                ty = "FLOAT";
                                data = String.valueOf(dataCursor.getFloat(columnIndex));
                            } else if (type == Cursor.FIELD_TYPE_INTEGER) {
                                ty = "INTEGER";
                                data = String.valueOf(dataCursor.getInt(columnIndex));
                            } else if (type == Cursor.FIELD_TYPE_STRING) {
                                ty = "STRING";
                                data = dataCursor.getString(columnIndex);
                            }
                            System.out.println("第" + i + "列->名称:" + columnName + " 索引:" + columnIndex + " 类型:" + ty + " 值:" + data);
                        }
                    } while (dataCursor.moveToNext());
                }
                System.out.println("------------------------ end ------------------------");
            }
            dataCursor.close();
        }
    }

    //03、jsonG03Photo，头像。
    // 取出4层 JSONObject 结构对应的所有信息转储到 m_jsonContactData 中，比如 jsonG00StructName
    // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标
    private void fun03_dumpPhoto(String idKey, String key1, Cursor cursor) {
        String typePhoto = getMimetype4lay(key1, "photo").trim();  // typePhoto 为：Photo.PHOTO

        Bitmap photoBmp = null;
        //        Cursor dataCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, new String[]{Photo.PHOTO},
        //                ContactsContract.Data.CONTACT_ID + "=?" + " AND " +
        //                        ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'",
        //                new String[]{String.valueOf(contactId)}, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                //cursor.moveToFirst();
                //byte[] bytes = cursor.getBlob(cursor.getColumnIndex(Photo.PHOTO));
                byte[] bytes = cursor.getBlob(cursor.getColumnIndex(typePhoto));
                //System.out.println("bytes.length = " + bytes.length);
                if (bytes != null) {
                    photoBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    //System.out.println("photoBmp.toString() = " + photoBmp.toString());
                }
            }
            //cursor.close();
        }
        String filename = "";
        try {
            filename = m_jsonContactData.getJSONObject(idKey).getString("displayName") + "_1";
            filename = filename.replace(" ", "_");  // 文件名空格全部替换为"_"
        } catch (JSONException e) {
            filename = "anonymity_1";
            e.printStackTrace();
        }
        saveBmpFile(photoBmp, m_sPathDownloads + "/Photo", filename, "png", 100);
        //return photoBmp;
    }

    // 获取联系人头像。原文链接：https://blog.csdn.net/angcyo/article/details/52177832
    public static Bitmap getPhoto(final ContentResolver contentResolver, String contactId) {
        Bitmap photo = null;
        Cursor dataCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, new String[]{Photo.PHOTO},
                ContactsContract.Data.CONTACT_ID + "=?" + " AND " +
                        ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'",
                new String[]{String.valueOf(contactId)}, null);
        if (dataCursor != null) {
            if (dataCursor.getCount() > 0) {
                dataCursor.moveToFirst();
                byte[] bytes = dataCursor.getBlob(dataCursor.getColumnIndex(Photo.PHOTO));
                if (bytes != null) {
                    photo = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                }
            }
            dataCursor.close();
        }
        return photo;
    }

    //Android笔记（15）将bitmap存为文件。2017-07-19
    //原文链接：https://blog.csdn.net/yangye608/article/details/75332958
    // filename 不带后缀；photoType : jpg、png；iQuality：压缩质量 50 - 100
    // 默认字符变量、特殊类型变量可以不带前缀，比如：String path、BufferedOutputStream bos
    private File saveBmpFile(Bitmap bmp, String path, String filename, String photoType, int iQuality) {
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }

        // iFlag：0，不重名的新文件名称；iFlag：1，最新回执信息文件 Receipt_x.txt 名称
        // String sPath = m_Fun.GetNewFile(m_sPathDownloads, "AllContactsLog_1.txt", 0).getAbsolutePath();
        //File file = new File(path, filename);
        File file = m_Fun.GetNewFile(path, filename + "." + photoType, 0);    // 获得不重名的新文件名称：Photo_x.bmp
        //System.out.println("filePath = " + file.getAbsolutePath());
        //I/System.out: filePath = /storage/emulated/0/Android/data/com.example.contacts2csv/files/Download/Photo/Wang Wu_2.png
        //S7            filePath = /storage/emulated/0/Android/data/com.example.contacts2csv/files/Download/Photo/Wang_Wu_12.png
        try {
            // 将Bitmap压缩成PNG编码，质量为100%存储
            //            ByteArrayOutputStream os = new ByteArrayOutputStream();
            //            bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
            //            byte[] photoPng = os.toByteArray();

            //若 bmp 为空，会报错
            //java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.graphics.Bitmap.compress
            // (android.graphics.Bitmap$CompressFormat, int, java.io.OutputStream)' on a null object reference

            // 将Bitmap压缩成PNG编码，质量为100%存储
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            switch (photoType) {
                case "jpg":
                    //bmp.compress(Bitmap.CompressFormat.JPEG, 80, bos); // 输出这种 JPEG, 80 文件后大约 2.5K
                    bmp.compress(Bitmap.CompressFormat.JPEG, iQuality, bos); // 输出 JPEG, 100 文件后大约 7K
                    break;
                case "png":
                    bmp.compress(Bitmap.CompressFormat.PNG, iQuality, bos);  // 输出这种 png, 100 文件后大约 16K
                    break;
                default:
                    break;
            }
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
