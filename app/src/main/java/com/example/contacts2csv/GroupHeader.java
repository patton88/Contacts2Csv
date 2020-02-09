package com.example.contacts2csv;

import android.provider.ContactsContract.Groups;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;

/**
 * @author glsite.com
 * @version $
 * @des
 * @updateAuthor $
 * @updateDes
 */
// 群组信息列表头
public class GroupHeader {
    public JSONObject m_jsonHeader;

    //群组信息Mimetype，来自：D:\Android\Android-SDK-Windows\sources\android-29\android\provider\ContactsContract.java
    public String[] Mimetype_Groups = new String[]{
            //mimetype数据字段                       //存储json变量           //数据内容
            Groups._ID,             //Groups_Id              //组Id
            Groups.ACCOUNT_NAME,    //Groups_AccountName     //组账户名
            Groups.ACCOUNT_TYPE,    //Groups_AccountType     //组账户类型
            Groups.DIRTY,           //Groups_Dirty           //组废弃标记
            Groups.VERSION,         //Groups_Version         //组版本
            Groups.SOURCE_ID,       //Groups_SourceId        //组资源Id
            Groups.RES_PACKAGE,     //Groups_ResPackage      //组资源包名称
            Groups.TITLE,           //Groups_Title           //组名称
            Groups.TITLE_RES,       //Groups_TitleRes        //组资源名称
            Groups.GROUP_VISIBLE,   //Groups_GroupVisible    //组可见
            Groups.SYNC1,           //Groups_Sync1           //组同步适配器1
            Groups.SYNC2,           //Groups_Sync2           //组同步适配器2
            Groups.SYNC3,           //Groups_Sync3           //组同步适配器3
            Groups.SYNC4,           //Groups_Sync4           //组同步适配器4
            Groups.SYSTEM_ID,       //Groups_SystemId        //组系统Id
            Groups.DELETED,         //Groups_Deleted         //组删除标记
            Groups.NOTES,           //Groups_Notes           //组注释
            Groups.SHOULD_SYNC,     //Groups_ShouldSync      //组是否要同步
            Groups.FAVORITES,       //Groups_Favorites       //搜藏用户时自动加入改组
            Groups.AUTO_ADD,        //Groups_AutoAdd         //创建用户时自动加入该组
    };

    //群组信息Mimetype，来自：D:\Android\Android-SDK-Windows\sources\android-29\android\provider\ContactsContract.java
    //@Override
    //public Entity getEntityAndIncrementCursor(Cursor cursor) throws RemoteException {
    //    // we expect the cursor is already at the row we need to read from
    //    final ContentValues values = new ContentValues();
    //    DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, values, _ID);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, ACCOUNT_NAME);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, ACCOUNT_TYPE);
    //    DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, values, DIRTY);
    //    DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, values, VERSION);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, SOURCE_ID);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, RES_PACKAGE);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, TITLE);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, TITLE_RES);
    //    DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, values, GROUP_VISIBLE);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, SYNC1);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, SYNC2);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, SYNC3);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, SYNC4);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, SYSTEM_ID);
    //    DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, values, DELETED);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, NOTES);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, SHOULD_SYNC);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, FAVORITES);
    //    DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, values, AUTO_ADD);
    //    cursor.moveToNext();
    //    return new Entity(values);
    //}

    // 群组信息列表头
    public GroupHeader() {
        m_jsonHeader = new JSONObject(new LinkedHashMap());     //Map会自动排序，用Vector也不合理，所以用JSONObject

        String arr2jsonHeader[][] = {
                {"Groups_Id", Groups._ID},                         //组Id
                {"Groups_AccountName", Groups.ACCOUNT_NAME},       //组账户名
                {"Groups_AccountType", Groups.ACCOUNT_TYPE},       //组账户类型
                {"Groups_Dirty", Groups.DIRTY},                    //组废弃标记
                {"Groups_Version", Groups.VERSION},                //组版本
                {"Groups_SourceId", Groups.SOURCE_ID},             //组资源Id
                {"Groups_ResPackage", Groups.RES_PACKAGE},         //组资源包名称
                {"Groups_Title", Groups.TITLE},                    //组名称
                {"Groups_TitleRes", Groups.TITLE_RES},             //组资源名称
                {"Groups_GroupVisible", Groups.GROUP_VISIBLE},     //组可见
                {"Groups_Sync1", Groups.SYNC1},                    //组同步适配器1
                {"Groups_Sync2", Groups.SYNC2},                    //组同步适配器2
                {"Groups_Sync3", Groups.SYNC3},                    //组同步适配器3
                {"Groups_Sync4", Groups.SYNC4},                    //组同步适配器4
                {"Groups_SystemId", Groups.SYSTEM_ID},             //组系统Id
                {"Groups_Deleted", Groups.DELETED},                //组删除标记
                {"Groups_Notes", Groups.NOTES},                    //组注释
                {"Groups_ShouldSync", Groups.SHOULD_SYNC},         //组是否要同步
                {"Groups_Favorites", Groups.FAVORITES},            //搜藏用户时自动加入改组
                {"Groups_AutoAdd", Groups.AUTO_ADD},               //创建用户时自动加入该组
        };
        Arr2Json(arr2jsonHeader, m_jsonHeader);
    }

    private void Arr2Json(String[][] s2Arr, JSONObject jsonObject) {
        try {
            for (String[] sArr : s2Arr) {
                jsonObject.put(sArr[0], sArr[1]);   // sArr[0] 用于存放 mimetype 类型可读标题，sArr[1] 用于存放 mimetype 类型编码
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
