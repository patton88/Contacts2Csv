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
    public static final String SUCCESS_INSERT = "导入成功 %d 条，失败 %d 条，用时%s";
    public static final String FAIL_INSERT = "导入联系人失败";

    public static final String OUTPUT_COUNT_UPDATE = "导出总数%d条，成功 %d 条，失败 %d 条，用时%s";
    public static final String SUCCESS_OUTPUT_AGGREGATE = "导出成功 %d 条，失败 %d 条，合并 %d 条，用时%s";
    public static final String SUCCESS_OUTPUT = "导出成功 %d 条，失败 %d 条，用时%s";
    public static final String FAIL_OUTPUT = "导出联系人失败";

    public static final String DEL_COUNT_UPDATE = "删除总数%d条，成功 %d 条，失败 %d 条，用时%s";
    public static final String SUCCESS_DEL = "删除联系人成功 %d 条，失败 %d 条，用时%s";
    public static final String FAIL_DEL = "删除联系人失败";

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

    public static final String HELP_MESSAGE =
            "CSV联系人导入导出工具 1.2.1安卓版(20200225)\n" +
                    "\n" +
                    "QQ: 153248043\n" +
                    "Email: ybmj@vip.163.com\n" +
                    "Blog: https://www.cnblogs.com/ybmj/\n" +
                    "\n" +
                    "    许多手机、平板等设备自带联系人备份功能都有诸多限制，只适用于特定手机导入导出、备份文件加密不能编辑加工等。本软件可以帮你快速备份和恢复联系人，不用担心号码遗失，软件操作简单，使用方便。\n" +
                    "1、主要特点\n" +
                    "    (1)、导出的信息是 csv(Comma Separated value) 格式文本文件，可以使用文本编辑器、Excel 等进行编辑处理\n" +
                    "    (2)、导出时将自动生成类似 Contacts_X.txt 的不重复文件名，避免覆盖已有文件\n" +
                    "    (3)、导出时可选择修正三星 Galaxy S10(安卓10) 导出少8条记录的问题。其他型号和版本的设备建议不要选，以免出现导出数据问题\n" +
                    "    (4)、可以从备份文件中导入联系人记录\n" +
                    "    (5)、导入时可以自动选择类似 Contacts_X.txt 的最新文件名，也可以浏览选择指定文件进行导入\n" +
                    "    (6)、导入时已禁用安卓自带的同名聚合，避免丢失许多同名联系人信息\n" +
                    "    (7)、导入和删除时，可以选择是否处理联系人群组信息\n" +
                    "    (8)、导入导出时，可选是否处理用户头像，图像格式可选 png、jpg，图像质量可选 1-100\n" +
                    "    (9)、导入导出时，会剔除无任何信息的空白记录，可以选择剔除仅含姓名、无其他信息的记录\n" +
                    "    (10)、导入导出时，会自动为匿名用户添加类似 anonymous_X 的用户名\n" +
                    "    (11)、导入导出时，会自动将文件路径复制到剪贴板中，以供用户使用\n" +
                    "    (12)、导入导出时，程序将会自动处理安卓联系人14个类型100多个字段的信息\n" +
                    "    (13)、可以快速删除通讯录所有联系人和群组信息\n" +
                    "    (14)、在导入导出和删除众多记录时，能够显示处理总数，实时显示处理成功和失败的记录条数、已用时间等信息\n" +
                    "\n" +
                    "2、导出联系人\n" +
                    "    程序启动后，可以直接点击“导出联系人”按钮，程序会自动生成并在文件路径栏显示类似 Contacts_X.txt 的不重复最新文件名，避免覆盖已有文件，同时自动将路径复制到剪贴板供用户使用。在确认对话框中，点击“确认”按钮后程序便开始导出联系人。在导出众多记录时，程序会显示导出总数，实时显示处理成功和失败的记录条数、已用时间等信息。\n" +
                    "    打开导出文件时注意选择 UTF-8 格式，不然可能会出现乱码。不同厂家和不同版本的安卓设备，导出目录会有所不同。可以点击程序中的“唯一文件名(导出用)”按钮，程序便会自动生成并在文件路径栏显示不重复的唯一文件名称，同时自动将路径复制到剪贴板供用户使用。比如：\n" +
                    "    /storage/sdcard/Android/data/com.example.contacts2csv/files/Download/Contacts_7.txt\n" +
                    "\n" +
                    "3、删除联系人\n" +
                    "    在删除联系人记录前，请确认已经做好备份。可以点击“删除所有联系人”按钮，在出现的确认对话框中，用户可以选择是否删除联系人群组信息，当点击“确认”按钮后，程序便开始删除联系人信息。在删除众多记录时，会显示删除总数，实时显示处理成功和失败的记录条数、已用时间等信息。\n" +
                    "\n" +
                    "4、导入联系人\n" +
                    "    程序启动后，可以直接点击“导入联系人”按钮，程序会从自身目录下已有文件中自动选择类似 Contacts_X.txt 的最新文件并显示在文件路径栏，同时自动将路径复制到剪贴板供用户使用。在确认对话框中，用户可以选择是否导入联系人群组信息，当点击“确认”按钮后，程序将首先删除设备上所有联系人，然后开始从文件中导入联系人信息。在导入众多记录时，程序会显示导入总数，实时显示处理成功和失败的记录条数、已用时间等信息。\n" +
                    "    注意，在执行导入联系人操作前，请确认已经做好备份。因为在导入联系人时，程序将首先删除设备上所有联系人，然后再从文件中导入联系人信息。\n" +
                    "    点击“最新文件名(导入用)”按钮，程序便会查找自身目录下最新文件名并显示在文件路径栏，同时自动将路径复制到剪贴板供用户使用。比如：\n" +
                    "    /storage/sdcard/Android/data/com.example.contacts2csv/files/Download/Contacts_6.txt\n" +
                    "    另外，用户也可以点击程序中的“浏览”按钮，选择指定文件进行导入。注意，点击“浏览”按钮后，需要在弹出的系统文件管理器中点击右侧“三个竖点“按钮并选中 Show SD card 后，才能浏览选择 SD 卡中的文件。不同厂家和不同版本的安卓设备，设置方法可能会有所不同。\n" +
                    "    程序启动时，会自动查找自身目录下最新文件并显示在文件路径栏，同时自动将路径复制到剪贴板供用户使用。若没有找到类似 Contacts_X.txt 的最新文件，文件路径栏便只会显示目录路径。\n" +
                    "    \n" +
                    "5、程序私有目录\n" +
                    "    注意，该程序的用户文件都默认保存在自身私有目录中，程序卸载后都将被删除。所以，卸载前务必做好用户文件的备份工作。\n" +
                    "    不同厂家和不同版本的安卓设备，程序私有目录会有所不同。另外，程序中显示的程序私有目录与手机上用户可访问的目录也会有所不同。比如在三星S10手机上，程序中显示的路径是：\n" +
                    "    /storage/emulated/0/Android/data/com.example.contacts2csv/files/Download/Contacts_1.txt\n" +
                    "    用户可以访问的路径是：\n" +
                    "    /mnt/sdcard/Android/data/com.example.contacts2csv/files/Download/Contacts_1.txt\n" +
                    "\n" +
                    "6、适用环境\n" +
                    "    本程序适用于安卓5.0(API 21)及以上的手机、平板电脑等电子设备，屏幕分辨率建议1080×1920及以上。\n" +
                    "\n" +
                    "7、免责申明：用户可自行斟酌选用该程序，若转载请注明出处。对一切后果，作者不承担任何责任！\n"
            ;

}
