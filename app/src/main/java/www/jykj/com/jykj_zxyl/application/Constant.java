package www.jykj.com.jykj_zxyl.application;

public class Constant {


    /**
    *
    * 服务器地址
     */
    public final static String SERVICEURL = "https://www.jiuyihtn.com:28081/";
    public final static String LocalUrl = "https://192.168.0.117:28081/";

    /**
     *
     * 身份证识别地址
     */
    public final static String IDCARDSCANNINGURL = "https://ocr.tencentcloudapi.com/";

    /**
     * 身份证识别appID
     */
    public final static String IDCARDSCANNINGAPPID = "1253697945";

    /**
     * 身份证识别secretID
     */
    public final static String IDCARDSCANNINGSECRETID = "AKID3zoSCnZdmhU7Z3xgokaGqFaZWIszgxlR";


    /**
     * 身份证识别secretKEY
     */
    public final static String IDCARDSCANNINGSECRETKEY = "fCqTyLWWPDuQq6D8sT2TjsriiXjnjVAh";


    /**
     * 从相册选择
     */
    public final static int SELECT_PIC_FROM_ALBUM = 1;                  //从相册选择照片

    /**
     *  拍照
     */
    public final static int SELECT_PIC_BY_TACK_PHOTO = 2;                  //从相册选择照片

    /**\
     *
     *  区系统剪裁界面
     */
    public static final int REQUEST_PHOTO_CUT = 3;


    /**
     * umeng appkey
     */
    public static final String UMENG_APPKEY="5feb0616066c80422572525e";
    /**
     * umeng appsecret
     */
    public static final String UMENG_APP_SECRET="8dcf1b0b5dfe1915be8bdc7b3ee54811";


    /**
     *  ImageLoader配置
     */
    public static class Config {
        public static final boolean DEVELOPER_MODE = false;
    }

}
