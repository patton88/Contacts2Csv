package com.example.contacts2csv;


public class ExtraStrings {
    //dialog message
    public static final String DIALOG_OK = "确认";
    public static final String DIALOG_CANCEL = "取消";
    public static final String WARNDIALOG_TITLE = "警告";
    public static final String INSERT_WARNDIALOG_MESSAGE = "该操作将删除所有联系人和群组信息，然后从下面文件中(路径将放入剪贴板)导入新的联系人，是否执行？";
    public static final String INSERT_WARNDIALOG_MESSAGE_NoGroup = "该操作将删除所有联系人信息，然后从下面文件中(路径将放入剪贴板)导入新的联系人，是否执行？";
    public static final String OUTPUT_WARNDIALOG_MESSAGE = "\n    该操作将把所有联系人和群组信息导出到下面文件中(路径将放入剪贴板)，是否执行？";
    public static final String DEL_ALL_WARNDIALOG_MESSAGE = "该操作将删除所有联系人和群组信息，是否执行？";
    public static final String DEL_ALL_WARNDIALOG_MESSAGE_NoGroup = "该操作将删除所有联系人信息，是否执行？";
    public static final String HELP_DIALOG_TITLE = "帮助信息";

    //insert output status strings
    public static final String NO_TEXT = "";
    public static final String NULL_TEXT = "";
    public static final String FAIL_EDITTEXT_NOT_INPUT = "请输入文件名";
    public static final String FAIL_FIRE_NOT_EXIST = "导入联系人失败，该文件不存在";
    public static final String FAIL_READ_FIRE = "读取文件出错";

    public static final String INSERT_COUNT_UPDATE = "导入总数%d条，成功 %d 条，失败 %d 条，用时%s";
    public static final String SUCCESS_INSERT = "导入联系人成功 %d 条，失败 %d 条，合并 %d 条，用时%s";
    public static final String FAIL_INSERT = "导入联系人失败";

    public static final String OUTPUT_COUNT_UPDATE = "导出总数%d条，成功 %d 条，失败 %d 条，用时%s";
    public static final String SUCCESS_OUTPUT = "导出联系人成功 %d 条，失败 %d 条，用时%s";
    public static final String FAIL_OUTPUT = "导出联系人失败";

    public static final String DEL_COUNT_UPDATE = "删除总数%d条，成功 %d 条，失败 %d 条，用时%s";
    public static final String SUCCESS_DEL = "删除联系人成功 %d 条，失败 %d 条，用时%s";
    public static final String FAIL_DEL = "删除联系人失败";

    public static final String HELP_MESSAGE =
            "原作者 Jose Mourinho \n" +
                    "QQ:546771679\n" +
                    "使用说明: \n" +
                    "导入联系人: \n" +
                    "将文件放在sd卡根目录下，然后输入文件名即可导入.文件格式如下:\n " +
                    "姓名 手机号 住宅电话 \n" +
                    "每一列以空格分割，姓名不能为空，手机号和住宅电话可以为空 \n" +
                    "***重复导入会创建新的联系人，请慎用！ \n\n" +
                    "导出联系人： \n" +
                    "默认的导出文件名为'我的联系人.txt',存放在文件根目录 \n" +
                    "导出练习人信息只包含姓名，手机号和住宅电话，可能会丢失其他信息 \n\n" +
                    "***输出文件请用word打开，打开时选择字符编码utf-8，否则会出现乱码" +
                    "***连接USB的时候无法导入导出!";

    //insert output status
    public static final int INSERT_FAIL = -1;
    public static final int INSERT_SUCCESS = 1;
    public static final int INSERT_COUNTING = 11;
    public static final int OUTPUT_FAIL = -2;
    public static final int OUTPUT_SUCCESS = 2;
    public static final int OUTPUT_COUNTING = 12;
    public static final int DEL_FAIL = -3;
    public static final int DEL_SUCCESS = 3;
    public static final int DEL_COUNTING = 13;

    //charset
    public static final String CHARSET_GBK = "gbk";
    public static final String CHARSET_UTF8 = "utf-8";

    //os
    public static final String OS_WIN = "win";
    public static final String OS_LINUX = "linux";

    //format strings
    public static final String ENTER_WIN = "\n\r";
    public static final char ENTER_CHAR_LINUX = '\n';
    public static final String COMMA_STRING = ",";

    public static final int BUFFER_SIZE = 1 << 24;
    public static final String SPACE_STRING_1 = " ";
    public static final String SPACE_STRING_2 = SPACE_STRING_1 + SPACE_STRING_1;
    public static final String SPACE_STRING_4 = SPACE_STRING_2 + SPACE_STRING_2;
    public static final String SPACE_STRING_8 = SPACE_STRING_4 + SPACE_STRING_4;
    public static final int NAME_LENGTH = 20;
    public static final int MOBILE_NUM_LENGTH = 11;
    public static final String NO_MOBILE_NUM = SPACE_STRING_8 + SPACE_STRING_2 + SPACE_STRING_1;//11 spaces

    //dialog id
    public static final int DIALOG_TYPE_HELP = 0;
    public static final int DIALOG_TYPE_INSERT = 1;
    public static final int DIALOG_TYPE_OUTPUT = 2;
    public static final int DIALOG_TYPE_DEL_ALL = 3;

    //database id
    public static final int HOME_ID = 1;
    public static final int MOBILE_ID = 2;

    //output file and path
    public static final String FILE_NAME_PARENT = "/mnt/sdcard/";
    public static final String OUTPUT_FILENAME = "Contacts_1.txt";
    public static final String OUTPUT_GROUP_INFO_FILENAME = "Group_1.txt";
    public static final String OUTPUT_PATH = FILE_NAME_PARENT + OUTPUT_FILENAME;
    public static final String JSON_OUTPUT_FILENAME = "JsonHeader结构.txt";
    public static final String JSON_OUTPUT_PATH = FILE_NAME_PARENT + JSON_OUTPUT_FILENAME;

    //regular
    public static final String SPACE_REGULAR = "\\s+";
    public static final String NUM_REGULAR = "^[0-9]*$";
    public static final String HOME_REGULAR_01 = "\\d{3,4}[-]{0,1}\\d{7,8}";
    public static final String HOME_REGULAR_02 = "\\d{7,8}";
}
