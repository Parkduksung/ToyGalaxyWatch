package com.example.toygalaxywatch.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.toygalaxywatch.App;
import com.example.toygalaxywatch.MainActivity;
import com.example.toygalaxywatch.R;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * 헬로팩토리 메세징 서비스.
 */
public class HfRtmService extends Service {
    ///////////////////////////////////
    // Constant
    ///////////////////////////////////
    public static final String SERVER_URL = "http://msg.hellobell.net:8080/hb_staff";
    //    public static final String SERVER_URL = "http://hfactory.asuscomm.com:8080/hb_staff";
    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    private static final int MSG_NEW_ORDER = 0;
    private static final int MSG_STATE_CHANGED = 1;
    private static final String COMMAND_REQ_JOIN = "req_join";
    private static final String COMMAND_RES_JOIN = "res_join";
    private static final String COMMAND_RCV_EVENT = "event";
    private static final String PARM_BAND_NO = "band_no";
    private static final String TAG = "PUSH-RTM";

    ///////////////////////////////////
    // Member Variables
    ///////////////////////////////////
    private final IBinder mBinder = new HfMessageServiceBinder();

    //private List<BeaconCallback> mCallbacks;
    //private BleScanService mScanService;
    //MessageHandler mHandler;
    private Socket mSocket;
    private String mToken = null;


    public HfRtmService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();


//        // 안드로이드 O 이상 foreground service
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            final String strId = "hellobell_staff_nofifaction_ch";//getString(R.string.noti_channel_id);
//            final String strTitle = getString(R.string.app_name);
//            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            NotificationChannel channel = notificationManager.getNotificationChannel(strId);
//            if (channel == null) {
//                channel = new NotificationChannel(strId, strTitle, NotificationManager.IMPORTANCE_HIGH);
//                notificationManager.createNotificationChannel(channel);
//            }
//
//            Notification notification = new NotificationCompat.Builder(this, strId).build();
//            startForeground(1, notification);
//        }
        startForegroundService();

        //mHandler = new MessageHandler();
        registerReceiver();

    }

    private void startForegroundService() {
        Notification notification = getNotification();
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    private Notification getNotification() {
        final String strTitle = "Test";
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        CharSequence title = "duksung";

        Notification notification = new NotificationCompat.Builder(this, "hellobell_tablet")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText("메시지 수신 대기중...")
                .setContentIntent(pendingIntent)
                .build();

        // 안드로이드 O 이상 foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = notificationManager.getNotificationChannel("hellobell_tablet");
            if (channel == null) {
//                channel = new NotificationChannel(CHANNEL_ID, strTitle, NotificationManager.IMPORTANCE_HIGH);
                channel = new NotificationChannel("hellobell_tablet", strTitle, NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
        }

        return notification;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    // add action case..
//                    case Config.BR_LOGIN:
//                        //connect();
//                        initSocket("BR_LOGIN");
//                        break;
//                    case Config.BR_LOGOUT:
//                        disconnect();
//                        break;
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
        unregisterReceiver();

        // 안드로이드 O 이상 foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    /**
     * 서비스 바인더
     */
    public class HfMessageServiceBinder extends Binder {
        public HfRtmService getServiceInstance() {
            return HfRtmService.this;
        }
    }

    ///////////////////////////////////
    // RECEIVER 
    ///////////////////////////////////

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        initSocket("onStartCommand");
        //connect();
        return START_STICKY;
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(Config.BR_LOGIN);
//        intentFilter.addAction(Config.BR_LOGOUT);
        // add actions..
        registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }


    ///////////////////////////////////
    // Private method 
    ///////////////////////////////////

    public void initSocket(String log) {
        //            mSocket = IO.mSocket(SERVER_URL);
        mSocket = App.Companion.getInstance().getSocket();
        if (mSocket == null) {
            printLog(log + " > SOCKET이 NULL입니다. Application에서 URL을 확인해보세요.");
            return;
        }
        // 반드시! 소켓에 콜백큐를 클리어 해준다.
        // 해주지 않으면 로그인/아웃 할때마다 1회 요청당 응답이 제곱으로 늘어난다.
        mSocket.off();

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                printLog("EVENT_CONNECT");
//                if (!TextUtils.isEmpty(mToken)) {
//                    printLog("EVENT_CONNECT > 토큰이 살아있어 join 안함");
//                    return;
//                }

                requestJoin();

//				    mSocket.disconnect();
            }
        }).on(COMMAND_RES_JOIN, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    String strData = args[0].toString();
                    JSONObject data = new JSONObject(strData);
                    mToken = data.getString("token");

                    String log = ">>>>> " + COMMAND_RES_JOIN;


                    printLog(log);

                } catch (Exception e) {
                    e.printStackTrace();
                    printLog(">>>>> " + COMMAND_RES_JOIN + " ERROR : " + e.getMessage());
                    //sendBrConnection("res_join ERROR : " + e.getMessage());
                }

            }
        }).on(COMMAND_RCV_EVENT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //JSON
                try {
                    String strData = args[0].toString();
                    //JSONObject jsonObject = new JSONObject(strData);
                    onEventRecieved(strData);
                } catch (Exception e) {
                    // Error Handling
                    e.printStackTrace();
                }

            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                printLog("RTM : EVENT_DISCONNECT");
                mToken = null;
            }
        });

        printLog(log + " > CONNECT 요청");
        connect();

    }

    private void requestJoin() {
        if (mSocket == null) {
            printLog("requestJoin > Socket이 NULL입니다. ");
            return;
        }

        if (!mSocket.connected()) {
            printLog("requestJoin > Socket이 연결되어 있지 않습니다. ");
            return;
        }

        final int bandNo = 1;
        if (bandNo < 0) {
            printLog("requestJoin > BandNo가 유효하지 않습니다. : " + bandNo);
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put(PARM_BAND_NO, bandNo);
            mSocket.emit(COMMAND_REQ_JOIN, json.toString());

            String messge;

            messge = "<<<<< SEND : requestJoin";

            printLog(messge);

        } catch (JSONException e) {
            String messge = "CONNECT ERROR : " + e.getMessage();
            printLog(messge);
            //sendBrConnection(messge);
        }
    }

