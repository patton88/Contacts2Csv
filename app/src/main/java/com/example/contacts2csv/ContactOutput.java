//获取通讯录所有字段
//2012年01月02日 14:48:01 yemh111 阅读数：1368
package com.example.contacts2csv;
// 编译error: unmappable character for encoding UTF-8。
// 解决办法-OK，用文本编辑器打开文件，将文件另存为UTF-8格式
// 有警告，没关系：uses unchecked or unsafe operations. Recompile with -Xlint:unchecked for details.

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.Pair;
import android.provider.ContactsContract.CommonDataKinds.Photo;

import static android.provider.ContactsContract.RawContacts;
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

    public JSONObject m_jsonContactOne;            //用于存放获取的单条记录

    private JSONObject m_jsonContactData1;         //用于存放获取的所有记录中间数据
    private JSONObject m_jsonContactData2;         // 存放处理同名聚合后的中间变量
    private JSONObject m_jsonContactData3;         //用于存放获取的所有记录最终结果
    private ContactHeader m_contactHeader;         //用于存放通讯录所有记录的表头信息
    private ContactHeader m_contactHeaderCount;    //用于存放获取的每条记录每一列的计数器

    private int m_iSum;
    private int m_iSuccessCount;
    private int m_iFailCount;
    private long m_lStartTimer;
    int m_iAnonymous;  // 无名用户 anonymous 计数器

    public ContactOutput() {
        m_GroupOutput = new GroupOutput();
        m_contactHeader = new ContactHeader();      //用于存放通讯录所有记录的表头信息
    }

    public String getCurTime() {
        int time = (int) ((SystemClock.elapsedRealtime() - m_lStartTimer) / 1000);
        //String hh = new DecimalFormat("00").format(time / 3600);
        String mm = new DecimalFormat("00").format(time % 3600 / 60);
        String ss = new DecimalFormat("00").format(time % 60);
        //String timeFormat = new String(hh + ":" + mm + ":" + ss);
        String timeFormat = new String(mm + "分" + ss + "秒");

        return timeFormat;
    }

    //导出导入联系人时，处理组信息的方式
    //导出联系人时，先导出组信息到 Groups_xxx.txt，不用管该组有多少组成员；然后再导出全部联系人，包含联系人属于哪些组的信息
    //导入联系人时，根据属于哪些组的信息，判断这些组是否存在、不存在便创建，然后将该联系人加入这些组
    public boolean outputAllContacts(Context context, String sPath) {
        // 1、先导出群组信息到 Groups_xxx.txt，不用管该组有多少组成员
        String sGroups = m_GroupOutput.getContactsGroups2();    //获得群组信息
        m_GroupOutput.saveGroupinfo2File(sGroups);              //将群组信息写入文件

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
        m_iAnonymous = 0;  // 无名用户 anonymous 计数器
        m_jsonContactData1 = new JSONObject(new LinkedHashMap());  //解决JsonObject数据固定顺序
        m_contactHeader = new ContactHeader();      //用于存放通讯录所有记录的表头信息

        // 注意：mimetype 都尽量使用 Android 规范命名，避免使用硬字符串，以增强适应性
        //RawContacts.CONTENT_URI = "content://com.android.contacts/raw_contacts"
        //Data.CONTENT_URI = "content://com.android.contacts/data"
        //Data.CONTACT_ID = "contact_id";
        //Data.MIMETYPE = "mimetype";   // 注意：在 data 表中查询不到"mimetype_id"字段，只能查询到"mimetype"字段

        // 查询联系人 "raw_contacts" 表，具体查询表中的 "contact_id" 字段。
        ContentResolver resolver = m_MA.getContentResolver();
        Uri uriId = RawContacts.CONTENT_URI;
        String[] selectId = new String[]{Data.CONTACT_ID};
        String whereId = Data.CONTACT_ID + " != ?";             // 选区(查询范围)，不等于某个值
        String whereArgsId[] = new String[]{"null"};            // 选择条件数组，只需要非空记录
        String sortOrderId = Data.CONTACT_ID;                   // 查询结果排序规则
        Cursor cursorId = resolver.query(uriId, selectId, whereId, whereArgsId, sortOrderId);

        m_iSum = cursorId.getCount();                       // 导出联系人总数
        m_lStartTimer = SystemClock.elapsedRealtime();      // 计时器起始时间
        m_iSuccessCount = 0;
        m_iFailCount = 0;

        m_Fun.logString(m_iSum);

        while (cursorId.moveToNext()) {
            //m_Fun.logFileds(cursorId);
            int iColId = cursorId.getColumnIndex(sortOrderId);  //"contact_id"
            String contactId = cursorId.getString(iColId);

            String contactIdKey = "contact" + contactId;
            m_Fun.logString(contactIdKey);

            m_contactHeaderCount = new ContactHeader();         //用于存放获取的每条记录每一列的计数器
            try {
                m_jsonContactData1.put(contactIdKey, new JSONObject(new LinkedHashMap()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // 查询联系人 data 表，具体查询表中的3个字段
            //raw_contact_id		联系人ID，iContactId
            //mimetype_id		    数据类型，MimeTypeId
            //data1			        数据，data
            Uri uriData = Data.CONTENT_URI;                                 // 查询表的 Uri 路径
            // 注意：在 data 表中查询不到"mimetype_id"字段，只能查询到"mimetype"字段
            String[] selectData = new String[]{Data.MIMETYPE, Data.DATA1, Data.RAW_CONTACT_ID};     // 要查询出来的列名(字段)数组
            String whereData = Data.RAW_CONTACT_ID + " = ?";                // 选区(查询范围)       //String where = String.format("%s = ?", Groups._ID);
            String whereArgsData[] = new String[]{contactId};               // 选择条件数组
            String sortOrderData = Data.MIMETYPE;                           // 查询结果排序规则
            // 注意：必须查询全部字段，不然很多数据无法读出，导致 App 崩溃
            Cursor cursorData = resolver.query(uriData, null, whereData, whereArgsData, sortOrderData);

            //System.out.println("contactId = " + contactId);
            //System.out.println("cursorId.getCount() = " + cursorId.getCount());         //I/System.out: cursorId.getCount() = 4
            //System.out.println("cursorData.getCount() = " + cursorData.getCount());     //I/System.out: cursorData.getCount() = 25

            m_Fun.logString(cursorData.getCount());
            while (cursorData.moveToNext()) {
                int iColData = cursorData.getColumnIndex(Data.MIMETYPE);      // 注意：查询结果中没有"mimetype_id"字段，只有"mimetype"字段
                if (iColData < 0) {
                    continue;
                }
                String contactDataMime = cursorData.getString(iColData);    // mimetype：StructuredName.CONTENT_ITEM_TYPE、Phone.CONTENT_ITEM_TYPE、...
                m_Fun.logString(contactDataMime);

                //System.out.println("contactDataMime = " + contactDataMime);
                // 获取 cursorData 中联系人数据最终存入 m_jsonContactData1。用该函数可以取代一系列判断代码块
                int ret = getContactsData(contactIdKey, contactDataMime, m_jsonContactData1, cursorData);
                if (-1 != ret) {
                    if (1 == ret) { // 只统计处理姓名字段是否成功
                        m_iSuccessCount++;
                    } else if (0 == ret) {
                        m_iFailCount++;
                    }
                }
            }
            cursorData.close();
            m_MA.m_handler.sendEmptyMessage(ExtraStrings.OUTPUT_COUNTING);          // 更新导出联系人计数
        }
        cursorId.close();

        m_jsonContactData2 = new JSONObject(new LinkedHashMap());   // 存放处理同名聚合后的中间变量
        AggregateSameName(m_jsonContactData1, m_jsonContactData2);  // 处理同名聚合

        //由于mContactsHeader中联系人的某种数据(比如mobile手机号)的最大值可能会不断增加，导致mJsonResult中数据长短不一
        //所以，最后再以mContactsHeader中各种数据大小的最终值为标准，再次将mJsonContactData.mJsonResult的所有字段填充到mJsonContactData2.mJsonResult中
        m_jsonContactData3 = new JSONObject(new LinkedHashMap());  //解决JsonObject数据固定顺序
        //dumpJsonContactData(m_jsonContactData1, m_jsonContactData3);
        //filterJsonContactData(m_jsonContactData1, m_jsonContactData3);
        filterJsonContactData(m_jsonContactData2, m_jsonContactData3);

        // 输出 JSONObject 完整结构到文件，path 为文件绝对路径
        m_Fun.Json2File(m_jsonContactData1, m_sPathDownloads, "m_jsonContactData1_1.txt");
        m_Fun.Json2File(m_jsonContactData2, m_sPathDownloads, "m_jsonContactData2_1.txt");
        m_Fun.Json2File(m_jsonContactData3, m_sPathDownloads, "m_jsonContactData3_1.txt");

        return traverseJSON(m_jsonContactData3);
    }

    // 获取 cursor 中联系人数据最终存入 m_jsonContactData1。用该函数可以取代一系列判断代码块
    private int getContactsData(String contactIdKey, String mimetype, JSONObject jsonContactData, Cursor cursor) {
        int ret = -1;
        // mimetype：StructuredName.CONTENT_ITEM_TYPE、Phone.CONTENT_ITEM_TYPE、...
        String key1 = getKey1(mimetype);    // 根据 mimetype 确定 key1: "jsonG00StructName"、"jsonG01Phone"、...
        switch (getMimetype4lay(key1, "__mimetype_fun")) {
            case "fun00":       // 默认需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
            case "fun04":       // Set 数据需要分类处理，包括 jsonG04OrgSet、jsonG05ImSet、jsonG08PostalSet
                fun00_dumpJson4lay(contactIdKey, key1, jsonContactData, cursor, 0);
                break;
            case "fun01":       // "jsonG01Phone"，需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                fun00_dumpJson4lay(contactIdKey, key1, jsonContactData, cursor, 1);
                break;
            case "fun02":       // 无需处理 xxx.TYPE_CUSTOM，将 cursor 中的数据循环转储到 m_jsonContactData1 中，比如 jsonG00StructName，必须要循环处理
                boolean bRet = fun02_dumpJson4layAll(contactIdKey, key1, jsonContactData, cursor);
                if (key1.equals("jsonG00StructName")) {
                    ret = bRet ? 1 : 0;
                }
                break;
            case "fun03":       // "jsonG03Photo"，单独用 fun03_dumpPhoto() 处理
                if (m_MA.m_bDealPhoto) {
                    fun03_dumpPhoto(contactIdKey, key1, cursor);
                }
                break;
            case "fun05":       // "jsonG09GroupMember"，单独用 fun05_dumpJson4lay() 处理
                fun05_dumpJson4lay(contactIdKey, key1, jsonContactData, cursor);
                break;
            default:
                break;
        }
        return ret;
    }

    //在多级 JSONObject 的第1级中数据中(从0级起)，根据 value 获取 key 值，必须 value 值不重复
    //这里的 m_contactHeader.m_jsonHeader 的 value : jsonG00StructName、jsonG01Phone、...。根据 mimetype 确定 key1
    //原创将心666666于2014-10-01，https://blog.csdn.net/jiangxindu1/article/details/39720481
    public String getKey1(String val1) {
        String key1 = "";
        try {
            Iterator<String> it1 = m_contactHeader.m_jsonHeader.keys();
            while (it1.hasNext()) {
                key1 = it1.next();           //key1: "jsonG00StructName"、"jsonG01Phone"、...
                String mimeNew = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject("__mimetype_item").getString("__first").trim();
                if (mimeNew.equals(val1)) {
                    break;
                } else {
                    key1 = "";  // 不匹配必须将key1清空。否则当全部不匹配时，key2将等于最后一个测试值，导致明显的查找错误
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return key1;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Begin 处理同名聚合
    // 在导入记录是处理联系人聚合，这种思路不稳定行不通
    // 所以准备在导出联系人时处理，具体在已经得到 m_jsonContactData1 数据后，还未转储到 m_jsonContactData3 之前处理。
    // 即在调用 filterJsonContactData(m_jsonContactData1, m_jsonContactData3) 之前处理
    private void AggregateSameName(JSONObject jsonSource, JSONObject jsonTarget) {
        //JSONObject属性遍历
        try {
            Iterator<String> it = jsonSource.keys();
            while (it.hasNext()) {
                String key = it.next(); //contact592、contact593、...
                JSONObject json = jsonSource.getJSONObject(key);
                if (json.length() == 0) {  // 若该条为空记录，便跳过
                    continue;
                }

                if (m_MA.m_bFilterNameOnly) {   // 剔除 jsonSource 中只有用户名、没有任何其他信息的联系人记录
                    Iterator<String> it2 = json.keys();
                    while (it2.hasNext()) {
                        String key2 = it2.next(); //displayName、lastName、firstName、...
                        if (!(key2.equals("displayName") || key2.equals("lastName") || key2.equals("firstName"))) {
                            if (!TextUtils.isEmpty(json.getString(key2))) {
                                dumpJsonAllFields2(key, jsonSource, jsonTarget); // 一次处理一条联系人记录
                            }
                        }
                    }
                } else {
                    dumpJsonAllFields2(key, jsonSource, jsonTarget); // 一次处理一条联系人记录
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 一次处理一条联系人记录，key 为：contact592、contact593、...
    private void dumpJsonAllFields2(String key, JSONObject jsonSource, JSONObject jsonTarget) {
        try {
            boolean bHasSameName = false;
            if (m_MA.m_iAggregateSameName >= 0 && m_MA.m_iAggregateSameName <= 3) {    // 聚合同名联系人信息
                // 查找通讯录中是否存在姓名 jsonItem.getString("displayName") 的联系人，若有则返回联系人记录的 contactId，不存在则返回 -1
                // m_iAggregateSameName : 0 完全相同；1 头部相同；2 尾部相同；3 任何位置相同
                if (bHasSameName = dealSameNameContact(jsonSource.getJSONObject(key), jsonTarget, m_MA.m_iAggregateSameName)) {
                    m_MA.m_iSameName++;
                }
            }
            if (!bHasSameName) {
                jsonTarget.put(key, new JSONObject(new LinkedHashMap()));
                Iterator<String> it1 = jsonSource.getJSONObject(key).keys();
                while (it1.hasNext()) {
                    String key1 = it1.next().trim();        //key1 : "displayName"、"lastName"、"mobile"、...
                    String val1 = jsonSource.getJSONObject(key).getString(key1).trim();
                    if (!TextUtils.isEmpty(val1) && !hasSameField(jsonTarget.getJSONObject(key), val1)) { // 非空、非重复才转储
                        jsonTarget.getJSONObject(key).put(key1, val1);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 查找 json 中是否已经存在同样类型同样内容的数据
    private boolean hasSameField(JSONObject json, String val) {
        boolean ret = false;
        val = val.trim();
        try {
            Iterator<String> it = json.keys();
            while (it.hasNext()) {
                String key = it.next();
                if (key.equals("displayName") || key.equals("lastName") || key.equals("firstName")) {
                    continue;   // 跳过姓名字段
                }
                if (val.equals(json.getString(key))) {
                    ret = true;
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;
    }

    // 在 jsonTarget 中查找是否有 "displayName" 为 name 的联系人
    private boolean dealSameNameContact(JSONObject jsonS, JSONObject jsonTarget, int iFlag) {
        boolean ret = false;
        try {
            String contactName = jsonS.getString("displayName"); // 待转储联系人名称

            Iterator<String> it1 = jsonTarget.keys();
            while (it1.hasNext()) {
                String key1 = it1.next(); //contact592、contact593、...
                String name0 = jsonTarget.getJSONObject(key1).getString("displayName").trim();

                // iFlag : 0 完全相同；1 头部相同；2 尾部相同；3 任何位置相同
                if (0 == iFlag && name0.equalsIgnoreCase(contactName)) {                 // 0 完全相同
                    ret = true;
                    dumpFields(jsonS, jsonTarget.getJSONObject(key1));
                    break;
                } else if (1 == iFlag) {            // 1 头部相同
                    if ((contactName.length() >= name0.length() && 0 == contactName.indexOf(name0)) ||
                            (contactName.length() < name0.length() && 0 == name0.indexOf(contactName))) {
                        ret = true;
                        dumpFields(jsonS, jsonTarget.getJSONObject(key1));
                        break;
                    }
                } else if (2 == iFlag) {                                                // 2 尾部相同
                    if (contactName.length() >= name0.length()) {
                        if (name0.equals(contactName.substring(contactName.length() - name0.length()))) {
                            ret = true;
                            dumpFields(jsonS, jsonTarget.getJSONObject(key1));
                            break;
                        }
                    } else if (contactName.length() < name0.length()) {
                        if (contactName.equals(name0.substring(name0.length() - contactName.length()))) {
                            ret = true;
                            dumpFields(jsonS, jsonTarget.getJSONObject(key1));
                            break;
                        }
                    }
                } else if (3 == iFlag && (-1 != contactName.indexOf(name0) || -1 != name0.indexOf(contactName))) {   // 3 任何位置相同
                    ret = true;
                    dumpFields(jsonS, jsonTarget.getJSONObject(key1));
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    // 单个联系人源数据 jsonS，单个联系人目标数据 jsonT
    private void dumpFields(JSONObject jsonS, JSONObject jsonT) {
        try {
            Iterator<String> it = jsonS.keys();
            while (it.hasNext()) {
                String keyS = it.next();        //key1 : "displayName"、"lastName"、"mobile"、...
                String valS = jsonS.getString(keyS).trim();
                String valT = "";
                if (jsonT.has(keyS)) {
                    valT = jsonT.getString(keyS).trim();
                }

                if (keyS.equals("displayName") || keyS.equals("lastName") || keyS.equals("firstName")) {
                    if (valS.length() > valT.length()) {
                        jsonT.remove(keyS);
                        jsonT.put(keyS, valS);
                    }
                } else {
                    if (jsonT.has(keyS) && !valT.equals(valS) && !hasSameField(jsonT, valS)) { // 非空、非重复才转储
                        jsonT.put(getNewkey(keyS, jsonT), valS);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 获取 json 没有的新 keynew
    private String getNewkey(String key, JSONObject jsonT) {
        String key2 = key.replaceAll("\\d+$", "").trim();    // java 正则去掉字符串末尾数字
        String keynew = key2;
        int n = 2;
        while (jsonT.has(keynew)) {
            keynew = key2 + String.valueOf(n++);
        }

        return keynew;
    }

    //Android中如何修改json里某个字段的值.原创圣西罗的红与黑v 2017-09-27
    //原文链接：https://blog.csdn.net/GXL_1899/article/details/78114314
    //直接调用就可以.  第一个参数为key,第二个为值,第三个传一个整串json的jsonObject.
    /**
     * 解析Json数据.
     * @param key    更换数据key
     * @param value  更换Value
     * @param object  解析对象
     */
    public void analyzeJson(String key, Object value, Object object) {
        try {
            if (object instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) object;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    analyzeJson(key, value, jsonObject);
                }
            } else if (object instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) object;
                Iterator iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String jsonKey = iterator.next().toString();
                    Object ob = jsonObject.get(jsonKey);
                    if (ob != null) {
                        if (ob instanceof JSONArray) {
                            analyzeJson(key, value, ob);
                        } else if (ob instanceof JSONObject) {
                            analyzeJson(key, value, ob);
                        } else {
                            if (jsonKey.equals(key)) {
                                jsonObject.put(key, value);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // End 处理同名聚合
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //1、第一类型有4层结构，mJsonG00到mJsonG06、mJsonG08
    //m_jsonHeader->jsonG00StructName->displayName->__first
    //2、第二类型有5层结构，mJsonG07、mJsonG09
    //m_jsonHeader->jsonG04OrgSet->jsonG04_00WorkOrgType->workCompany->__first

    //                     ->it1、key1     ->it2、key2          ->it3、key3                 ->it4、key4
    //class ContactHeader  ->JSONObject    ->JSONObject         ->JSONObject                ->JSONObject    ->JSONObject
    //class ContactHeader  ->m_jsonHeader  ->jsonG04OrgSet      ->jsonG04_00WorkOrgType     ->workCompany   ->__first
    //class ContactHeader  ->m_jsonHeader  ->jsonG00StructName  ->displayName               ->__first

    // 由于mContactsHeader中联系人的某种数据(比如mobile手机号)的最大值可能会不断增加，导致mJsonResult中数据长短不一，
    // 并且各信息字段的顺序和位置也不一致。所以，最后再以mContactsHeader中各种数据大小的最终值为标准，
    // 再次将mJsonContactData.mJsonResult的所有字段填充到mJsonContactData2.mJsonResult中
    // 转储过程中，同时剔除 jsonSource 中只有用户名、没有任何其他信息的联系人记录
    private void filterJsonContactData(JSONObject jsonSource, JSONObject jsonTarget) {
        //JSONObject属性遍历
        try {
            Iterator<String> it = jsonSource.keys();
            while (it.hasNext()) {
                String key = it.next(); //contact592、contact593、...
                JSONObject json = jsonSource.getJSONObject(key);
                if (json.length() == 0) {  // 若该条为空记录，便跳过
                    continue;
                }

                if (m_MA.m_bFilterNameOnly) {   // 剔除 jsonSource 中只有用户名、没有任何其他信息的联系人记录
                    Iterator<String> it2 = json.keys();
                    while (it2.hasNext()) {
                        String key2 = it2.next(); //displayName、lastName、firstName、...
                        if (!(key2.equals("displayName") || key2.equals("lastName") || key2.equals("firstName"))) {
                            if (!TextUtils.isEmpty(json.getString(key2))) {
                                jsonTarget.put(key, new JSONObject(new LinkedHashMap()));
                                dumpJsonAllFields(key, jsonSource, jsonTarget); // 一次处理一条联系人记录
                            }
                        }
                    }
                } else {
                    jsonTarget.put(key, new JSONObject(new LinkedHashMap()));
                    dumpJsonAllFields(key, jsonSource, jsonTarget); // 一次处理一条联系人记录
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //1、第一类型有4层结构，mJsonG00到mJsonG06、mJsonG08
    //m_jsonHeader->jsonG00StructName->displayName->__first
    //2、第二类型有5层结构，mJsonG07、mJsonG09
    //m_jsonHeader->jsonG04OrgSet->jsonG04_00WorkOrgType->workCompany->__first

    //                     ->it1、key1     ->it2、key2          ->it3、key3                 ->it4、key4
    //class ContactHeader  ->JSONObject    ->JSONObject         ->JSONObject                ->JSONObject    ->JSONObject
    //class ContactHeader  ->m_jsonHeader  ->jsonG04OrgSet      ->jsonG04_00WorkOrgType     ->workCompany   ->__first
    //class ContactHeader  ->m_jsonHeader  ->jsonG00StructName  ->displayName               ->__first

    // 由于mContactsHeader中联系人的某种数据(比如mobile手机号)的最大值可能会不断增加，导致mJsonResult中数据长短不一，
    // 并且各信息字段的顺序和位置也不一致。所以，最后再以mContactsHeader中各种数据大小的最终值为标准，
    // 再次将mJsonContactData.mJsonResult的所有字段填充到mJsonContactData2.mJsonResult中
    private void dumpJsonContactData(JSONObject jsonSource, JSONObject jsonTarget) {
        //JSONObject属性遍历
        try {
            Iterator<String> it = jsonSource.keys();
            while (it.hasNext()) {
                String key = it.next(); //contact592、contact593、...
                if (jsonSource.getJSONObject(key).length() == 0) {  // 若该条为空记录，便跳过
                    continue;
                }
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

    //这里放入的jsonObject是一个对象(引用或指针)，放了之后还可以进行操作
    //m_jsonContactData1.put("contact" + mIntSum, mJsonResult);
    //mIntSum++;
    //...
    //return traverseJSON(m_jsonContactData1);
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

    //String substring(int beginIndex, int endIndex)，返回的字串长度为 endIndex-beginIndex。
    //endIndex(不包括)可以指向末尾字符之后的数值("emptiness".length())："emptiness".substring(9) returns "" (an empty string)

    // 根据 subtype 值，取得类型前缀。处理 __mimetype_subtype_ 字段
    private String getPrefix(String subtype, String key1) {
        String prefix = getKey2(subtype, key1); //根据子类型 subtype 确定 key2 : displayName、lastName、...
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

    // 取出4层 JSONObject 结构对应的信息转储到 m_jsonContactData1 中。每次转储该种类的指定子类型的字段
    // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标；int iPhone : 0 非电话号码；1 电话号码
    private boolean fun00_dumpJson4lay(String idKey, String key1, JSONObject jsonContactData, Cursor cursor, int iPhone) {
        boolean ret = false;
        // 正确用法！取当前 cursor 对应的信息子类型
        String type = getMimetype4lay(key1, "__mimetype_type").trim();  // type 为大类型(data2)：Phone.TYPE、Email.TYPE、...
        int iSubtype = cursor.getInt(cursor.getColumnIndex(type));      // subtype 为子类型，比如 xxx.TYPE_HOME、xxx.TYPE_WORK、...
        String prefix = getPrefix(String.valueOf(iSubtype), key1);      // 根据 subtype 值，取得类型前缀。处理 __mimetype_subtype_ 字段

        // 处理 jsonG05ImSet
        String protocal = getMimetype4lay(key1, "__mimetype_protocal").trim();
        if (!TextUtils.isEmpty(protocal)) {  //若存在 "__mimetype_protocal" 字段，便取出 xxx.PROTOCOL(data5) 的值，作为子类型
            iSubtype = cursor.getInt(cursor.getColumnIndex(protocal));
        }

        //subtype : 当前cursor对应的信息子类型：0、1、2、...
        String subtype = String.valueOf(iSubtype).trim();  // 默认处理
        // 注意：下面用法不对！会出现把字符的Ascii值当做数值，然后再转换为数字字符串返回。比如，原应返回数字 0，这种方式可能会返回数字字符串 30
        //String subtype = cursor.getString(cursor.getColumnIndex(subtype)).trim();

        String key2 = getKey2(subtype, key1);   //根据子类型 subtype 确定 key2 : displayName、lastName、...
        String subtype2 = getSubtype2(key1, key2);
        if (!TextUtils.isEmpty(subtype2) && subtype.equals(subtype2)) {
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
            ret = put2json4lay(idKey, key1, key2, val, prefix, jsonContactData);   // 将获取的数据存入 m_jsonContactData1
        }
        return ret;
    }

    public String getSubtype2(String key1, String key2) {
        String subtype2 = "";
        try {
            subtype2 = m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__first");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return subtype2;
    }

    //在多级 JSONObject 的第2级中数据中(从0级起)，根据 value 获取 key 值，必须 value 值不重复
    //这里的 m_contactHeader.m_jsonHeader.getJSONObject(key1) 的 value : "data1"、"data2"、...。根据子类型 subtype 确定 key2 : displayName、lastName、...
    //原创将心666666于2014-10-01，https://blog.csdn.net/jiangxindu1/article/details/39720481
    public String getKey2(String val2, String key1) {
        String key2 = "";
        try {
            JSONObject json = m_contactHeader.m_jsonHeader.getJSONObject(key1);
            Iterator<String> it = json.keys();
            while (it.hasNext()) {
                key2 = it.next();
                if (json.getJSONObject(key2).getString("__first").equals(val2.trim())) {
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

    // 取出4层 JSONObject 结构对应的所有信息转储到 m_jsonContactData1 中。专门处理："jsonG09GroupMember"
    // idKey : contactIdKey，如：contact1、contact2、...；
    // key1 : m_jsonHeader的key1，如：jsonG00StructName、jsonG07Note、jsonG09GroupMember；
    // cursor : 查询游标
    private boolean fun05_dumpJson4lay(String idKey, String key1, JSONObject jsonContactData, Cursor cursor) {
        String key2 = "groupId";
        String col = get4layColumnName(key1, key2);                      // 获取该类信息的在数据表中的列号(字段号)
        String groupId = cursor.getString(cursor.getColumnIndex(col));   // 获取数据表中的数据
        put2json4lay(idKey, key1, key2, groupId, "", jsonContactData);           // 将获取的数据存入 m_jsonContactData1

        key2 = "groupTitle";
        String groupTitle = "";     //由于新建联系人群组时一般是输入群组名称，所以在联系人信息中保存联系人群组名称
        if (!TextUtils.isEmpty(groupId)) {
            groupTitle = m_GroupOutput.getGroupTitle(groupId, m_MA);
        }
        return put2json4lay(idKey, key1, key2, groupTitle, "", jsonContactData); // 将获取的数据存入 m_jsonContactData1
    }

    // 无需处理 xxx.TYPE_CUSTOM，将 cursor 中的数据循环转储到 m_jsonContactData1 中，比如 jsonG00StructName，必须要循环处理
    // idKey : contactIdKey，如：contact1、contact2、...；
    // key1 : m_jsonHeader的key1，如：jsonG00StructName、jsonG07Note、jsonG09GroupMember；
    // cursor : 查询游标
    private boolean fun02_dumpJson4layAll(String idKey, String key1, JSONObject jsonContactData, Cursor cursor) {  // 该函数必须要循环处理
        boolean ret = false;
        try {
            int i = 0;  // 判断是否是无名用户的计数器
            boolean bDealAnonymous = true;  // 需要为无名用户添加 "displayName"
            Iterator<String> it = m_contactHeader.m_jsonHeader.getJSONObject(key1).keys();
            while (it.hasNext()) {
                String key2 = it.next();                                    // key2："displayName"、"lastName"、"firstName"、...
                // 跳过前面的元素 "__mimetype_xxx"
                if (key2.length() > "__mimetype_".length() && key2.substring(0, "__mimetype_".length()).equals("__mimetype_")) {
                    continue;
                }

                String col = get4layColumnName(key1, key2);                 // 获取该类信息的在数据表中的列号(字段号)
                String data = cursor.getString(cursor.getColumnIndex(col)); // 获取数据表中的数据
                // 处理无名用户的计数器增量
                if (TextUtils.isEmpty(data) && (key2.equals("displayName") || key2.equals("lastName") || key2.equals("firstName"))) {
                    i++;
                }
                put2json4lay(idKey, key1, key2, data, "", jsonContactData);          // 将获取的数据存入 m_jsonContactData1

                if (bDealAnonymous && 3 == i) {   // 为无名记录添加名字
                    m_jsonContactData1.getJSONObject(idKey).put("displayName", "anonymous_" + (++m_iAnonymous));
                    bDealAnonymous = false;
                }
            }
            ret = true;
        } catch (JSONException e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    // 将获取的数据存入 m_jsonContactData1
    // idKey : contactIdKey，如：contact1、contact2、...；
    // key1 : m_jsonHeader的key1，如：jsonG00StructName、jsonG07Note、jsonG09GroupMember；
    // key2："groupId"、"groupSourceId"、"__mimetype_xxx"、...
    // val: 存入的数据 data
    // prefix: 子类型前缀，home、work、other、...
    // cursor : 查询游标
    // private ContactHeader m_contactHeader;         //用于存放通讯录所有记录的表头信息
    // private ContactHeader m_contactHeaderCount;    //用于存放获取的每条记录每一列的计数器
    private boolean put2json4lay(String idKey, String key1, String key2, String val, String prefix, JSONObject jsonContactData) {
        boolean ret = false;
        String keyNew = prefix + key2;
        try {
            if (null != m_contactHeaderCount) {
                int n = Integer.valueOf(m_contactHeaderCount.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__second"));
                n++;
                m_contactHeaderCount.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).put("__second", String.valueOf(n));
                if (n > 1) {
                    keyNew += n;
                }
                //没有下面两行就只能获得某种数据的第一个值。比如只能获得第一个手机号，其他手机号丢失
                n = java.lang.Math.max(n, Integer.valueOf(m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).getString("__second")));
                m_contactHeader.m_jsonHeader.getJSONObject(key1).getJSONObject(key2).put("__second", String.valueOf(n));
            }
            jsonContactData.getJSONObject(idKey).put(keyNew, val);
            ret = true;
        } catch (JSONException e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
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
    // 取出4层 JSONObject 结构对应的所有信息转储到 m_jsonContactData1 中，比如 jsonG00StructName
    // idKey : contactIdKey；key1 : m_jsonHeader的key1；cursor : 查询游标
    private boolean fun03_dumpPhoto(String idKey, String key1, Cursor cursor) {
        boolean ret = false;
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
            //cursor.close();   //外部传入的游标，不应该在此关闭
        }
        String filename = "";
        try {
            filename = m_jsonContactData1.getJSONObject(idKey).getString("displayName") + "_1";
            filename = filename.replace(" ", "_");  // 文件名空格全部替换为"_"
        } catch (JSONException e) {
            filename = "anonymity_1";
            e.printStackTrace();
        }
        ret = saveBmpFile(photoBmp, m_sPathDownloads + "/Photo", filename, "png", 100);
        return ret;
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
    private boolean saveBmpFile(Bitmap bmp, String path, String filename, String photoType, int iQuality) {
        boolean ret = false;
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
            ret = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ret = false;
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }

    public int getSum() {
        return m_iSum;
    }

    public int getSuccessCount() {
        return m_iSuccessCount;
    }

    public int getFailCount() {
        return m_iFailCount;
    }
}
