//获取通讯录所有字段
//2012年01月02日 14:48:01 yemh111 阅读数：1368
package com.example.contacts2csv;
// 编译error: unmappable character for encoding UTF-8。
// 解决办法-OK，用文本编辑器打开文件，将文件另存为UTF-8格式
// 有警告，没关系：uses unchecked or unsafe operations. Recompile with -Xlint:unchecked for details.

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.Pair;
import android.provider.ContactsContract.CommonDataKinds.Photo;

import static com.example.contacts2csv.MainActivity.m_MA;

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
    private JSONObject m_jsonContactData;            //用于存放获取的所有记录中间数据
    private JSONObject m_jsonContactData2;           //用于存放获取的所有记录最终结果
    private ContactHeader m_contactHeader;         //用于存放通讯录所有记录的表头信息
    private ContactHeader m_contactHeaderCount;    //用于存放获取的每条记录每一列的计数器
    private final String m_sTAG = getClass().getSimpleName();

    public int getSum() {
        return m_jsonContactData2.length();
    }

    public ContactOutput() {
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

    public boolean outputAllContacts(Context context, String sPath) {
        String sContacts = getAllContacts();
        writeFile(sPath, sContacts);
        return true;
    }

    public void writeFile(String sPath, String str) {
        try {
            File file = new File(sPath);
            FileWriter writer = new FileWriter(file, false);
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            // 获取 cursor 中联系人数据最终存入 m_jsonContactData。用该函数可以取代一系列判断代码块
            getContactsData(contactIdKey, sMimetype, cursor);
        }
        cursor.close();

        //由于mContactsHeader中联系人的某种数据(比如mobile手机号)的最大值可能会不断增加，导致mJsonResult中数据长短不一
        //所以，最后再以mContactsHeader中各种数据大小的最终值为标准，再次填充将mJsonContactData.mJsonResult的所有字段填充到mJsonContactData2.mJsonResult中
        //private JSONObject m_jsonContactData2;        //用于存放获取的所有记录
        m_jsonContactData2 = new JSONObject(new LinkedHashMap());  //解决JsonObject数据固定顺序
        dumpJsonContactData(m_jsonContactData, m_jsonContactData2);

        //        try {  // 实现 Logcat 输出 m_jsonContactData2 完整结构
        //            System.out.println("m_jsonContactData2 : \n" + m_jsonContactData2.toString(4));
        //        } catch (JSONException e) {
        //            e.printStackTrace();
        //        }

        return traverseJSON(m_jsonContactData2);
    }

    // 获取 cursor 中联系人数据最终存入 m_jsonContactData。用该函数可以取代一系列判断代码块
    private void getContactsData(String sContactId, String sMimetype, Cursor cursor) {
        try {
            //String mimetype = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first").trim();
            Iterator<String> it1 = m_contactHeader.m_jsonHeader.keys();
            while (it1.hasNext()) {
                String key1 = it1.next();       //key1: "jsonG00StructName"、"jsonG01Phone"、...
                Iterator<String> it2 = m_contactHeader.m_jsonHeader.getJSONObject(key1).keys();
                while (it2.hasNext()) {
                    String key2 = it2.next();   //key2: "__mimetype_x"、"displayName"、"lastName"、...
                    Iterator<String> it3 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).keys();
                    String key3 = it3.next();   //4层结构key3: "__first"、"__second"；5层结构key3：不会是这些值
                    //处理第一类型有4层结构，mJsonG00到mJsonG03、mJsonG05到mJsonG07、mJsonG09、mJsonG11到mJsonG13
                    if ("__first" == key3) {
                        String mimetype = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject("__mimetype_item").getString("__first").trim();
                        if (sMimetype.equals(mimetype)) {
                            switch (getMimetype4lay(key1, "__mimetype_fun")) {
                                case "fun00":  // 默认需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                                    fun00_dumpJson4lay(sContactId, key1, cursor, 0);
                                    break;
                                case "fun01":   // "jsonG01Phone"，需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                                    fun00_dumpJson4lay(sContactId, key1, cursor, 1);
                                    break;
                                case "fun02":   //无需处理 xxx.TYPE_CUSTOM，用 fun02_dumpJson4layAll() 处理
                                    fun02_dumpJson4layAll(sContactId, key1, cursor);
                                    break;
                                case "fun03":   // "jsonG03Photo"，单独用 dumpPhoto() 处理
                                    break;
                                case "fun04":   // Set 数据需要分类处理，用 dumpJson4laySet() 处理
                                    break;
                                case "fun05":   // "jsonG05Im"，用 fun05_dumpJson4layIm2() 处理
                                    fun05_dumpJson4layIm2(sContactId, key1, cursor, 0);
                                    break;
                                default:
                                    break;
                            }
                            return;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 获取 cursor 中联系人数据最终存入 m_jsonContactData。用该函数可以取代一系列判断代码块
    private void getContactsData0(String sContactId, String sMimetype, Cursor cursor) {
        try {
            //String mimetype = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first").trim();
            Iterator<String> it1 = m_contactHeader.m_jsonHeader.keys();
            while (it1.hasNext()) {
                String key1 = it1.next();       //key1: "jsonG00StructName"、"jsonG01Phone"、...
                Iterator<String> it2 = m_contactHeader.m_jsonHeader.getJSONObject(key1).keys();
                while (it2.hasNext()) {
                    String key2 = it2.next();   //key2: "__mimetype_x"、"displayName"、"lastName"、...
                    Iterator<String> it3 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).keys();
                    String key3 = it3.next();   //4层结构key3: "__first"、"__second"；5层结构key3：不会是这些值
                    //处理第一类型有4层结构，mJsonG00到mJsonG03、mJsonG05到mJsonG07、mJsonG09、mJsonG11到mJsonG13
                    if ("__first" == key3) {
                        String mimetype = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject("__mimetype_item").getString("__first").trim();
                        if (sMimetype.equals(mimetype)) {
                            if(key1.equals("jsonG00StructName")){
                                fun02_dumpJson4layAll(sContactId, key1, cursor);
                            } else if (key1.equals("jsonG01Phone")) {
                                fun00_dumpJson4lay(sContactId, key1, cursor, 1);
                            } else {
                                fun00_dumpJson4lay(sContactId, key1, cursor, 0);
                            }
                            return;
                        }
                    } else {
                        /*
                         * 对第二类型有5层结构mJsonG04、mJsonG08、mJsonG10
                         * key1: "jsonG04OrgSet"、"jsonG08PostalSet"、"jsonG10WebSet"
                         * key2: "jsonG04_00WorkOrgType"、"jsonG04_01OtherOrgType"、...
                         * key3: "__mimetype_x"、"workCompany"、"workJobTitle"、...
                         * key4: 5层结构key4: "__first"、"__second"
                         * */
                        it3 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).keys();
                        Iterator<String> it4;
                        String key4;
                        while (it3.hasNext()) {
                            key3 = it3.next();
                            it4 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).keys();
                            key4 = it4.next();
                            //处理第二类型有5层结构，mJsonG04、mJsonG08、mJsonG10
                            if ("__first" == key4) {
                                String mimetype = mimetype = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject("__mimetype_item").getJSONObject(key3).getString("__first").trim();
                                if (sMimetype.equals(mimetype)) {
                                    dumpJson5lay(sContactId, key1, cursor);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getMimetype4lay(String key1, String key2) {
        String mimetype = "";
        try {
            mimetype = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first").trim();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mimetype;
    }

    private String getMimetype5lay(String key1, String key2, String key3) {
        String mimetype = "";
        try {
            //mimetype = m_contactHeader.m_jsonHeader.getJSONObject(key1).getString(key2);
            //I/System.out: mimetype = {"__first":"vnd.android.cursor.item\/name","__second":"0"}
            mimetype = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).getString("__first").trim();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //System.out.println("mimetype = " + mimetype);
        return mimetype;
    }

    //1、第一类型有4层结构，mJsonG00到mJsonG06、mJsonG08
    //m_jsonHeader->jsonG00StructName->displayName->__first
    //2、第二类型有5层结构，mJsonG07、mJsonG09
    //m_jsonHeader->jsonG04OrgSet->jsonG04_00WorkOrgType->workCompany->__first

    //                      ->it1、key1     ->it2、key2           ->it3、key3                 ->it4、key4
    //class ContactHeader  ->JSONObject    ->JSONObject          ->JSONObject                ->JSONObject    ->JSONObject
    //class ContactHeader  ->m_jsonHeader  ->jsonG04OrgSet     ->jsonG04_00WorkOrgType    ->workCompany   ->__first
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
                String key = it.next();
                jsonTarget.put(key, new JSONObject(new LinkedHashMap()));
                dumpJsonAllFields(key, jsonSource, jsonTarget);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void dumpJsonAllFields(String key, JSONObject jsonSource, JSONObject jsonTarget) {
        int n = 0;
        //JSONObject属性遍历
        try {
            Iterator<String> it1 = m_contactHeader.m_jsonHeader.keys();
            while (it1.hasNext()) {
                String key1 = it1.next();       //key1: "jsonG00StructName"、"jsonG01Phone"、...
                Iterator<String> it2 = m_contactHeader.m_jsonHeader.getJSONObject(key1).keys();
                while (it2.hasNext()) {
                    String key2 = it2.next();   //key2: "displayName"、"lastName"、...
                    Iterator<String> it3 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).keys();
                    String key3 = it3.next();
                    //处理第一类型有4层结构，mJsonG00到mJsonG06、mJsonG08
                    if ("__first" == key3) {
                        // 跳过前面的元素 "__mimetype_x"
                        if (key2.length() > "__mimetype_".length() && key2.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                            continue;
                        }
                        n = Integer.valueOf(m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__second"));
                        Dump2Json(key, key2, n, jsonSource, jsonTarget);
                    } else {
                        /*
                         * 对第二类型有5层结构mJsonG07、mJsonG09等
                         * key1: "jsonG04OrgSet"
                         * key2: "jsonG04_00WorkOrgType"、"jsonG04_01OtherOrgType"、...
                         * key3: "workCompany"、"workJobTitle"、...
                         * */
                        it3 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).keys();
                        Iterator<String> it4;
                        String key4;
                        while (it3.hasNext()) {
                            key3 = it3.next();
                            it4 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).keys();
                            key4 = it4.next();
                            //处理第二类型有5层结构，mJsonG07、mJsonG09
                            if ("__first" == key4) {
                                // 跳过前面的元素 "__mimetype_x"
                                if (key3.length() > "__mimetype_".length() && key3.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                                    continue;
                                }
                                n = Integer.valueOf(m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).getString("__second"));
                                Dump2Json(key, key3, n, jsonSource, jsonTarget);
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //将mJsonObject的内容转储到mJsonResult中
    //private void Dump2Json(String keyField, int n, String key, JSONObject jsonSource, JSONObject jsonTarget) throws JSONException {
    private void Dump2Json(String key, String keyField, int n, JSONObject jsonSource, JSONObject jsonTarget) {
        for (int i = 0; i <= n; i++) {
            String keyNew = keyField;
            if (i > 1) {
                keyNew = keyField + i;
            }
            try {
                if (jsonSource.getJSONObject(key).has(keyNew)) {
                    jsonTarget.getJSONObject(key).put(keyNew, jsonSource.getJSONObject(key).getString(keyNew));
                } else {
                    jsonTarget.getJSONObject(key).put(keyNew, "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
        str = str.replaceAll("^(0+)", "");  //java正则去掉字符串前导0
        str = str.replace(" ", "");
        str = str.replace("-", "");
        str = str.replace("+86", "");
        str = str.replace("+", "");
        str = str.replace("\\", "");
        str = str.replace("(", "");
        str = str.replace(")", "");
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
                // 跳过前面的元素 "__mimetype_x"
                //if (key2.length() > "__mimetype_".length() && key2.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                    //System.out.println("key2.substring(0, \"__mimetype_\".length()) = " + key2.substring(0, "__mimetype_".length()));
                    //continue;
                //}

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

    //根据 type 值，取得类型前缀
    private String getPrefix(String type, String key1) {
        String prefix = getKey(type, key1); //JSONObject中根据value获取key值，必须value值不重复
        if (prefix.indexOf("__mimetype_subtype_") != -1){
            prefix = prefix.substring(prefix.lastIndexOf("_")); // 截取 prefix 最后一个 "_" 后面的字串，"custom"、"home"、"work"、"other"
        } else {
            prefix = "";
        }
        return prefix;
    }

    /**
     * Returns the value of the requested column as a String.
     *
     * <p>The result and whether this method throws an exception when the
     * column value is null or the column type is not a string type is
     * implementation-defined.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a String.
     */
    //String getString(int columnIndex);    //得到 cursor 对应列的 String 类型的值

    /**
     * Returns the value of the requested column as an int.
     *
     * <p>The result and whether this method throws an exception when the
     * column value is null, the column type is not an integral type, or the
     * integer value is outside the range [<code>Integer.MIN_VALUE</code>,
     * <code>Integer.MAX_VALUE</code>] is implementation-defined.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as an int.
     */
    //int getInt(int columnIndex);          //得到 cursor 对应列的 int 类型的值


    // 对im操作
    // A、首先判断 Im.TYPE(data2) 的类型；
    // B、若 data2 中取出的是自定义类型 Im.TYPE_CUSTOM(0)，就要取 Im.LABEL(data3) 中的值
    // C、接着取 Im.PROTOCOL(data5) 的值，判断是哪种Im。
    // D、若 data5 中取出的是自定义协议 PROTOCOL_CUSTOM(0)，就要取 CUSTOM_PROTOCOL(data6) 中的值
    //05、jsonG05Im，即时消息。
    // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标；int iPhone : 0 非电话号码；1 电话号码
    private void fun05_dumpJson4layIm2(String idKey, String key1, Cursor cursor, int iPhone) {
        // type 为信息种类(大类型)，比如Phone.TYPE、Email.TYPE等
        String type = getMimetype4lay(key1, "__mimetype_type").trim();
        // type为 type 种类信息的子类型，比如Phone.TYPE大类型中的Phone.TYPE_HOME、Phone.TYPE_MOBILE等
        int iSubtype = cursor.getInt(cursor.getColumnIndex(type));  // 正确用法！取当前cursor对应的信息子类型
        String prefix = getPrefix(String.valueOf(iSubtype), key1);      // 获取类型前缀

//        // 取出Im类型，获取类型前缀
//        int imType = cursor.getInt(cursor.getColumnIndex(Im.TYPE));
//        String prefix = getPrefix(String.valueOf(imType), key1);
//        if (Im.TYPE_CUSTOM == imType) {
//            imType = cursor.getInt(cursor.getColumnIndex(Im.LABEL));
//        }
//
//        // 获取即时通讯协议类型
//        int imProtocal = cursor.getInt(cursor.getColumnIndex(Im.PROTOCOL));
//        if (Im.PROTOCOL_CUSTOM == imProtocal) {
//            imProtocal = cursor.getInt(cursor.getColumnIndex(Im.CUSTOM_PROTOCOL));
//        }
//        // 获取即时通讯信息
//        if (Im.TYPE_CUSTOM == imProtocal) {
//            String workMsg = cursor.getString(cursor.getColumnIndex(Im.DATA));
//            put2json4lay(idKey, key1, getKey(String.valueOf(Im.TYPE_CUSTOM), key1), workMsg);       // 将获取的数据存入 m_jsonContactData
//        } else if (Im.PROTOCOL_MSN == imProtocal) {
//            String workMsn = cursor.getString(cursor.getColumnIndex(Im.DATA));
//            put2json4lay(idKey, key1, getKey(String.valueOf(Im.PROTOCOL_MSN), key1), workMsn);      // 将获取的数据存入 m_jsonContactData
//        }
//        if (Im.PROTOCOL_QQ == imProtocal) {
//            String instantsMsg = cursor.getString(cursor.getColumnIndex(Im.DATA));
//            put2json4lay(idKey, key1, getKey(String.valueOf(Im.PROTOCOL_QQ), key1), instantsMsg);   // 将获取的数据存入 m_jsonContactData
//        }

        String subtype = "";
        String protocal = getMimetype4lay(key1, "__mimetype_custom_protocal").trim();
        if (!TextUtils.isEmpty(protocal)){
            subtype = protocal;
        } else {
            subtype = String.valueOf(iSubtype);
            // 注意：下面用法不对！会出现把字符的Ascii值当做数值，然后再转换为数字字符串返回。比如，原应返回数字 0，这种方式可能会返回数字字符串 30
            //String subtype = cursor.getString(cursor.getColumnIndex(subtype)).trim();
        }

        try {
            Iterator<String> it = m_contactHeader.m_jsonHeader.getJSONObject(key1).keys();
            while (it.hasNext()) {
                String key2 = it.next();                                        // 获得key
                // 跳过前面的元素 "__mimetype_x"
                if (key2.length() > "__mimetype_".length() && key2.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                    //System.out.println("key2.substring(0, \"__mimetype_\".length()) = " + key2.substring(0, "__mimetype_".length()));
                    continue;
                }
                String type2 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first");

                if (subtype.equals(type2)) {
                    String col = getMimetype4lay(key1, "__mimetype_data").trim();// 获取该类信息的在数据表中的列号(字段号)，Phone.DATA等
                    int iCol = cursor.getColumnIndex(col);
                    String data = "";
                    if (iCol > -1) {
                        //E/CursorWindow: Failed to read row 0, column -1 from a CursorWindow which has 10 rows, 82 columns.
                        //String telNum = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "telNum")));
                        data = cursor.getString(iCol);          // 获取数据表中的数据
                        if (1 == iPhone) {
                            data = funRemove(data);             // 电话号码才处理
                        }
                    }
                    put2json4lay2(idKey, key1, key2, data, prefix); // 将获取的数据存入 m_jsonContactData
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 取出4层 JSONObject 结构对应的信息转储到 m_jsonContactData 中。每次转储该种类的指定子类型的字段
    // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标；int iPhone : 0 非电话号码；1 电话号码
    private void dumpJson4layIm0(String idKey, String key1, Cursor cursor, int iPhone) {
        // type 为信息种类(大类型)，比如Phone.TYPE、Email.TYPE等
        String type = getMimetype4lay(key1, "__mimetype_type").trim();
        // type为 type 种类信息的子类型，比如Phone.TYPE大类型中的Phone.TYPE_HOME、Phone.TYPE_MOBILE等
        String subtype = cursor.getString(cursor.getColumnIndex(type)).trim();     // 取当前cursor对应的信息子类型

        try {
            Iterator<String> it = m_contactHeader.m_jsonHeader.getJSONObject(key1).keys();
            while (it.hasNext()) {
                String key2 = it.next();                                        // 获得key
                // 跳过前面的元素 "__mimetype_x"
                if (key2.length() > "__mimetype_".length() && key2.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                    //System.out.println("key2.substring(0, \"__mimetype_\".length()) = " + key2.substring(0, "__mimetype_".length()));
                    continue;
                }
                String subtype2 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first");

                if (subtype.equals(subtype2)) {
                    String col = getMimetype4lay(key1, "__mimetype_data").trim();// 获取该类信息的在数据表中的列号(字段号)，Phone.DATA等
                    int iCol = cursor.getColumnIndex(col);
                    String data = "";
                    if (iCol > -1) {
                        //E/CursorWindow: Failed to read row 0, column -1 from a CursorWindow which has 10 rows, 82 columns.
                        //String telNum = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "telNum")));
                        data = cursor.getString(iCol);          // 获取数据表中的数据
                        if (1 == iPhone) {
                            data = funRemove(data);             // 电话号码才处理
                        }
                    }
                    put2json4lay(idKey, key1, key2, data);      // 将获取的数据存入 m_jsonContactData
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 取出4层 JSONObject 结构对应的信息转储到 m_jsonContactData 中。每次转储该种类的指定子类型的字段
    // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标；int iPhone : 0 非电话号码；1 电话号码
    private void fun00_dumpJson4lay(String idKey, String key1, Cursor cursor, int iPhone) {
        // type 为信息种类(大类型)，比如Phone.TYPE、Email.TYPE等
        String type = getMimetype4lay(key1, "__mimetype_type").trim();
        // type为 type 种类信息的子类型，比如Phone.TYPE大类型中的Phone.TYPE_HOME、Phone.TYPE_MOBILE等
        //int phoneType = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));
        //"jsonG05Im"
        //System.out.println("type = " + type);
        //Attempt to invoke virtual method 'java.lang.String java.lang.String.trim()' on a null object reference
        String subtype = "";
        subtype = cursor.getString(cursor.getColumnIndex(type));     // 取当前cursor对应的信息子类型
        if (TextUtils.isEmpty(subtype)) {
            return; //取信息子类型失败，直接返回。避免App崩溃
        }
        subtype = subtype.trim();
        //String subtype = String.valueOf(cursor.getInt(cursor.getColumnIndex(type))); // 取当前cursor对应的信息子类型

        try {
            Iterator<String> it = m_contactHeader.m_jsonHeader.getJSONObject(key1).keys();
            while (it.hasNext()) {
                //key1 : jsonG01Phone、jsonG02Email、...
                //key2 : homeNum、mobile、__mimetype_x、...
                String key2 = it.next();                                        // 获得key
                // 跳过前面的元素 "__mimetype_x"
                if (key2.length() > "__mimetype_".length() && key2.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                    //System.out.println("key2.substring(0, \"__mimetype_\".length()) = " + key2.substring(0, "__mimetype_".length()));
                    continue;
                }
                //subtype : 当前cursor对应的信息子类型
                //subtype2 : 0、1、2、...
                String subtype2 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first");

                if (subtype.equals(subtype2)) {
                    // Phone 数据有三个字段存储：NUMBER 为 Phone 值；TYPE 为类型，当为自定义（TYPE_CUSTOM）时，LABEL 字段要写入用户自定义的类型
                    if (subtype.equals("0")) {    //"customData", 比如 Phone.TYPE_CUSTOM = 0;    //Put the actual subtype in LABEL
                        //subtype = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));// 取当前cursor对应的信息子类型
                        //{"__mimetype_label", Phone.LABEL},        //Phone.TYPE = "data3";
                        subtype = cursor.getString(cursor.getColumnIndex(getMimetype4lay(key1, "__mimetype_label").trim()));// 取当前cursor对应的信息子类型
                    }

                    String col = getMimetype4lay(key1, "__mimetype_data").trim();// 获取该类信息的在数据表中的列号(字段号)，Phone.DATA等
                    int iCol = cursor.getColumnIndex(col);
                    String data = "";

                    if (iCol > -1) {
                        //E/CursorWindow: Failed to read row 0, column -1 from a CursorWindow which has 10 rows, 82 columns.
                        //String telNum = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "telNum")));
                        data = cursor.getString(iCol);          // 获取数据表中的数据
                        if (1 == iPhone) {
                            data = funRemove(data);             // 电话号码才处理
                        }
                    }
                    put2json4lay(idKey, key1, key2, data);      // 将获取的数据存入 m_jsonContactData
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 取出4层 JSONObject 结构对应的所有信息转储到 m_jsonContactData 中，比如 jsonG00StructName
    // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标
    private void fun02_dumpJson4layAll(String idKey, String key1, Cursor cursor) {
        try {
            Iterator<String> it = m_contactHeader.m_jsonHeader.getJSONObject(key1).keys();
            while (it.hasNext()) {
                String key2 = it.next();                                    // 获得key
                // 跳过前面的元素 "__mimetype_x"
                if (key2.length() > "__mimetype_".length() && key2.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                    continue;
                }
                String col = get4layColumnName(key1, key2);                 // 获取该类信息的在数据表中的列号(字段号)
                String data = cursor.getString(cursor.getColumnIndex(col)); // 获取数据表中的数据
                put2json4lay(idKey, key1, key2, data);                      // 将获取的数据存入 m_jsonContactData
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //处理4层结构，mJsonG00到mJsonG06、mJsonG08
    private String get4layColumnName(String key1, String key2) {
        String col = "";
        try {
            //E/CursorWindow: Failed to read row 0, column -1 from a CursorWindow which has 10 rows, 82 columns.
            //return m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first");
            col = String.valueOf(m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first"));
        } catch (JSONException e) {
            e.printStackTrace();
            //return "";
        }
        return col;
    }

    //处理4层结构
    private void put2json4lay2(String idKey, String key1, String key2, String val, String prefix) {
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

    //处理4层结构，mJsonG00到mJsonG06、mJsonG08
    //由于Java是值传递，传递过大JSONObject参数，当手机有上千个联系人时，将导致资源占用过大而崩溃
    private void put2json4lay(String idKey, String key1, String key2, String val) {
        String keyNew = key2;
        try {
            int n = Integer.valueOf(m_contactHeaderCount.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__second"));
            n++;
            m_contactHeaderCount.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).put("__second", String.valueOf(n));
            if (n > 1) {
                keyNew += n;
            }
            m_jsonContactData.getJSONObject(idKey).put(keyNew, val);

            //下面两行好像无用
            n = java.lang.Math.max(n, Integer.valueOf(m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__second")));
            m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).put("__second", String.valueOf(n));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 取出5层 JSONObject 结构对应的信息转储到 m_jsonContactData 中。每次转储该种类的指定子类型的字段
    // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标
    private void dumpJson5lay(String idKey, String key1, Cursor cursor) {
        //key1 : m_contactHeader.jsonG04OrgSet，                    或者m_contactHeader.jsonG08PostalSet

        // category为信息种类(大类型)，比如Phone.TYPE、Email.TYPE等
        String category = getMimetype4lay(key1, "__mimetype_type").trim();
        // type为category种类信息的子类型，比如Phone.TYPE大类型中的Phone.TYPE_HOME、Phone.TYPE_MOBILE等
        //int phoneType = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));
        String type = cursor.getString(cursor.getColumnIndex(category)).trim();     // 取当前cursor对应的信息子类型
        //String type = String.valueOf(cursor.getInt(cursor.getColumnIndex(category))); // 取当前cursor对应的信息子类型
        //System.out.println("category.type = " + category + "." + type);

        try {
            //key2 : jsonG04_00WorkOrgType、jsonG04_01OtherOrgType， 或者jsonG08_00HomePostal、jsonG08_01WorkPostal、jsonG08_02OtherPostal
            Iterator<String> it = m_contactHeader.m_jsonHeader.getJSONObject(key1).keys();
            while (it.hasNext()) {
                String key2 = it.next();
                // 跳过前面的元素 "__mimetype_x"
                if (key2.length() > "__mimetype_".length() && key2.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                    continue;
                }
                Iterator<String> it2 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).keys();
                while (it2.hasNext()) {
                    String key3 = it2.next();                                   // 获的key
                    // 跳过前面的元素 "__mimetype_x"
                    if (key3.length() > "__mimetype_".length() && key3.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                        continue;
                    }

                    String type2 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).getString("__first");
                    if (type.equals(type2)) {
                        String col = getMimetype4lay(key1, "__mimetype_data").trim();// 获取该类信息的在数据表中的列号(字段号)，Phone.DATA等
                        int iCol = cursor.getColumnIndex(col);
                        String data = "";
                        if (iCol > -1) {
                            //E/CursorWindow: Failed to read row 0, column -1 from a CursorWindow which has 10 rows, 82 columns.
                            //String telNum = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "telNum")));
                            data = cursor.getString(iCol);          // 获取数据表中的数据
                        }
                        put2json5lay(idKey, key1, key2, key3, data);                // 将获取的数据存入 m_jsonContactData
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //处理5层结构应该多一层getJSONObject操作，mJsonG07、mJsonG09
    private String get5layColumnName(String key1, String key2, String key3) {
        try {
            //W/System.err: org.json.JSONException: Value vnd.android.cursor.item/im at __first of type java.lang.String cannot be converted to JSONObject
            return m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).getString("__first");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    //处理5层结构应该多一层getJSONObject操作，mJsonG07、mJsonG09
    //由于Java是值传递，传递过大JSONObject参数，当手机有上千个联系人时，将导致资源占用过大而崩溃
    private void put2json5lay(String idKey, String key1, String key2, String key3, String strVal) {
        String keyNew = key3;
        try {
            int n = Integer.valueOf(m_contactHeaderCount.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).getString("__second"));
            n++;
            m_contactHeaderCount.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).put("__second", String.valueOf(n));
            if (n > 1) {
                keyNew += n;
            }
            m_jsonContactData.getJSONObject(idKey).put(keyNew, strVal);

            n = java.lang.Math.max(n, Integer.valueOf(m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).getString("__second")));
            m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).put("__second", String.valueOf(n));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
}