//    private void sendBrConnection(String str) {
//        Intent i = new Intent(Config.BR_SOCKET);
//        //i.putExtra(Config.EXTRA_SOCKET, str);
//        Config.saveToken(str);
//        sendBroadcast(i);
//    }

//    private void sendBrMessage(String json) {
//        Intent i = new Intent(Config.BR_NEW_CALL);
//        i.putExtra(Config.EXTRA_ITEM, json);
//        //mRcvCount++;
//        //i.putExtra(Config.EXTRA_TEST_COUNT, mRcvCount);
//        int cnt = Config.getRcvCount();
//        Config.saveRcvCount(++cnt);
//
//        sendBroadcast(i);
//    }

    /**
     * 서버에서 온 메시지 규격은 다음과 같다
     * 예시 : {"cmd":"req", data={"idx":912}}
     *
     * @param json
     * @throws Exception
     */
    private void onEventRecieved(String json) throws Exception {

        String log = ">>>> EVENT MSG : ";

        printLog(log);

        // ListManagerService로 전달
        postEvent(json);
    }

//    private void parseCommandReq(JSONObject jsonObject) throws JSONException {
//        int orderSeq = jsonObject.getInt("idx");
//        int currentSeq = Util.getPrefInt(getApplicationContext(), Config.PREF_ORDER_SEQ, -1);
//        if (orderSeq != currentSeq) {
//            Util.savePref(getApplicationContext(), Config.PREF_ORDER_SEQ, orderSeq);
//            notiNewOrder();
//        }
//    }
//
//    private void notiNewOrder() {
//        if (getApplication() instanceof HfApp) {
//            if (((HfApp) getApplication()).isBackground()) {
//                Intent intent = new Intent(this, HomeActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                startActivity(intent);
//
//                ExLog.print("새주문 >>>> HomeActivity 호출 ");
//            } else {
//                // Broadcast
//                Intent i = new Intent(Config.BR_NEW_CALL);
//                sendBroadcast(i);
//                ExLog.print("새주문 >>>> BR_NEW_CALL  ");
//            }
//        }
//    }
//
//    private void parseCommandState(JSONObject jsonObject) throws JSONException {
//        int idx = jsonObject.getInt("idx");
//        String state = jsonObject.getString("stat");
//
//        notiStateChanged(idx, state);
//    }
//
//    private void notiStateChanged(int idx, String state) {
//        // TODO : getWaitTagJob 처리
//        ApiCallItem currentOrder = Config.getWaitTagJob();
//        if (currentOrder == null) {
//            return;
//        }
//        if (currentOrder.getCall_no() == idx && OrderStatus.getStatus(state) == OrderStatus.COMPLETE) {
//            // Broadcast
//            Intent i = new Intent(Config.BR_STATUS_COMPLETE);
//            sendBroadcast(i);
//            ExLog.print("다른 단말에서 완료 처리 했음 >>>> BR_STATUS_COMPLETE  ");
//        }
//    }

    ///////////////////////////////////
    // Bind public method
    ///////////////////////////////////
    public void connect() {
        if (mSocket != null ) {
            mSocket.connect();
        }
    }

    public void disconnect() {
        if (mSocket != null) {
            mSocket.disconnect();
        }
    }

    ///////////////////////////////////
    // Handler 
    ///////////////////////////////////

//    class MessageHandler extends Handler {
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//
//            try {
//                JSONObject data = (JSONObject) msg.obj;
//                switch (msg.what) {
//                    case MSG_NEW_ORDER: {
////                        parseCommandReq(data);
//                        break;
//                    }
//                    case MSG_STATE_CHANGED: {
////                        parseCommandState(data);
//                        break;
//                    }
//                }
//            } catch (Exception e) {
//                ExLog.e("HF_MESSAGE parsing error : " + e.getMessage());
//                if (BuildConfig.DEBUG) {
//                    Toast.makeText(HfRtmService.this, "message parsing error", Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }
//
//
//        }
//    }

    ///////////////////////////////////
    // Event Bus 
    ///////////////////////////////////
    public void postEvent(String json) {
        //EventBus.getDefault().post(new ItemPushEvent(ItemPushEvent.FROM_RTM, str));
//        if (Config.isLogin()) {
//            // UI쪽에 알려줌.
//            ExLog.print("========= RTM : SEND INTENT_PUSH_REFRESH ========");
//            Intent intent = new Intent(Config.BR_PUSH_RTM);
//            intent.putExtra(Config.EXTRA_PUSH_JSON, json);
//            sendBroadcast(intent);
//        }
    }

    ///////////////////////////////////
    // ETC 
    ///////////////////////////////////
    private void printLog(String string) {
        String log = getLogFormatString(string);
    }

    private String getLogFormatString(String string) {
//        String socketId = "NULL";
//        if (mSocket != null) {
//            socketId = " >> BAND : " + Config.getBandNo() + " >> OBJ : " + mSocket.toString() + " >> ID : " + mSocket.id();
//        }

        String log = "\n+~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~+" +
//                "\n| " + TAG + " : " + string.replace("\n", "\n| ") +
                "\n| " + TAG + " : " + string +
//                "\n| " + socketId +
                "\n+~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~+";

        return log;
    }

}
