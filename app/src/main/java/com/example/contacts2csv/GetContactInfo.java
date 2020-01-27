//获取通讯录所有字段
//2012年01月02日 14:48:01 yemh111 阅读数：1368
package com.example.contacts2csv;
// 编译error: unmappable character for encoding UTF-8。
// 解决办法-OK，用文本编辑器打开文件，将文件另存为UTF-8格式
// 有警告，没关系：uses unchecked or unsafe operations. Recompile with -Xlint:unchecked for details.

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Data;
import android.util.Pair;

//import cn.ihope.mozyhome.domain.Contacts;

public class GetContactInfo {
    private Context m_context;                      //查询上下文
    private JSONObject m_jsonContactData;            //用于存放获取的所有记录中间数据
    private JSONObject m_jsonContactData2;           //用于存放获取的所有记录最终结果
    private ContactHeader m_contactHeader;         //用于存放通讯录所有记录的表头信息
    private ContactHeader m_contactHeaderCount;    //用于存放获取的每条记录每一列的计数器

    public int GetContactsSum() {
        return m_jsonContactData2.length();
    }

    public GetContactInfo(Context context) {
        this.m_context = context;
        m_contactHeader = new ContactHeader();
        m_contactHeader.init();
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

    public String getAllContact() {
        // 获得通讯录信息 ，URI是ContactsContract.Contacts.CONTENT_URI
        m_jsonContactData = new JSONObject(new LinkedHashMap());  //解决JsonObject数据固定顺序
        String sMimetype = "";
        int iOldId = -11;
        int iContactId = -11;
        String contactIdKey = "";
        Cursor cursor = m_context.getContentResolver().query(Data.CONTENT_URI, null, null, null, Data.RAW_CONTACT_ID);

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
                m_contactHeaderCount.init();

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

            //m_contactHeader.jsonG00StructName，获得通讯录中联系人的名字
            if (StructuredName.CONTENT_ITEM_TYPE.equals(sMimetype)) {
                dumpJsonG00StructName(contactIdKey, cursor);
            }

            //m_contactHeader.jsonG01Phone，获取电话信息，共20项
            if (Phone.CONTENT_ITEM_TYPE.equals(sMimetype)) {
                dumpJsonG01Phone(contactIdKey, cursor);
            }

            //m_contactHeader.jsonG02Email，查找Email地址
            if (Email.CONTENT_ITEM_TYPE.equals(sMimetype)) {
                dumpJsonG02Email(contactIdKey, cursor);
            }

            //m_contactHeader.jsonG03Event，查找Event地址
            if (Event.CONTENT_ITEM_TYPE.equals(sMimetype)) {
                dumpJsonG03Event(contactIdKey, cursor);
            }

            //m_contactHeader.jsonG04Im，即时消息
            if (Im.CONTENT_ITEM_TYPE.equals(sMimetype)) {
                dumpJsonG04Im(contactIdKey, cursor);
            }

            //m_contactHeader.jsonG05Remark，获取备注信息
            if (Note.CONTENT_ITEM_TYPE.equals(sMimetype)) {
                dumpJsonG05Remark(contactIdKey, cursor);
            }

            //m_contactHeader.jsonG06NickName，获取昵称信息
            if (Nickname.CONTENT_ITEM_TYPE.equals(sMimetype)) {
                dumpJsonG06NickName(contactIdKey, cursor);
            }

            //1、第一类型有4层结构，mJsonG00到mJsonG06、mJsonG08
            //m_jsonHeader->jsonG00StructName->displayName-first
            //2、第二类型有5层结构，mJsonG07、mJsonG09
            //m_jsonHeader->jsonG07OrgType->jsonG07_00WorkOrgType->workCompany-first

            //m_contactHeader.jsonG07OrgType，获取组织信息
            if (Organization.CONTENT_ITEM_TYPE.equals(sMimetype)) {
                dumpJsonG07OrgType(contactIdKey, cursor);
            }

            //m_contactHeader.jsonG08WebType，获取网站信息
            if (Website.CONTENT_ITEM_TYPE.equals(sMimetype)) {
                dumpJsonG08WebType(contactIdKey, cursor);
            }

            //m_contactHeader.jsonG09PostalType，查找通讯地址
            if (StructuredPostal.CONTENT_ITEM_TYPE.equals(sMimetype)) {
                dumpJsonG09PostalType(contactIdKey, cursor);
            }
        }
        cursor.close();

        //由于mContactsHeader中联系人的某种数据(比如mobile手机号)的最大值可能会不断增加，导致mJsonResult中数据长短不一
        //所以，最后再以mContactsHeader中各种数据大小的最终值为标准，再次填充将mJsonContactData.mJsonResult的所有字段填充到mJsonContactData2.mJsonResult中
        //private JSONObject m_jsonContactData2;        //用于存放获取的所有记录
        m_jsonContactData2 = new JSONObject(new LinkedHashMap());  //解决JsonObject数据固定顺序
        dumpJsonContactData(m_jsonContactData, m_jsonContactData2);

        return traverseJSON5(m_jsonContactData2);
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
                    String key2 = it2.next();   //key2: "displayName"、"lastName"、..
                    Iterator<String> it3 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).keys();
                    String key3 = it3.next();
                    //处理第一类型有4层结构，mJsonG00到mJsonG06、mJsonG08
                    if ("__first" == key3) {
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
        for(int i = 0; i <= n; i++){
            String keyNew = keyField;
            if(i > 1){
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

    //处理第一类型有4层结构，mJsonG00到mJsonG06、mJsonG08
    //由于Java是值传递，传递过大JSONObject参数，当手机有上千个联系人时，将导致资源占用过大而崩溃
    private void put2json(String idKey, String key1, String key2, String strVal) {
        String keyNew = key2;
        try {
            int n = Integer.valueOf(m_contactHeaderCount.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__second"));
            n++;
            m_contactHeaderCount.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).put("__second", String.valueOf(n));
            if(n > 1){
                keyNew += n;
            }
            m_jsonContactData.getJSONObject(idKey).put(keyNew, strVal);

            n = java.lang.Math.max(n, Integer.valueOf(m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__second")));
            m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).put("__second", String.valueOf(n));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //处理第二类型有5层结构应该多一层getJSONObject操作，mJsonG07、mJsonG09
    //由于Java是值传递，传递过大JSONObject参数，当手机有上千个联系人时，将导致资源占用过大而崩溃
    private void put2json(String idKey, String key1, String key2, String key3, String strVal) {
        String keyNew = key3;
        try {
            int n = Integer.valueOf(m_contactHeaderCount.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).getString("__second"));
            n++;
            m_contactHeaderCount.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).put("__second", String.valueOf(n));
            if(n > 1){
                keyNew += n;
            }
            m_jsonContactData.getJSONObject(idKey).put(keyNew, strVal);

            n = java.lang.Math.max(n, Integer.valueOf(m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).getString("__second")));
            m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).put("__second", String.valueOf(n));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //这里放入的jsonObject是一个对象(引用或指针)，放了之后还可以进行操作
    //m_jsonContactData.put("contact" + mIntSum, mJsonResult);
    //mIntSum++;
    //...
    //return traverseJSON5(m_jsonContactData);
    //JSON(JavaScript Object Notation，JavaScript对象容器)
    public String traverseJSON5(JSONObject json) {
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
                pair = traverseJSON3(json.getJSONObject(key), pair.first);
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
    public Pair<String, String> traverseJSON3(JSONObject json, String keys) {
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

    //处理第一类型有4层结构，mJsonG00到mJsonG06、mJsonG08
    private String getColumnName(String key1, String key2) {
        try {
            return m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    //处理第二类型有5层结构应该多一层getJSONObject操作，mJsonG07、mJsonG09
    private String getColumnName(String key1, String key2, String key3) {
        try {
            return m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getJSONObject(key3).getString("__first");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //转储联系人各个字段数据的函数组 Begin

    //m_contactHeader.jsonG00StructName，获得通讯录中联系人的名字
    private void dumpJsonG00StructName(String idKey, Cursor cursor) {
        //if (StructuredName.CONTENT_ITEM_TYPE.equals(mimetype)) {
        try {
        Iterator<String> it = m_contactHeader.m_jsonHeader.getJSONObject("jsonG00StructName").keys();
            while (it.hasNext()) {
                String key2 = it.next();
                String str = getColumnName("jsonG00StructName", key2);
                put2json(idKey, "jsonG00StructName", key2, cursor.getString(cursor.getColumnIndex(str)));
                //int n = Integer.valueOf(m_contactHeader.m_jsonHeader.getJSONObject("jsonG00StructName").getJSONObject(key).getString("__second"));
                //System.out.println("jsonG00StructName." + key + ".__second = " + n);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //m_contactHeader.jsonG01Phone，获取电话信息，共20项
    private void dumpJsonG01Phone(String idKey, Cursor cursor) {
        //if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
        // 取出电话类型
        int phoneType = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));

        // 1、住宅电话
        if (phoneType == Phone.TYPE_HOME) {
            String homeNum = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "homeNum")));
            homeNum = funRemove(homeNum);
            //System.out.println(homeNum);
            put2json(idKey, "jsonG01Phone", "homeNum", homeNum);
            //System.out.println("homeNum" + mJsonObject.getString("homeNum"));
        }

        // 2、手机
        if (phoneType == Phone.TYPE_MOBILE) {
            String mobile = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "mobile")));
            mobile = funRemove(mobile);
            put2json(idKey, "jsonG01Phone", "mobile", mobile);
            //System.out.println("mobile" + mJsonObject.getString("mobile"));
            //System.out.println("mJsonObject : " + mJsonObject.toString());
        }

        // 3、单位电话
        if (phoneType == Phone.TYPE_WORK) {
            String jobNum = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "workNum")));
            jobNum = funRemove(jobNum);
            put2json(idKey, "jsonG01Phone", "workNum", jobNum);
            //System.out.println("mJsonObject : " + mJsonObject.toString());
        }

        // 4、单位传真
        if (phoneType == Phone.TYPE_FAX_WORK) {
            String workFax = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "workFax")));
            put2json(idKey, "jsonG01Phone", "workFax", workFax);
        }

