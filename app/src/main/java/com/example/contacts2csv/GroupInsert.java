package com.example.contacts2csv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Groups;

import static com.example.contacts2csv.MainActivity.m_MA;

import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.text.TextUtils;

/**
 * @author glsite.com
 * @version 1.0.0$
 * @des Input Groups
 * @updateAuthor MinJun, 20200210$
 * @updateDes Input Groups
 */
public class GroupInsert {

    // 判断是否存在名为 groupTitle 的联系人群组，若没有便新建。并返回该群组 ID
    public String createGroup(String groupTitle, Context context) {
        String groupId = getGroupId(groupTitle, context);
        if (TextUtils.isEmpty(groupId)) {
            ContentValues values = new ContentValues();
            values.put(Groups.TITLE, groupTitle);
            context.getContentResolver().insert(Groups.CONTENT_URI, values);
            groupId = getGroupId(groupTitle, context);
            //System.out.println("createGroup Success, " + "groupTitle : " + groupTitle + "groupId : " + groupId);
        }
        return groupId;
    }

    // 删除所有群组
    public void delAllGroup(Context context) {
        Cursor cursor = context.getContentResolver().query(Groups.CONTENT_URI, null, null, null, null);
        while (cursor != null && cursor.moveToNext()) {
            String groupId = cursor.getString(cursor.getColumnIndex(Groups._ID));
            String where = Groups._ID + " = ?";
            String[] where_params = new String[]{groupId};
            context.getContentResolver().delete(Groups.CONTENT_URI, where, where_params);
        }
        cursor.close();
    }

    // This is the code to get the ID of the group:
    // 也可以判断该群组是否存在
    public String getGroupId(String groupTitle, Context context) {
        String groupId = "";
        Uri uri = Groups.CONTENT_URI;
        String where = String.format("%s = ?", Groups.TITLE);
        String[] whereParams = new String[]{groupTitle};
        String[] selectColumns = {Groups._ID};
        Cursor cursor = context.getContentResolver().query(uri, selectColumns, where, whereParams, null);

        try {
            if (cursor.moveToFirst()) {
                groupId = cursor.getString(0);
            }
        } finally {
            cursor.close();
        }
        if (cursor != null) {
            cursor.close();
        }
        return groupId;
    }

    // This is the code to get the Title of the group:
    // 也可以判断该群组是否存在
    public String getGroupTitle(String groupId, Context context) {
        String groupTitle = "";
        Uri uri = Groups.CONTENT_URI;
        String where = String.format("%s = ?", Groups._ID);
        String[] whereParams = new String[]{groupId};
        String[] selectColumns = {Groups.TITLE};
        Cursor cursor = context.getContentResolver().query(uri, selectColumns, where, whereParams, null);

        try {
            if (cursor.moveToFirst()) {
                groupTitle = cursor.getString(0);
            }
        } finally {
            cursor.close();
        }
        if (cursor != null) {
            cursor.close();
        }
        return groupTitle;
    }

    // android将联系人加入群组
    public void addContactToGroup(String contactId, String groupTitle, Context context) {
        // 判断是否存在名为 groupTitle 的联系人群组，若没有便新建。并返回该群组 ID
        String groupId = createGroup(groupTitle, context);

        //judge whether the contact has been in the group
        if (ifExistContactInGroup(contactId, groupId, context)) {
            return;  //the contact has been in the group
        } else {
            ContentValues values = new ContentValues();
            values.put(GroupMembership.RAW_CONTACT_ID, contactId);
            values.put(GroupMembership.GROUP_ROW_ID, groupId);
            values.put(GroupMembership.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
            context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        }
    }

    public boolean ifExistContactInGroup(String contactId, String groupId, Context context) {
        String where = Data.MIMETYPE + " = '" + GroupMembership.CONTENT_ITEM_TYPE
                + "' AND " + Data.DATA1 + " = '" + groupId
                + "' AND " + Data.RAW_CONTACT_ID + " = '" + contactId + "'";
        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, new String[]{Data.DISPLAY_NAME}, where, null, null);
        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }
}
