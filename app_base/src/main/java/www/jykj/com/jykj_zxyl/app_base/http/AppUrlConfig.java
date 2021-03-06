package www.jykj.com.jykj_zxyl.app_base.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:公用App请求Url配置
 *
 * @author: qiuxinhai
 * @date: 2018/5/31 16:10
 */
public class AppUrlConfig {

    /**
     * 线上Api OnLine Key
     */
    static String SERVICE_API_ONLINE_KEY = "service_api_online_key";
    /**
     * 线上Api OnLine Url
     */
    public static String SERVICE_API_ONLINE_URL = "https://www.jiuyihtn.com:38081/";

    /**
     * 线下Api Test Key
     */
    static String SERVICE_API_TEST_KEY = "service_api_test_key";

//    /**线下Api Test key*/
//    public static String SERVICE_API_TEST_URL="http://114.215.137.171:28080/";

    /**
     * 线下Api Test key
     */
    static String SERVICE_API_TEST_URL = "https://www.jiuyihtn.com:28081/";
    /**
     * 线下患者端Api Test key
     */
    static String SERVICE_PATIENT_API_KEY="service_patient_api_key";
    /**
     * 线下患者端Api Test url
     */
    static String SERVICE_PATIENT_API_URL="https://www.jiuyihtn.com:28081/";

    /**
     * 直播 key
     */
    static String SERVICE_LIVE_API_KEY="service_live_api_key";
    /**
     * 直播 url
     */
    static String SERVICE_LIVE_API_URL="https://www.jiuyihtn.com:41041/";
    static String SERVICE_LIVE_API_URL2="https://192.168.0.105:41041/";

    /**
     * 资金池 key
     */
    static String SERVICE_FUND_POOL_API_KEY="service_fund_pool_api_key";
    /**
     * 资金池 url
     */
    static String SERVICE_FUND_POOL_API_URL="https://www.jiuyihtn.com:41051/";


    /**
     * 本地Api key
     */
    static String SERVICE_API_LOCAL_KEY = "service_api_local_key";
    /**
     * 本地Api url
     */
    static String SERVICE_API_LOCAL_URL = "https://192.168.0.119:28081/";

    /**
     * 资金池 key
     */
    static String SERVICE_CAPITALPOOL_KEY="service_capitalpool_api_key";

    /**
     * 资金池Api url
     */
    static String SERVICE_CAPITALPOOL_URL = "https://www.jiuyihtn.com:41051/";

    /**
     * 本地Api key
     */
    static String SERVICE_API_LOCAL_KEY_1 = "service_api_local_key_1";

    /**
     * 本地Api url
     */
    static String SERVICE_API_LOCAL_URL_1 = "https://192.168.0.102:38081/";

    /**
     * 获取所有的url
     * 新·
     *
     * @return map
     */
    public static Map<String, String> getAllApiUrl() {
        Map<String, String> map = new HashMap<>();
        map.put(SERVICE_API_ONLINE_KEY, SERVICE_API_ONLINE_URL);
        map.put(SERVICE_API_TEST_KEY, SERVICE_API_TEST_URL);
        map.put(SERVICE_API_LOCAL_KEY, SERVICE_API_LOCAL_URL);
        map.put(SERVICE_API_LOCAL_KEY_1, SERVICE_API_LOCAL_URL_1);
        map.put(SERVICE_PATIENT_API_KEY,SERVICE_PATIENT_API_URL);
        map.put(SERVICE_LIVE_API_KEY,SERVICE_LIVE_API_URL);
        map.put(SERVICE_FUND_POOL_API_KEY,SERVICE_FUND_POOL_API_URL);
        return map;
    }


}
