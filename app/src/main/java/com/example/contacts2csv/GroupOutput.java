package com.example.contacts2csv;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.example.contacts2csv.MainActivity.m_Fun;
import static com.example.contacts2csv.MainActivity.m_MA;
import static com.example.contacts2csv.MainActivity.m_sPathDownloads;

/**
 * @author glsite.com
 * @version 1.0.0$
 * @des Contact Groups Oupput
 * @updateAuthor MinJun$
 * @updateDes Contact Groups Oupput
 */

//导出导入联系人时，应该先处理组信息
//导出联系人时，先导出组信息到Groups_xxx.txt，不用管该组有多少组成员；然后再导出联系人，包含联系人属于哪些组的信息
//导入联系人时，先导入组信息，不用管该组有多少组成员；然后再导入联系人，导入联系人时根据属于哪些组的信息，将该联系人加入这些组即可

public class GroupOutput {
    private final String m_sTAG = getClass().getSimpleName();
    private GroupHeader m_groupHeader;         //用于存放群组信息列表头

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 处理分组信息 Begin

    //Android联系人--群组分组查询。原创BunToy 最后发布于2015-03-04 11:03:03 阅读数 1223  收藏
    //原文链接：https://blog.csdn.net/xiabing082/article/details/44057413

    public GroupOutput() {
        m_groupHeader = new GroupHeader();
    }

    // 当 Groups.DELETED = 0 的时候， 是查询没有被删除的联系人分组
    public void getContactsGroups() {
        String[] RAW_PROJECTION = new String[]{Groups._ID, Groups.TITLE,};
        String RAW_CONTACTS_WHERE = Groups.DELETED + " = ? ";
        Cursor cursor = m_MA.getContentResolver().query(Groups.CONTENT_URI, RAW_PROJECTION,
                RAW_CONTACTS_WHERE, new String[]{"" + 0}, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            Log.e("XZQ", id + "   " + title);
        }
        cursor.close();
    }

    // 当 Groups.DELETED = 1 的时候，是查询删除的分组
    public void getContactsGroups1() {
        String[] RAW_PROJECTION = new String[]{Groups._ID, Groups.TITLE};
        String RAW_CONTACTS_WHERE = Groups.DELETED + " = ? ";
        Cursor cursor = m_MA.getContentResolver().query(Groups.CONTENT_URI, RAW_PROJECTION, RAW_CONTACTS_WHERE, new String[]{"" + 1}, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            Log.e("TAG", id + "   " + title);
        }
        cursor.close();
    }

    // 默认情况下查询所有的分组
    public String getContactsGroups2() {
        String sGroupInfo = "";     //全部群组信息
        String sGroupHeader = "";   //群组信息表头
        Cursor cursor = m_MA.getContentResolver().query(Groups.CONTENT_URI, null, null, null, null);
        int n = 0;
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(Groups._ID));              // 得到组ID
            String title = cursor.getString(cursor.getColumnIndex(Groups.TITLE));   // 得到组名称
            int count = getCountOfGroup(id);                                        // 得到组成员数
            //Log.e("MainActivity", id + "   " + title + "  " + count);
            //System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");
            //System.out.println("\n群组ID：" + id + "，群组名称：" + title + "，成员数：" + count);

            List<ContactEntity> m = getAllContactsByGroupId(id, m_MA); // 获取某个分组下的 所有联系人信息
            int i = 1;
            for (ContactEntity c : m) {
                //System.out.println("\t群组成员" + i++ + "：" + c.getContactName());
            }

            //System.out.println("\n群组详细信息：\n{");
            Iterator<String> it = m_groupHeader.m_jsonHeader.keys();
            String str = "";
            while (it.hasNext()) {
                String key = it.next();
                if (0 == n) {
                    sGroupHeader += TextUtils.isEmpty(sGroupHeader) ? key : ("," + key);
                }
                try {
                    String mime = m_groupHeader.m_jsonHeader.getString(key);
                    String info = cursor.getString(cursor.getColumnIndex(mime));
                    str += TextUtils.isEmpty(str) ? info : ("," + info);
                    //System.out.println("\t" + key + ", " + info);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //System.out.println("}\n");
            //System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");

            sGroupInfo += TextUtils.isEmpty(sGroupInfo) ? str : ("\n" + str);    //每个群组信息换行
            n++;
        }
        cursor.close();

        return sGroupHeader + "\n" + sGroupInfo;
    }

