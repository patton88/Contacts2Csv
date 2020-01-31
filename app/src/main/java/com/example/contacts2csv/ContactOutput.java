//获取通讯录所有字段
//2012年01月02日 14:48:01 yemh111 阅读数：1368
package com.example.contacts2csv;
// 编译error: unmappable character for encoding UTF-8。
// 解决办法-OK，用文本编辑器打开文件，将文件另存为UTF-8格式
// 有警告，没关系：uses unchecked or unsafe operations. Recompile with -Xlint:unchecked for details.

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
import android.util.Pair;
import android.provider.ContactsContract.CommonDataKinds.Im;

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
        //2020-01-28 15:03:43.698 8739-8739/com.example.contacts2csv I/System.out: {displayName=data1, lastName=data2, firstName=data3, prefix=data4, middleName=data5, suffix=data6, phoneticLastName=data7, phoneticFirstName=data9, phoneticMiddleName=data8, homeNum=data1, mobile=data1, workNum=data1, workFax=data1, homeFax=data1, pager=data1, otherNum=data1, callbackNum=data1, carNum=data1, compMainTel=data1, isdn=data1, mainTel=data1, otherFax=data1, wirelessDev=data1, telegram=data1, tty_tdd=data1, workMobile=data1, workPager=data1, assistantNum=data1, mms=data1, homeEmail=data1, workEmail=data1, otherEmail=data1, mobileEmail=data1, anniversary=data1, otherday=data1, birthday=data1, homeMsg=data1, workMsg=data1, otherMsg=data1, customIm=data1, aimIm=data1, msnIm=data1, yahooIm=data1, skypeIm=data1, qqIm=data1, googleTalkIm=data1, icqIm=data1, jabberIm=data1, netmeetingIm=data1, remark=data1, defaultNickName=data1, otherNickName=data1, maindenNickName=data1, shortNickName=data1, initialsNickName=data1, workCompany=data1, workJobTitle=data4, workDepartment=data5, workJobDescription=data5, workSymbol=data5, workPhoneticName=data5, workOfficeLocation=data5, otherCompany=data1, otherJobTitle=data4, otherDepartment=data5, otherJobDescription=data5, otherSymbol=data5, otherPhoneticName=data5, otherOfficeLocation=data5, homepage=data1, blog=data1, profile=data1, home=data1, workPage=data1, ftpPage=data1, otherPage=data1, workFormattedAddress=data1, workStreet=data4, workBox=data5, workArea=data6, workCity=data7, workState=data8, workZip=data9, workCountry=data10, homeFormattedAddress=data1, homeStreet=data4, homeBox=data5, homeArea=data6, homeCity=data7, homeState=data8, homeZip=data9, homeCountry=data10, otherFormattedAddress=data1, otherStreet=data4, otherBox=data5, otherArea=data6, otherCity=data7, otherState=data8, otherZip=data9, otherCountry=data10}
    }

    public boolean outputAllContacts(Context context, String sPath) {
        String sContacts = getAllContacts();
        writeFile(sPath, sContacts);
        return true;
    }

    private void writeFile(String sPath, String str) {
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


            //m_contactHeader.jsonG00StructName，获得通讯录中联系人的名字。StructuredName.CONTENT_ITEM_TYPE
            if (sMimetype.equals(getMimetype4lay("jsonG00StructName", "__mimetype_0"))) {
                //dumpJsonG00StructName(contactIdKey, "jsonG00StructName", cursor);

                // 取出4层 JSONObject 结构对应的所有信息转储到 m_jsonContactData 中，比如StructuredName.CONTENT_ITEM_TYPE
                // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标
                //private void dumpJson4layAll(String idKey, String key1, Cursor cursor) {
                dumpJson4layAll(contactIdKey, "jsonG00StructName", cursor);
            }
            //m_contactHeader.jsonG01Phone，获取电话信息，共20项。Phone.CONTENT_ITEM_TYPE
            else if (sMimetype.equals(getMimetype4lay("jsonG01Phone", "__mimetype_0"))) {
                //dumpJsonG01Phone(contactIdKey, cursor);

                // 取出4层 JSONObject 结构对应的信息转储到 m_jsonContactData 中。每次转储该种类的指定子类型的字段
                // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标；int iPhone : 0 非电话号码；1 电话号码
                //private void dumpJson4lay(String idKey, String key1, Cursor cursor, int iPhone) {
                dumpJson4lay(contactIdKey, "jsonG01Phone", cursor, 1);
            }
            //m_contactHeader.jsonG02Email，查找Email地址。Email.CONTENT_ITEM_TYPE
            else if (sMimetype.equals(getMimetype4lay("jsonG02Email", "__mimetype_0"))) {
                //dumpJsonG02Email(contactIdKey, cursor);
                dumpJson4lay(contactIdKey, "jsonG02Email", cursor, 0);
            }
            //m_contactHeader.jsonG03Event，查找Event地址。Event.CONTENT_ITEM_TYPE
            else if (sMimetype.equals(getMimetype4lay("jsonG03Event", "__mimetype_0"))) {
                //dumpJsonG03Event(contactIdKey, cursor);
                dumpJson4lay(contactIdKey, "jsonG03Event", cursor, 0);
            }
            //m_contactHeader.jsonG04Im，即时消息。Im.CONTENT_ITEM_TYPE
            else if (sMimetype.equals(getMimetype4lay("jsonG04Im", "__mimetype_0"))) {
                //dumpJsonG04Im(contactIdKey, cursor);
                //System.out.println("sMimetype = " + sMimetype);
                dumpJson4layIm(contactIdKey, "jsonG04Im", cursor, 0);
            }
            //m_contactHeader.jsonG05Remark，获取备注信息。Note.CONTENT_ITEM_TYPE
            else if (sMimetype.equals(getMimetype4lay("jsonG05Remark", "__mimetype_0"))) {
                //dumpJsonG05Remark(contactIdKey, cursor);
                dumpJson4lay(contactIdKey, "jsonG05Remark", cursor, 0);
            }
            //m_contactHeader.jsonG06NickName，获取昵称信息。Nickname.CONTENT_ITEM_TYPE
            else if (sMimetype.equals(getMimetype4lay("jsonG06NickName", "__mimetype_0"))) {
                //dumpJsonG06NickName(contactIdKey, cursor);
                dumpJson4lay(contactIdKey, "jsonG06NickName", cursor, 0);
            }
            //1、第一类型有4层结构，mJsonG00到mJsonG06、mJsonG08
            //m_jsonHeader->jsonG00StructName->displayName-first
            //2、第二类型有5层结构，mJsonG07、mJsonG09
            //m_jsonHeader->jsonG07OrgType->jsonG07_00WorkOrgType->workCompany-first
            //m_contactHeader.jsonG07OrgType，获取组织信息。Organization.CONTENT_ITEM_TYPE
            else if (sMimetype.equals(getMimetype4lay("jsonG07OrgType", "__mimetype_0"))) {
                //dumpJsonG07OrgType(contactIdKey, cursor);

                // 取出5层 JSONObject 结构对应的信息转储到 m_jsonContactData 中。每次转储该种类的指定子类型的字段
                // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标
                //private void dumpJson5lay(String idKey, String key1, Cursor cursor) {
                dumpJson5lay(contactIdKey, "jsonG07OrgType", cursor);
            }
            //m_contactHeader.jsonG08WebType，获取网站信息。Website.CONTENT_ITEM_TYPE
            else if (sMimetype.equals(getMimetype4lay("jsonG08WebType", "__mimetype_0"))) {
                //dumpJsonG08WebType(contactIdKey, cursor);
                dumpJson4lay(contactIdKey, "jsonG08WebType", cursor, 0);
            }
            //m_contactHeader.jsonG09PostalType，查找通讯地址。StructuredPostal.CONTENT_ITEM_TYPE
            else if (sMimetype.equals(getMimetype4lay("jsonG09PostalType", "__mimetype_0"))) {
                //dumpJsonG09PostalType(contactIdKey, cursor);
                dumpJson5lay(contactIdKey, "jsonG09PostalType", cursor);
            }
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

    private String getMimetype4lay(String key1, String key2) {
        String mimetype = "";
        try {
            //mimetype = m_contactHeader.m_jsonHeader.getJSONObject(key1).getString(key2);
            //I/System.out: mimetype = {"__first":"vnd.android.cursor.item\/name","__second":"0"}
            mimetype = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first").trim();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //System.out.println("mimetype = " + mimetype);
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
    //m_jsonHeader->jsonG07OrgType->jsonG07_00WorkOrgType->workCompany->__first

    //                      ->it1、key1     ->it2、key2           ->it3、key3                 ->it4、key4
    //class ContactHeader  ->JSONObject    ->JSONObject          ->JSONObject                ->JSONObject    ->JSONObject
    //class ContactHeader  ->m_jsonHeader  ->jsonG07OrgType     ->jsonG07_00WorkOrgType    ->workCompany   ->__first
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
                         * key1: "jsonG07OrgType"
                         * key2: "jsonG07_00WorkOrgType"、"jsonG07_01OtherOrgType"、...
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
            //I/System.out: contact3 = {"displayName":"","lastName":"","firstName":"","prefix":"","middleName":"","suffix":"","phoneticLastName":"","phoneticFirstName":"","phoneticMiddleName":"","homeNum":"13944444444","mobile":"13655555555","mobile2":"","mobile3":"","mobile4":"","workNum":"","workNum2":"","workFax":"","homeFax":"","pager":"","otherNum":"","callbackNum":"","carNum":"","compMainTel":"","isdn":"","mainTel":"","otherFax":"","wirelessDev":"","telegram":"","tty_tdd":"","workMobile":"","workPager":"","assistantNum":"","mms":"","homeEmail":"","workEmail":"","otherEmail":"","mobileEmail":"","anniversary":"","otherday":"","birthday":"","homeMsg":"","workMsg":"","otherMsg":"","customIm":"","aimIm":"","msnIm":"","yahooIm":"","skypeIm":"","qqIm":"","googleTalkIm":"","icqIm":"","jabberIm":"","netmeetingIm":"","remark":"","defaultNickName":"","otherNickName":"","maindenNickName":"","shortNickName":"","initialsNickName":"","workCompany":"","workJobTitle":"","workDepartment":"","workJobDescription":"","workSymbol":"","workPhoneticName":"","workOfficeLocation":"","otherCompany":"","otherJobTitle":"","otherDepartment":"","otherJobDescription":"","otherSymbol":"","otherPhoneticName":"","otherOfficeLocation":"","homepage":"","blog":"","profile":"","home":"","workPage":"","ftpPage":"","otherPage":"","workFormattedAddress":"","workStreet":"","workBox":"","workArea":"","workCity":"","workState":"","workZip":"","workCountry":"","homeFormattedAddress":"","homeStreet":"","homeBox":"","homeArea":"","homeCity":"","homeState":"","homeZip":"","homeCountry":"","otherFormattedAddress":"","otherStreet":"","otherBox":"","otherArea":"","otherCity":"","otherState":"","otherZip":"","otherCountry":""}
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
    public String getKey(String val, String key1)
    {
        String key = "";
        try {
            JSONObject json = m_contactHeader.m_jsonHeader.getJSONObject(key1);
            Iterator<String> it = json.keys();
            while (it.hasNext()) {
                key = it.next();
                if(json.getString(key).equals(val)){
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return key;
    }

    // 取出4层 JSONObject 结构对应的信息转储到 m_jsonContactData 中。每次转储该种类的指定子类型的字段
    // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标；int iPhone : 0 非电话号码；1 电话号码
    private void dumpJson4layIm0(String idKey, String key1, Cursor cursor, int iPhone) {
        // 获取即时通讯消息
        int protocal = cursor.getInt(cursor.getColumnIndex(Im.PROTOCOL));
        if (Im.TYPE_CUSTOM == protocal) {
            String workMsg = cursor.getString(cursor.getColumnIndex(Im.DATA));
            put2json4lay(idKey, key1, getKey(String.valueOf(Im.TYPE_CUSTOM), key1), workMsg); // 将获取的数据存入 m_jsonContactData
        } else if (Im.PROTOCOL_MSN == protocal) {
            String workMsn = cursor.getString(cursor.getColumnIndex(Im.DATA));
            put2json4lay(idKey, key1, getKey(String.valueOf(Im.PROTOCOL_MSN), key1), workMsn); // 将获取的数据存入 m_jsonContactData
        }
        if (Im.PROTOCOL_QQ == protocal) {
            String instantsMsg = cursor.getString(cursor.getColumnIndex(Im.DATA));
            put2json4lay(idKey, key1, getKey(String.valueOf(Im.PROTOCOL_QQ), key1), instantsMsg); // 将获取的数据存入 m_jsonContactData
        }
    }

    // 取出4层 JSONObject 结构对应的信息转储到 m_jsonContactData 中。每次转储该种类的指定子类型的字段
    // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标；int iPhone : 0 非电话号码；1 电话号码
    private void dumpJson4layIm(String idKey, String key1, Cursor cursor, int iPhone) {
        // kind为信息种类(大类型)，比如Phone.TYPE、Email.TYPE等
        String kind = getMimetype4lay(key1, "__mimetype_2").trim();
        // type为kind种类信息的子类型，比如Phone.TYPE大类型中的Phone.TYPE_HOME、Phone.TYPE_MOBILE等
        String type = cursor.getString(cursor.getColumnIndex(kind)).trim();     // 取当前cursor对应的信息子类型

        // 获取即时通讯消息
        //String protocal = cursor.getString(cursor.getColumnIndex(kind)).trim();
        if (type.equals(String.valueOf(Im.TYPE_CUSTOM))) {
            String workMsg = cursor.getString(cursor.getColumnIndex(Im.DATA));
            put2json4lay(idKey, key1, getKey(String.valueOf(Im.TYPE_CUSTOM), key1), workMsg); // 将获取的数据存入 m_jsonContactData
        } else if (type.equals(String.valueOf(Im.PROTOCOL_MSN))) {
            String workMsn = cursor.getString(cursor.getColumnIndex(Im.DATA));
            put2json4lay(idKey, key1, getKey(String.valueOf(Im.PROTOCOL_MSN), key1), workMsn); // 将获取的数据存入 m_jsonContactData
        } else if (type.equals(String.valueOf(Im.PROTOCOL_QQ))) {
            String instantsMsg = cursor.getString(cursor.getColumnIndex(Im.DATA));
            put2json4lay(idKey, key1, getKey(String.valueOf(Im.PROTOCOL_QQ), key1), instantsMsg); // 将获取的数据存入 m_jsonContactData
        }
    }

    // 取出4层 JSONObject 结构对应的信息转储到 m_jsonContactData 中。每次转储该种类的指定子类型的字段
    // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标；int iPhone : 0 非电话号码；1 电话号码
    private void dumpJson4lay(String idKey, String key1, Cursor cursor, int iPhone) {
        // kind为信息种类(大类型)，比如Phone.TYPE、Email.TYPE等
        String kind = getMimetype4lay(key1, "__mimetype_2").trim();
        // type为kind种类信息的子类型，比如Phone.TYPE大类型中的Phone.TYPE_HOME、Phone.TYPE_MOBILE等
        //int phoneType = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));
        //"jsonG04Im"
        //System.out.println("kind = " + kind);
        String type = cursor.getString(cursor.getColumnIndex(kind)).trim();     // 取当前cursor对应的信息子类型
        //String type = String.valueOf(cursor.getInt(cursor.getColumnIndex(kind))); // 取当前cursor对应的信息子类型

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

                if (type.equals(type2)) {
                    String col = getMimetype4lay(key1, "__mimetype_1").trim();// 获取该类信息的在数据表中的列号(字段号)，Phone.DATA等
                    int iCol = cursor.getColumnIndex(col);
                    String data = "";
                    if (iCol > -1) {
                        //E/CursorWindow: Failed to read row 0, column -1 from a CursorWindow which has 10 rows, 82 columns.
                        //String homeNum = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "homeNum")));
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

    // 取出4层 JSONObject 结构对应的所有信息转储到 m_jsonContactData 中，比如StructuredName.CONTENT_ITEM_TYPE
    // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标
    private void dumpJson4layAll(String idKey, String key1, Cursor cursor) {
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
        //key1 : m_contactHeader.jsonG07OrgType，                    或者m_contactHeader.jsonG09PostalType

        // kind为信息种类(大类型)，比如Phone.TYPE、Email.TYPE等
        String kind = getMimetype4lay(key1, "__mimetype_2").trim();
        // type为kind种类信息的子类型，比如Phone.TYPE大类型中的Phone.TYPE_HOME、Phone.TYPE_MOBILE等
        //int phoneType = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));
        String type = cursor.getString(cursor.getColumnIndex(kind)).trim();     // 取当前cursor对应的信息子类型
        //String type = String.valueOf(cursor.getInt(cursor.getColumnIndex(kind))); // 取当前cursor对应的信息子类型
        System.out.println("kind.type = " + kind + "." + type);

        try {
            //key2 : jsonG07_00WorkOrgType、jsonG07_01OtherOrgType， 或者jsonG09_00HomePostal、jsonG09_01WorkPostal、jsonG09_02OtherPostal
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
                        String col = getMimetype4lay(key1, "__mimetype_1").trim();// 获取该类信息的在数据表中的列号(字段号)，Phone.DATA等
                        int iCol = cursor.getColumnIndex(col);
                        String data = "";
                        if (iCol > -1) {
                            //E/CursorWindow: Failed to read row 0, column -1 from a CursorWindow which has 10 rows, 82 columns.
                            //String homeNum = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "homeNum")));
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

}