        // 5、住宅传真
        if (phoneType == Phone.TYPE_FAX_HOME) {
            String homeFax = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "homeFax")));
            put2json(idKey, "jsonG01Phone", "homeFax", homeFax);
        }

        // 6、寻呼机
        if (phoneType == Phone.TYPE_PAGER) {
            String pager = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "pager")));
            put2json(idKey, "jsonG01Phone", "pager", pager);
        }

        // 7、Other电话
        if (phoneType == Phone.TYPE_OTHER) {
            String otherNum = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "otherNum")));
            otherNum = funRemove(otherNum);
            //System.out.println(otherNum);
            put2json(idKey, "jsonG01Phone", "otherNum", otherNum);
            //System.out.println("otherNum" + mJsonObject.getString("otherNum"));
        }

        // 8、回拨号码
        if (phoneType == Phone.TYPE_CALLBACK) {
            String quickNum = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "callbackNum")));
            put2json(idKey, "jsonG01Phone", "callbackNum", quickNum);
        }

        // 9、车载电话
        if (phoneType == Phone.TYPE_CAR) {
            String carNum = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "carNum")));
            put2json(idKey, "jsonG01Phone", "carNum", carNum);
        }

        // 10、公司总机
        if (phoneType == Phone.TYPE_COMPANY_MAIN) {
            String jobTel = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "compMainTel")));
            put2json(idKey, "jsonG01Phone", "compMainTel", jobTel);
        }

        // 11、ISDN
        if (phoneType == Phone.TYPE_ISDN) {
            String isdn = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "isdn")));
            put2json(idKey, "jsonG01Phone", "isdn", isdn);
        }

        // 12、总机
        if (phoneType == Phone.TYPE_MAIN) {
            String tel = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "mainTel")));
            put2json(idKey, "jsonG01Phone", "mainTel", tel);
        }

        // 13、Other传真
        if (phoneType == Phone.TYPE_OTHER_FAX ) {
            String homeFax = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "otherFax")));
            put2json(idKey, "jsonG01Phone", "otherFax", homeFax);
        }

        // 14、无线装置
        if (phoneType == Phone.TYPE_RADIO) {
            String wirelessDev = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "wirelessDev")));
            put2json(idKey, "jsonG01Phone", "wirelessDev", wirelessDev);
        }

        // 15、电报
        if (phoneType == Phone.TYPE_TELEX) {
            String telegram = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "telegram")));
            put2json(idKey, "jsonG01Phone", "telegram", telegram);
        }

        // 16、TTY_TDD
        if (phoneType == Phone.TYPE_TTY_TDD) {
            String tty_tdd = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "tty_tdd")));
            put2json(idKey, "jsonG01Phone", "tty_tdd", tty_tdd);
        }

        // 17、单位手机
        if (phoneType == Phone.TYPE_WORK_MOBILE) {
            String jobMobile = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "workMobile")));
            jobMobile = funRemove(jobMobile);
            put2json(idKey, "jsonG01Phone", "workMobile", jobMobile);
        }

        // 18、单位寻呼机
        if (phoneType == Phone.TYPE_WORK_PAGER) {
            String jobPager = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "workPager")));
            put2json(idKey, "jsonG01Phone", "workPager", jobPager);
        }

        // 19、助理
        if (phoneType == Phone.TYPE_ASSISTANT) {
            String assistantNum = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "assistantNum")));
            put2json(idKey, "jsonG01Phone", "assistantNum", assistantNum);
        }

        // 20、彩信
        if (phoneType == Phone.TYPE_MMS) {
            String mms = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG01Phone", "mms")));
            put2json(idKey, "jsonG01Phone", "mms", mms);
        }
    }

    //m_contactHeader.jsonG02Email，查找Email地址
    private void dumpJsonG02Email(String idKey, Cursor cursor) {
        //if (Email.CONTENT_ITEM_TYPE.equals(mimetype)) {
        // 取出邮件类型
        int emailType = cursor.getInt(cursor.getColumnIndex(Email.TYPE));

        if (emailType == Email.TYPE_HOME) {         // 住宅邮件地址，TYPE_HOME = 1;
            String homeEmail = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG02Email", "homeEmail")));
            put2json(idKey, "jsonG02Email", "homeEmail", homeEmail);
        }
        else if (emailType == Email.TYPE_WORK) {    // 单位邮件地址，TYPE_WORK = 2;
            String jobEmail = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG02Email", "workEmail")));
            put2json(idKey, "jsonG02Email", "workEmail", jobEmail);
        }
        else if (emailType == Email.TYPE_OTHER) {    // 单位邮件地址，TYPE_OTHER = 3;
            String jobEmail = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG02Email", "otherEmail")));
            put2json(idKey, "jsonG02Email", "otherEmail", jobEmail);
        }
        else if (emailType == Email.TYPE_MOBILE) {  // 手机邮件地址，TYPE_MOBILE = 4;
            String mobileEmail = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG02Email", "mobileEmail")));
            put2json(idKey, "jsonG02Email", "mobileEmail", mobileEmail);
        }
    }

    //m_contactHeader.jsonG03Event，查找Event地址
    private void dumpJsonG03Event(String idKey, Cursor cursor) {
        //if (Event.CONTENT_ITEM_TYPE.equals(mimetype)) {
        // 取出时间类型
        int eventType = cursor.getInt(cursor.getColumnIndex(Event.TYPE));

        // 周年纪念日，TYPE_ANNIVERSARY = 1;
        if (eventType == Event.TYPE_ANNIVERSARY) {
            String anniversary = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG03Event", "anniversary")));
            put2json(idKey, "jsonG03Event", "anniversary", anniversary);
        }
        // 其他日子，TYPE_OTHER = 2;
        else if (eventType == Event.TYPE_OTHER) {
            String otherday = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG03Event", "otherday")));
            put2json(idKey, "jsonG03Event", "otherday", otherday);
        }
        // 生日，TYPE_BIRTHDAY = 3;
        else if (eventType == Event.TYPE_BIRTHDAY) {
            String birthday = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG03Event", "birthday")));
            put2json(idKey, "jsonG03Event", "birthday", birthday);
        }
    }

    //m_contactHeader.jsonG04Im，即时消息
    private void dumpJsonG04Im(String idKey, Cursor cursor) {
        //if (Im.CONTENT_ITEM_TYPE.equals(mimetype)) {
        // 取出即时消息类型
        int protocal = cursor.getInt(cursor.getColumnIndex(Im.PROTOCOL));

        if (Im.TYPE_HOME == protocal) {
            String homeMsg = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG04Im", "homeMsg")));
            put2json(idKey, "jsonG04Im", "homeMsg", homeMsg);
        }
        else if (Im.TYPE_WORK == protocal) {
            String workMsg = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG04Im", "workMsg")));
            put2json(idKey, "jsonG04Im", "workMsg", workMsg);
        }
        else if (Im.TYPE_OTHER == protocal) {
            String otherMsg = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG04Im", "otherMsg")));
            put2json(idKey, "jsonG04Im", "otherMsg", otherMsg);
        }
        else if (Im.PROTOCOL_CUSTOM == protocal) {
            String customIm = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG04Im", "customIm")));
            put2json(idKey, "jsonG04Im", "customIm", customIm);
        }
        else if (Im.PROTOCOL_AIM == protocal) {
            String aimIm = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG04Im", "aimIm")));
            put2json(idKey, "jsonG04Im", "aimIm", aimIm);
        }
        else if (Im.PROTOCOL_MSN == protocal) {
            String msnIm = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG04Im", "msnIm")));
            put2json(idKey, "jsonG04Im", "msnIm", msnIm);
        }
        else if (Im.PROTOCOL_YAHOO == protocal) {
            String yahooIm = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG04Im", "yahooIm")));
            put2json(idKey, "jsonG04Im", "yahooIm", yahooIm);
        }
        else if (Im.PROTOCOL_SKYPE == protocal) {
            String skypeIm = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG04Im", "skypeIm")));
            put2json(idKey, "jsonG04Im", "skypeIm", skypeIm);
        }
        else if (Im.PROTOCOL_QQ == protocal) {
            String qqIm = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG04Im", "qqIm")));
            put2json(idKey, "jsonG04Im", "qqIm", qqIm);
        }
        else if (Im.PROTOCOL_GOOGLE_TALK == protocal) {
            String googleTalkIm = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG04Im", "googleTalkIm")));
            put2json(idKey, "jsonG04Im", "googleTalkIm", googleTalkIm);
        }
        else if (Im.PROTOCOL_ICQ == protocal) {
            String icqIm = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG04Im", "icqIm")));
            put2json(idKey, "jsonG04Im", "icqIm", icqIm);
        }
        else if (Im.PROTOCOL_JABBER == protocal) {
            String jabberIm = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG04Im", "jabberIm")));
            put2json(idKey, "jsonG04Im", "jabberIm", jabberIm);
        }
        else if (Im.PROTOCOL_NETMEETING == protocal) {
            String netmeetingIm = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG04Im", "netmeetingIm")));
            put2json(idKey, "jsonG04Im", "netmeetingIm", netmeetingIm);
        }
    }

    //m_contactHeader.jsonG05Remark，获取备注信息
    private void dumpJsonG05Remark(String idKey, Cursor cursor) {
        //if (Note.CONTENT_ITEM_TYPE.equals(mimetype)) {
        String remark = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG05Remark", "remark")));
        put2json(idKey, "jsonG05Remark", "remark", remark);
    }

    //m_contactHeader.jsonG06NickName，获取昵称信息
    private void dumpJsonG06NickName(String idKey, Cursor cursor) {
        //if (Nickname.CONTENT_ITEM_TYPE.equals(mimetype)) {
        // 取出昵称类型
        int nickType = cursor.getInt(cursor.getColumnIndex(Nickname.NAME));

        if (Nickname.TYPE_DEFAULT == nickType) {
            String defaultNickName = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG06NickName", "defaultNickName")));
            put2json(idKey, "jsonG06NickName", "defaultNickName", defaultNickName);
        }
        else if (Nickname.TYPE_OTHER_NAME == nickType) {
            String otherNickName = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG06NickName", "otherNickName")));
            put2json(idKey, "jsonG06NickName", "otherNickName", otherNickName);
        }
        else if (Nickname.TYPE_MAIDEN_NAME == nickType) {  // /** @TYPE_MAINDEN_NAME deprecated Use TYPE_MAIDEN_NAME instead. */
            String maindenNickName = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG06NickName", "maindenNickName")));
            put2json(idKey, "jsonG06NickName", "maindenNickName", maindenNickName);
        }
        else if (Nickname.TYPE_SHORT_NAME == nickType) {
            String shortNickName = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG06NickName", "shortNickName")));
            put2json(idKey, "jsonG06NickName", "shortNickName", shortNickName);
        }
        else if (Nickname.TYPE_INITIALS == nickType) {
            String initialsNickName = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG06NickName", "initialsNickName")));
            put2json(idKey, "jsonG06NickName", "initialsNickName", initialsNickName);
        }
    }


    //m_contactHeader.jsonG07OrgType，获取组织信息
    private void dumpJsonG07OrgType(String idKey, Cursor cursor) {
        //if (Organization.CONTENT_ITEM_TYPE.equals(mimetype)) {
        // 取出组织类型
        int orgType = cursor.getInt(cursor.getColumnIndex(Organization.TYPE));

        try {
            //m_contactHeader.jsonG07_00WorkOrgType，单位组织信息，TYPE_WORK = 1;
            //处理第二类型有5层结构应该多一层getJSONObject操作，mJsonG07、mJsonG09
            if (orgType == Organization.TYPE_WORK) {
                Iterator<String> it = m_contactHeader.m_jsonHeader.getJSONObject("jsonG07OrgType").getJSONObject("jsonG07_00WorkOrgType").keys();
                while (it.hasNext()) {
                    String key2 = it.next();
                    String str = getColumnName("jsonG07OrgType", "jsonG07_00WorkOrgType", key2);
                    put2json(idKey, "jsonG07OrgType", "jsonG07_00WorkOrgType", key2, cursor.getString(cursor.getColumnIndex(str)));
                }
            }
            //处理第二类型有5层结构应该多一层getJSONObject操作，mJsonG07、mJsonG09
            //m_contactHeader.jsonG07_01OtherOrgType，其他组织信息，TYPE_OTHER = 2;
            else if (orgType == Organization.TYPE_OTHER) {
                Iterator<String> it = m_contactHeader.m_jsonHeader.getJSONObject("jsonG07OrgType").getJSONObject("jsonG07_01OtherOrgType").keys();
                while (it.hasNext()) {
                    String key2 = it.next();
                    String str = getColumnName("jsonG07OrgType", "jsonG07_01OtherOrgType", key2);
                    put2json(idKey, "jsonG07OrgType", "jsonG07_01OtherOrgType", key2, cursor.getString(cursor.getColumnIndex(str)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //m_contactHeader.jsonG08WebType，获取网站信息
    private void dumpJsonG08WebType(String idKey, Cursor cursor) {
        //if (Website.CONTENT_ITEM_TYPE.equals(mimetype)) {
        // 取出组织类型
        int webType = cursor.getInt(cursor.getColumnIndex(Website.TYPE));

        // 主页，TYPE_HOMEPAGE = 1;
        if (webType == Website.TYPE_CUSTOM) {
            String homepage = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG08WebType", "homepage")));
            put2json(idKey, "jsonG08WebType", "homepage", homepage);
        }
        // 博客，TYPE_BLOG = 2;
        else if (webType == Website.TYPE_BLOG) {
            String blog = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG08WebType", "blog")));
            put2json(idKey, "jsonG08WebType", "blog", blog);
        }
        // 个人主页，TYPE_PROFILE = 3;
        else if (webType == Website.TYPE_HOMEPAGE) {
            String profile = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG08WebType", "profile")));
            put2json(idKey, "jsonG08WebType", "profile", profile);
        }
        // 家庭主页，TYPE_HOME = 4;
        else if (webType == Website.TYPE_HOMEPAGE) {
            String home = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG08WebType", "home")));
            put2json(idKey, "jsonG08WebType", "home", home);
        }
        // 工作主页，TYPE_WORK = 5;
        else if (webType == Website.TYPE_WORK) {
            String workPage = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG08WebType", "workPage")));
            put2json(idKey, "jsonG08WebType", "workPage", workPage);
        }
        // ftp主页，TYPE_FTP = 6;
        else if (webType == Website.TYPE_WORK) {
            String ftpPage = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG08WebType", "ftpPage")));
            put2json(idKey, "jsonG08WebType", "ftpPage", ftpPage);
        }
        // 其他主页，TYPE_OTHER = 7;
        else if (webType == Website.TYPE_WORK) {
            String otherPage = cursor.getString(cursor.getColumnIndex(getColumnName("jsonG08WebType", "otherPage")));
            put2json(idKey, "jsonG08WebType", "otherPage", otherPage);
        }
    }

    //m_contactHeader.jsonG09PostalType，查找通讯地址
    private void dumpJsonG09PostalType(String idKey, Cursor cursor) {
        //if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimetype)) {
        // 取出通讯地址类型
        int postalType = cursor.getInt(cursor.getColumnIndex(StructuredPostal.TYPE));

        try {
            //处理第二类型有5层结构应该多一层getJSONObject操作，mJsonG07、mJsonG09
            //m_contactHeader.jsonG09_00WorkPostal，单位通讯地址，TYPE_HOME = 1;
            if (postalType == StructuredPostal.TYPE_WORK) {
                Iterator<String> it = null;
                it = m_contactHeader.m_jsonHeader.getJSONObject("jsonG09PostalType").getJSONObject("jsonG09_00WorkPostal").keys();
                while (it.hasNext()) {
                    String key2 = it.next();
                    String str = getColumnName("jsonG09PostalType", "jsonG09_00WorkPostal", key2);
                    put2json(idKey, "jsonG09PostalType", "jsonG09_00WorkPostal", key2, cursor.getString(cursor.getColumnIndex(str)));
                }
            }

            //处理第二类型有5层结构应该多一层getJSONObject操作，mJsonG07、mJsonG09
            //m_contactHeader.jsonG09_01HomePostal，住宅通讯地址，TYPE_WORK = 2;
            if (postalType == StructuredPostal.TYPE_HOME) {
                //E/ContactOutputTool: Error in outputContacts No value for m_contactHeader.jsonG09_01HomePostal
                //JSONObject遍历
                Iterator<String> it = m_contactHeader.m_jsonHeader.getJSONObject("jsonG09PostalType").getJSONObject("jsonG09_01HomePostal").keys();
                while (it.hasNext()) {
                    String key2 = it.next();
                    String str = getColumnName("jsonG09PostalType", "jsonG09_01HomePostal", key2);
                    put2json(idKey, "jsonG09PostalType", "jsonG09_01HomePostal", key2, cursor.getString(cursor.getColumnIndex(str)));
                }
            }

            //处理第二类型有5层结构应该多一层getJSONObject操作，mJsonG07、mJsonG09
            //m_contactHeader.jsonG09_02OtherPostal，其他通讯地址，TYPE_OTHER = 3;
            if (postalType == StructuredPostal.TYPE_OTHER) {
                Iterator<String> it = m_contactHeader.m_jsonHeader.getJSONObject("jsonG09PostalType").getJSONObject("jsonG09_02OtherPostal").keys();
                while (it.hasNext()) {
                    String key2 = it.next();
                    String str = getColumnName("jsonG09PostalType", "jsonG09_02OtherPostal", key2);
                    put2json(idKey, "jsonG09PostalType", "jsonG09_02OtherPostal", key2, cursor.getString(cursor.getColumnIndex(str)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //转储联系人各个字段数据的函数组 End
    ////////////////////////////////////////////////////////////////////////

}

/*
mobile,prefix,firstName,middleName,lastname,suffix,phoneticFirstName,phoneticMiddleName,phoneticLastName,homeNum,jobNum,workFax,homeFax,pager,quickNum,jobTel,carNum,isdn,tel,wirelessDev,telegram,tty_tdd,jobMobile,jobPager,assistantNum,mms,homeEmail,jobEmail,mobileEmail,birthday,anniversary,workMsg,instantsMsg,remark,nickName,company,jobTitle,department,home,homePage,workPage,street,ciry,box,area,state,zip,country,homeStreet,homeCity,homeBox,homeArea,homeState,homeZip,homeCountry,otherStreet,otherCity,otherBox,otherArea,otherState,otherZip,otherCountry
13323489735,,,,Zhangsan,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
13586473317,,,,Lisi,,,,,15986472331,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
*/