    // 根据 groupId 查询 groupTitle
    public String getGroupsName(String groupId) {
        int iGroupId = Integer.valueOf(groupId.trim());
        String groupTitle = "";     // 群组名称
        Cursor cursor = m_MA.getContentResolver().query(Groups.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            if (iGroupId == cursor.getInt(cursor.getColumnIndex(Groups._ID))) {
                groupTitle = cursor.getString(cursor.getColumnIndex(Groups.TITLE));   // 得到组名称
                break;
            }
        }
        cursor.close();

        return groupTitle;
    }

    public void saveGroupinfo2File(String str) {
        File file = m_Fun.GetNewFile(m_sPathDownloads, ExtraStrings.OUTPUT_GROUP_INFO_FILENAME, 0);
        String path = file.getAbsolutePath();

        m_Fun.writeFile(path, str);
    }

    // 默认情况下查询所有的分组
    public void getContactsGroups2_0() {
        Cursor cursor = m_MA.getContentResolver().query(Groups.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(Groups._ID));              // 得到组ID
            String title = cursor.getString(cursor.getColumnIndex(Groups.TITLE));   // 得到组名称
            int count = getCountOfGroup(id);                                        // 得到组成员数
            //Log.e("MainActivity", id + "   " + title + "  " + count);
            System.out.println("组ID：" + id + "，组名称：" + title + "，成员数：" + count);

            List<ContactEntity> m = getAllContactsByGroupId(id, m_MA); // 获取某个分组下的 所有联系人信息
            int i = 1;
            for (ContactEntity c : m) {
                System.out.println("\t组成员" + i++ + "：" + c.getContactName());
            }
        }
        cursor.close();
    }

    //I/System.out: 组ID：1，组名称：Family，成员数：0
    //I/System.out: 组ID：2，组名称：Friends，成员数：0
    //I/System.out: 组ID：3，组名称：Coworkers，成员数：0
    //I/System.out: 组ID：4，组名称：ICE，成员数：0
    //I/System.out: 组ID：5，组名称：group1，成员数：4
    //I/System.out: 	组成员1：Li Si
    //I/System.out: 	组成员2：Wang Wu
    //I/System.out: 	组成员3：Zhang San
    //I/System.out: 	组成员4：Zhang San2
    //I/System.out: 组ID：6，组名称：group2，成员数：2
    //I/System.out: 	组成员1：Wang Wu
    //I/System.out: 	组成员2：Zhang San2


    /**
     * @param 群组ID
     * @return 查询当前分组中有多少个联系人
     */
    private int getCountOfGroup(int groupId) {
        String selection = Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE + "' AND " + Data.DATA1 + "=" + groupId;
        String[] colvalue = new String[]{Data.RAW_CONTACT_ID};
        //Cursor cursor = m_MA.getContentResolver().query(Groups.CONTENT_URI, null, null, null, null);
        Cursor cursor = m_MA.getContentResolver().query(Data.CONTENT_URI, colvalue, selection, null, "data1 asc");
        int count = cursor.getCount();
        return count;
    }


    //其实联系人分组实现原理是：
    //  根据 Data.MIMETYPE 为 GroupMembership 类型，data1 中的组 id 来进行分组。
    // 设置 Data.CONTENT_URI 中的 Data.MIMETYPE 为
    // GroupMembership.CONTENT_ITEM_TYPE 类型，data1 字段为某一分组的组ID，
    // 该值可查询 Groups.CONTENT_URI(该表保存了各分组的组_id，组名称 title 等分组信息)得到。
    //例如查询具有某一分组的所有联系人的RawContacts._ID，代码如下
    public static final String[] RAW_PROJECTION = new String[]{
        Data.RAW_CONTACT_ID,
    };
    public static final String RAW_CONTACTS_WHERE = GroupMembership.GROUP_ROW_ID + "=?" + " and " +
                    Data.MIMETYPE + "=" + "'" + GroupMembership.CONTENT_ITEM_TYPE + "'";
    //具有同一组id的原始联系人的id
    //Cursor mMemberRawIds =rc.query(URI, RAW_PROJECTION, RAW_CONTACTS_WHERE, new String[]{""+groupId}, "data1 asc");

    public class GroupEntity {
        private int groupId;
        private String groupName;

        public int getGroupId() {
            return groupId;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }
    }

    //Android通讯录之分组联系人，Ziv最后发布于2017-10-15
    //https://blog.csdn.net/qq_30180559/article/details/78242861
    // 获取所有的 联系人分组信息
    public List<GroupEntity> getAllGroupInfo(Context context) {
        List<GroupEntity> groupList = new ArrayList<GroupEntity>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI, null, null, null, null);
            while (cursor.moveToNext()) {
                GroupEntity ge = new GroupEntity();
                int groupId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Groups._ID));             // 组id
                String groupName = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE));   // 组名
                ge.setGroupId(groupId);
                ge.setGroupName(groupName);
                Log.i("MainActivity", "group id:" + groupId + ">>groupName:" + groupName);
                groupList.add(ge);
                ge = null;
            }
            return groupList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public class ContactEntity {
        private int contactId;
        private String contactName;
        private String telNumber;

        public int getContactId() {
            return contactId;
        }

        public void setContactId(int contactId) {
            this.contactId = contactId;
        }

        public String getContactName() {
            return contactName;
        }

        public void setContactName(String contactName) {
            this.contactName = contactName;
        }

        public String getTelNumber() {
            return telNumber;
        }

        public void setTelNumber(String telNumber) {
            this.telNumber = telNumber;
        }
    }

    /**
     * 获取某个分组下的 所有联系人信息
     * 思路：通过组的id 去查询 RAW_CONTACT_ID, 通过RAW_CONTACT_ID去查询联系人要查询得到 data表的Data.RAW_CONTACT_ID字段
     */
    public List<ContactEntity> getAllContactsByGroupId(int groupId, Context context) {
        String[] RAW_PROJECTION = new String[]{Data.RAW_CONTACT_ID,};
        String RAW_CONTACTS_WHERE = GroupMembership.GROUP_ROW_ID + "=?" + " and " +
                Data.MIMETYPE + "=" + "'" + GroupMembership.CONTENT_ITEM_TYPE + "'";

        // 通过分组的id 查询得到RAW_CONTACT_ID
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, RAW_PROJECTION,
                RAW_CONTACTS_WHERE, new String[]{groupId + ""}, "data1 asc");

        List<ContactEntity> contactList = new ArrayList<ContactEntity>();

        while (cursor.moveToNext()) {
            // RAW_CONTACT_ID
            int col = cursor.getColumnIndex("raw_contact_id");
            int raw_contact_id = cursor.getInt(col);

            // Log.i("getAllContactsByGroupId", "raw_contact_id:" + raw_contact_id);

            ContactEntity ce = new ContactEntity();
            ce.setContactId(raw_contact_id);

            Uri dataUri = Uri.parse("content://com.android.contacts/data");
            Cursor dataCursor = context.getContentResolver().query(dataUri, null,
                    "raw_contact_id=?", new String[]{raw_contact_id + ""}, null);

            while (dataCursor.moveToNext()) {
                String data1 = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                String mime = dataCursor.getString(dataCursor.getColumnIndex("mimetype"));

                if ("vnd.android.cursor.item/phone_v2".equals(mime)) {
                    ce.setTelNumber(data1);
                } else if ("vnd.android.cursor.item/name".equals(mime)) {
                    ce.setContactName(data1);
                }
            }

            dataCursor.close();
            contactList.add(ce);
            ce = null;
        }
        cursor.close();
        return contactList;
    }


    ////方法是 通过分组的id 查询 该组的所有联系人
    ////思路是 通过分组的id 查询出data表里的raw_contact_id
    //// 通过raw_contact_id去查询联系人的姓名电话号码
    ////self 是上下文  id是联系人分组的id
    //
    //Cursor cursor = self
    //	.getContentResolver().query(Data.CONTENT_URI,
    //	 new String[] { Data.RAW_CONTACT_ID },
    //	Data.MIMETYPE
    //	+ " =' "
    //	+ GroupMembership.CONTENT_ITEM_TYPE
    //	+ " ' " + " and " +GroupMembership._ID
    //	+ " = ? "
    //
    //	, new String[] { String.valueOf(id) }, null);
    ////打印出来为false 我搞了半天还是没有搞懂
    //Log.e(TAG, "查询该组的联系人cursor********************：" + cursor.moveToFirst());

    //我想做个android通讯录的demo。
    //功能主要用：操作系统的通讯录联系人。这个比较简单，通过URL可以获取到，我可以实现。
    //但是：我想获取联系人分组，就没有思路做了。android模拟器里，没有分组这个选项，只有收藏，但是真机里有分组这个功能的。
    //那么要怎么样去，获取手机通讯录里设置的分组呢？大家说说思路吧！
    //功能主要包括：可以在demo里获取手机通讯录里的分组，修改分组名字，可以添加分组，删除分组，添加组成员，移除组成员！
    //谢谢大家了！我在百度 google里搜了很多资料，还是一头雾水。后来自己去研究了android通讯录数据库，大家帮忙指点下哦
    //0 2012-07-14 16:23:42

    private String COLUMN_NAME = "1";
    private String COLUMN_NUMBER = "2";

    // 查询分组的联系人方法 outid是分组的id。https://bbs.csdn.net/topics/390134732
    public ArrayList<HashMap<String, String>> getContactsByGroupId(int outid, Context context) {
        Log.e(m_sTAG, "开始查询该组的联系人********************id:" + outid);
        ArrayList<HashMap<String, String>> mymaplist = new ArrayList<HashMap<String, String>>();
        // 思路 我们通过组的id 去查询 RAW_CONTACT_ID, 通过RAW_CONTACT_ID去查询联系人
        // 要查询得到 data表的Data.RAW_CONTACT_ID字段
        String[] RAW_PROJECTION = new String[]{Data.RAW_CONTACT_ID,};

        String RAW_CONTACTS_WHERE = GroupMembership.GROUP_ROW_ID + "=?" + " and "
                + Data.MIMETYPE + "=" + "'" + GroupMembership.CONTENT_ITEM_TYPE + "'";
        // 通过分组的id outid；查询得到RAW_CONTACT_ID
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, RAW_PROJECTION,
                RAW_CONTACTS_WHERE, new String[]{"" + outid}, "data1 asc");

        while (cursor.moveToNext()) {
            HashMap<String, String> map = new HashMap<String, String>();
            // RAW_CONTACT_ID
            int contactId = cursor.getInt(cursor.getColumnIndex("raw_contact_id"));
            Log.e(m_sTAG, "查询该组的联系人的raw_contact_id****************：" + contactId);
            String[] RAW_PROJECTION02 = new String[]{StructuredName.DISPLAY_NAME,};
            String RAW_CONTACTS_WHERE02 = StructuredName.CONTACT_ID + "=?" + " and "
                    + Data.MIMETYPE + "=" + "'" + StructuredName.CONTENT_ITEM_TYPE + "'";
            // 通过raw_contact_id的值获取用户的名字
            Cursor cursor01 = context.getContentResolver().query(Data.CONTENT_URI, RAW_PROJECTION02,
                    RAW_CONTACTS_WHERE02, new String[]{"" + contactId}, "data1 asc");
            String contacts_name = null;
            while (cursor01.moveToNext()) {
                contacts_name = cursor01.getString(cursor01.getColumnIndex("data1"));
                Log.e(m_sTAG, "联系人姓名:" + contacts_name);
            }
            map.put(COLUMN_NAME, contacts_name);

            String[] RAW_PROJECTION03 = new String[]{Phone.NUMBER,};

            String RAW_CONTACTS_WHERE03 =
                    Phone.CONTACT_ID + "=?" + " and " + Data.MIMETYPE + "=" + "'" + Phone.CONTENT_ITEM_TYPE + "'";
            // 有多个号码时
            // String num_str = cursor
            // .getString(cursor
            // .getColumnIndex(Contacts.HAS_PHONE_NUMBER));
            // int num = Integer.valueOf(num_str);
            // if (num > 0) {

            // 通过raw_contact_id 获取电话号码
            Cursor cursor02 = context.getContentResolver().query(Data.CONTENT_URI, RAW_PROJECTION03,
                    RAW_CONTACTS_WHERE03, new String[]{"" + contactId}, "data1 asc");
            String phonenum = null;
            while (cursor02.moveToNext()) {
                phonenum = cursor02.getString(cursor02.getColumnIndex("data1"));
                // map.put("phonekey", phonenum);
                Log.e(m_sTAG, "联系人电话号码:" + phonenum);
            }
            map.put(COLUMN_NUMBER, phonenum);
            // }
            mymaplist.add(map);
        }
        Log.e(m_sTAG, "结束查询改组的联系人，返回联系人的集合********************" + mymaplist.size());
        return mymaplist;
    }


    //2012-07-19 11:20:47只看TA 引用 举报 #3    得分 0	随雨诺
    //先顶下，lz有个问题啊，你这分组的outid是哪里来的？自己跑到数据库区看的？
    //还有：代码比较乱，希望大家给优化！谢谢！
    // 查询没有分组的联系人
    public ArrayList<HashMap<String, String>> getContactsByNoGroup(Context context) {
        Log.e(m_sTAG, "开始查询没有分组的联系人********************");
        ArrayList<HashMap<String, String>> mymaplist = new ArrayList<HashMap<String, String>>();
        // 思路 我们通过组的id 去查询 RAW_CONTACT_ID, 通过RAW_CONTACT_ID去查询联系人
        // 查询未分组联系人的过滤条件
        String RAW_CONTACTS_IN_NO_GROUP_SELECTION = "1=1) and " + Data.RAW_CONTACT_ID + " not in( select "
                + Data.RAW_CONTACT_ID + " from view_data_restricted where " + Data.MIMETYPE + "='"
                + GroupMembership.CONTENT_ITEM_TYPE + "') group by (" + Data.RAW_CONTACT_ID;

        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI,
                null, RAW_CONTACTS_IN_NO_GROUP_SELECTION, null, null);

        Log.e(m_sTAG, "查询该组的联系人cursorgetCount********************：" + cursor.getCount());
        while (cursor.moveToNext()) {
            HashMap<String, String> map = new HashMap<String, String>();
            // RAW_CONTACT_ID
            int contactId = cursor.getInt(cursor.getColumnIndex("raw_contact_id"));
            Log.e(m_sTAG, "查询该组的联系人cursor***联系人的id****************：" + contactId);
            String[] RAW_PROJECTION02 = new String[]{StructuredName.DISPLAY_NAME,};

            String RAW_CONTACTS_WHERE02 = StructuredName.CONTACT_ID + "=?" + " and "
                    + Data.MIMETYPE + "=" + "'" + StructuredName.CONTENT_ITEM_TYPE + "'";

            Cursor cursor01 = context.getContentResolver().query(
                    Data.CONTENT_URI, RAW_PROJECTION02,
                    RAW_CONTACTS_WHERE02, new String[]{"" + contactId}, "data1 asc");
            String contacts_name = null;
            while (cursor01.moveToNext()) {
                contacts_name = cursor01.getString(cursor01.getColumnIndex("data1"));
                Log.e(m_sTAG, "联系人姓名:" + contacts_name);
            }
            map.put(COLUMN_NAME, contacts_name);
            // 有多个号码时

            String[] RAW_PROJECTION03 = new String[]{Phone.NUMBER,};

            String RAW_CONTACTS_WHERE03 = Phone.CONTACT_ID + "=?" + " and "
                    + Data.MIMETYPE + "=" + "'" + Phone.CONTENT_ITEM_TYPE + "'";

            // String num_str = cursor
            // .getString(cursor
            // .getColumnIndex(Contacts.HAS_PHONE_NUMBER));
            // int num = Integer.valueOf(num_str);
            // if (num > 0) {
            Cursor cursor02 = context.getContentResolver().query(Data.CONTENT_URI, RAW_PROJECTION03,
                    RAW_CONTACTS_WHERE03, new String[]{"" + contactId}, "data1 asc");
            String phonenum = null;
            while (cursor02.moveToNext()) {
                phonenum = cursor02.getString(cursor02.getColumnIndex("data1"));
                map.put("phonekey", phonenum);
                Log.e(m_sTAG, "联系人电话号码:" + phonenum);
            }
            map.put(COLUMN_NUMBER, phonenum);
            // }
            mymaplist.add(map);
        }
        Log.e(m_sTAG, "结束查询没有分组的联系人，返回结合********************" + mymaplist.size());
        return mymaplist;
    }

    // 处理分组信息 End
    ////////////////////////////////////////////////////////////////////////////////////////////////
}
