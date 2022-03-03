package com.example.travelapp.utils.baidu;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.travelapp.utils.Cost;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;

/**
 * 地标识别
 */
public class BaiduLandmark {

    public static String landmark(String filePath) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/landmark";
        try {
            byte[] imgData = FileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");
            String result;
            try(Cost c = new Cost()){
                String param = "image=" + imgParam;
                // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
                String accessToken = BaiduAuthService.getAuth();
                result = HttpUtil.post(url, accessToken, param);
            }
            Log.e("cost: ",result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String landmarkTest() {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/landmark";
        try {
            // 本地文件路径
            //武汉东湖 /sdcard/DCIM/mmexport1640421962144.jpg
            //日本 /sdcard/DCIM/mmexport1640421957182.jpg
            //大笨钟 /sdcard/DCIM/mmexport1640421952786.jpg
            //埃菲尔 /sdcard/DCIM/mmexport1640421947562.jpg
            //长城 /sdcard/DCIM/mmexport1640421935588.jpg

            //长城
            // /sdcard/Tencent/MicroMsg/WeiXin/mmexport1640441411624.jpg no
            // /sdcard/Tencent/MicroMsg/WeiXin/mmexport1640441407159.jpg yes
            // /sdcard/Tencent/MicroMsg/WeiXin/mmexport1640441402887.jpg yes
            // /sdcard/Tencent/MicroMsg/WeiXin/mmexport1640441397860.jpg yes

            //大峡谷
            // /sdcard/Tencent/MicroMsg/WeiXin/mmexport1640443951981.jpg

            //教堂
            // /sdcard/Tencent/MicroMsg/WeiXin/mmexport1640444759877.jpg

            //姬路城
            // /sdcard/Tencent/MicroMsg/WeiXin/mmexport1640767294061.jpg
            // /sdcard/Tencent/MicroMsg/WeiXin/mmexport1640767301417.jpg
            // /sdcard/Tencent/MicroMsg/WeiXin/mmexport1640767306454.jpg
            // /sdcard/Tencent/MicroMsg/WeiXin/mmexport1640767310835.jpg
            // /sdcard/Tencent/MicroMsg/WeiXin/mmexport1640767317261.jpg

            String filePath = "/sdcard/Tencent/MicroMsg/WeiXin/mmexport1640767317261.jpg";
            byte[] imgData = FileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");
            String result;
            try(Cost c = new Cost()){
                String param = "image=" + imgParam;
                // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
                String accessToken = BaiduAuthService.getAuth();
                result = HttpUtil.post(url, accessToken, param);
            }
            Log.e("cost: ",result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
