package com.example.travelapp.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.travelapp.LiveDataBus;
import com.example.travelapp.MyApplication;
import com.example.travelapp.R;
import com.example.travelapp.bean.ChatMsg;
import com.example.travelapp.bean.DataContent;
import com.example.travelapp.bean.Dialog;
import com.example.travelapp.bean.Message;
import com.example.travelapp.database.ChatDataBase;
import com.example.travelapp.utils.baidu.GsonUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebSocketUtils {
    private static WebSocketUtils instance = null;
    WebSocketClient client;
    //转成的要操作的数据
    URI uri;
    //后台连接的url
    String address = "ws://10.242.253.199:8888/chat";
    //连接是否关闭，用于判断是主动关闭还是断开，断开需要重连
    boolean isHandClose;
    //记录心跳重联的次数
    int reconnectTimes;
    //心跳timer
    Disposable timerHeart;
    //重连timer
    Disposable timer;

    //懒加载单例模式，synchronized确保线程安全。
    // 适合没有多线程频繁调用的情况，多线程频繁调用的时候，可以用双重检查锁定DCL、静态内部类、枚举 等方法
    public static synchronized WebSocketUtils getInstance() {
        if (instance == null) {
            instance = new WebSocketUtils();
        }
        return instance;
    }

    public void initSocket() {
        isHandClose = false;
        startSocket();
    }

    public void startSocket() {
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (null == client || !client.isOpen()) {
            client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    reconnectTimes = 0;
                    if (timer != null) {
                        timer.dispose();
                    }
                    //socket连接成功
                    registerWebSocket();

                    getOfflineMessage();
                    //50秒钟进行一次心跳，防止异常断开
                    timerHeart = Observable.interval(50, TimeUnit.SECONDS).subscribe(aLong -> {
                        DataContent dataContent = new DataContent(4,null);
                        sendMessage(GsonUtils.toJson(dataContent));
                    });
                }

                @Override
                public void onMessage(String s) {
                    //LogUtils.eTag(TAG, "收到消息", s);
                    DataContent data = GsonUtils.fromJson(s,DataContent.class);
                    ChatMsg chatMsg = data.getChatMsg();
                    changeSigned(chatMsg.getDialogId().toString());
                    Message message = new Message(chatMsg.getContent(),
                            chatMsg.getFromUserId(),
                            chatMsg.getToUserId(),2);
                    ChatDataBase.getDatabase(MyApplication.Companion.getContext())
                            .getChatDao().insert(message);
                    //将ids发送给chatActivity然后变成已读
                    LiveDataBus.INSTANCE.with("unread_ids").postValue(chatMsg.getDialogId().toString());
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    if (isHandClose) {
                        reconnectTimes = 0;
                        if (timer != null) {
                            timer.dispose();
                        }
                        if (timerHeart != null) {
                            timerHeart.dispose();
                        }
                    } else {
                        //非正常关闭进行重连，重连每次30秒，最多十次
                        timer = Observable.timer(30, TimeUnit.SECONDS).subscribe(aLong -> {
                            reconnectTimes = reconnectTimes + 1;
                            startSocket();
                        });
                        //重联超过十次没成功，结束重连
                        if (reconnectTimes > 10) {
                            reconnectTimes = 0;
                            timer.dispose();
                            if (timerHeart != null) {
                                timerHeart.dispose();
                            }
                        }
                    }
                }

                @Override
                public void onError(Exception e) {

                }
            };
            client.connect();
        }
    }

    //向服务器发送消息
    public void sendMessage(String s) {
        if (client == null || client.isClosed() || !client.isOpen()) {
            //LogUtils.eTag(TAG, "未建立实时通信");
            return;
        }
        try {
            client.send(s);
            //LogUtils.eTag(TAG, "发送消息：", s);
        } catch (Exception e) {
            e.printStackTrace();
            startSocket();
        }
    }

    //关闭socket
    public void closeSocket() {
        isHandClose = true;
        if (client != null) {
            client.close();
            client = null;
        }
    }

    //向服务器发送初始化信息
    void registerWebSocket(){
        //{"action": 1, "chatMsg": { "fromUserId": "123", "toUserId": null, "content": null}}
        ChatMsg chatMsg = new ChatMsg(UserUtils.Companion.getUserid(),null,null);
        DataContent dataContent = new DataContent(1,chatMsg);
        sendMessage(GsonUtils.toJson(dataContent));
    }

    //我发送给别人的聊天
    public void sendData(ChatMsg chatMsg) {
        //{"action": 2, "chatMsg": { "fromUserId": "123", "toUserId": "211005BC84519XWH", "content": "我想问问你去南京的旅游攻略可以吗"}}
        DataContent dataContent = new DataContent(2,chatMsg);
        sendMessage(GsonUtils.toJson(dataContent));
    }

    //消息改为签收状态
    public void changeSigned(String s){
        DataContent dataContent = new DataContent(3,null);
        dataContent.setExtend(s);
        sendMessage(GsonUtils.toJson(dataContent));
    }

    //离线消息签收并保存到数据库
    public void getOfflineMessage(){
        OkHttpClient client = new OkHttpClient.Builder().build();
        String url = Objects.requireNonNull(MyApplication.Companion
                .getContext()).getString(R.string.serverBasePath)+"dialog/offlineMessage?";
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        builder.addQueryParameter("toUserId",UserUtils.Companion.getUserid());
        Request request = new Request.Builder().url(builder.build()).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //异步请求失败之后的回调
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //异步请求成功之后的回调
                JSONArray json = JSONArray.parseArray(response.body().string());
                List<Dialog> list = JSONObject.parseArray(
                        json.toJSONString(), Dialog.class);
                StringBuilder ids = new StringBuilder();
                for(Dialog dialog:list){
                    ids.append(dialog.getId()).append(",");
                    Message message = new Message(dialog.getContent(),
                            dialog.getFromUserId(),
                            dialog.getToUserId(),2);
                    ChatDataBase.getDatabase(MyApplication.Companion.getContext())
                            .getChatDao().insert(message);
                }
                changeSigned(ids.toString());
                //将ids发送给chatActivity然后变成已读
                LiveDataBus.INSTANCE.with("unread_ids").postValue(ids.toString());
            }
        });
    }
}
