package com.onus.demotest.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Person
import android.content.Context
import android.content.Intent
import android.content.LocusId
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.ArrayMap
import android.util.Log
import com.onus.demotest.data.ActivityStateManager
import com.onus.demotest.pages.notification.BindNotificationActivity
import com.onus.demotest.utils.CommonUtils

/**
 * @Author: onuszhao
 * @Date: 2023-10-12 15:22
 * @Description:
 */
class PushMonitorService : NotificationListenerService() {

    private var page: BindNotificationActivity? = null

    private val handler = Handler()

    override fun onCreate() {
        super.onCreate()
        Log.d("onuszhao", "PushMonitorService  onCreate")

        // handler.postDelayed({
        //     activeNotifications.toMutableList().sortedByDescending { it.postTime }.forEach {
        //         // activeNotifications.forEach {
        //         processNotificationInfo(true, it)
        //     }
        // }, 1000)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("onuszhao", "PushMonitorService  onListenerConnected  通知条数:${activeNotifications.size}")
        //当连接成功时调用，一般在开启监听后会回调一次该方法
        page = ActivityStateManager.getCurActivity() as? BindNotificationActivity
        page?.createResultLine("开始监听")

        handler.postDelayed({
            page?.createResultLine("通知条数:${activeNotifications.size}")

            try {
                activeNotifications.toMutableList().sortedByDescending { it.postTime }.forEach {
                    // activeNotifications.forEach {
                    processNotificationInfo(true, it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("onuszhao", "PushMonitorService  onDestroy")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        processNotificationInfo(true, sbn)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?, rankingMap: RankingMap?) {
        super.onNotificationPosted(sbn, rankingMap)
        // processNotificationInfo(true, sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        //当移除一条消息的时候回调，sbn是被移除的消息
        processNotificationInfo(false, sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?) {
        super.onNotificationRemoved(sbn, rankingMap)
        // processNotificationInfo(false, sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?, reason: Int) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        // processNotificationInfo(false, sbn)
    }

    private fun processNotificationInfo(posted: Boolean, sbn: StatusBarNotification?, rankingMap: RankingMap? = null) {
        sbn ?: return
        kotlin.runCatching {
            val pkgName = sbn.packageName   // 获取应用名字
            val time = sbn.postTime
            val timeStr = CommonUtils.format(time)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val pkgName = sbn.opPkg
            }
            val extras = sbn.notification.extras
            // 获取通知消息的title
            var title = ""
            extras.getString(Notification.EXTRA_TITLE)?.let {
                title = it
            }
            // 获取通知消息的内容
            var text = ""
            extras.getString(Notification.EXTRA_TEXT)?.let {
                text = it
            }
            val desc = sbn.describeContents()

            val id = sbn.id
            val key = sbn.key
            val tag = sbn.tag
            val groupKey = sbn.groupKey
            val isAppGroup = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                sbn.isAppGroup
            } else null
            val isGroup = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sbn.isGroup
            } else null
            val overrideGroupKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sbn.overrideGroupKey
            } else null
            val isClearable = sbn.isClearable
            val isOngoing = sbn.isOngoing

            val userId = sbn.userId
            val userDesc = sbn.user.describeContents()
            // val userInfo =  sbn.user.toString()

            val notifyChannelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                sbn.notification.channelId
            } else null
            val notifyShortcutId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                sbn.notification.shortcutId
            } else null
            val notifyCategory = sbn.notification.category
            val notifyVibrate = sbn.notification.vibrate
            val notifySound = sbn.notification.sound
            val notifyDefaults = sbn.notification.defaults
            val notifyFlags = sbn.notification.flags
            val notifyColor = sbn.notification.color
            val notifyContentView = sbn.notification.contentView
            val notifyGroup = sbn.notification.group
            val notifyGroupBehavior = sbn.notification.groupAlertBehavior
            val notifyVisibility = sbn.notification.visibility
            val notifyInfo = sbn.notification.toString()
            val notifyStyle = sbn.notification.extras.getString(Notification.EXTRA_TEMPLATE)

            val badgeIconType = sbn.notification.badgeIconType
            val smallIcon = sbn.notification.smallIcon
            val largeIcon = sbn.notification.largeIcon
            val icon = sbn.notification.icon
            val iconLevel = sbn.notification.iconLevel
            val number = sbn.notification.number
            var bubbleMetadata: Notification.BubbleMetadata? = null
            kotlin.runCatching {
                bubbleMetadata = sbn.notification.bubbleMetadata
            }
            var locusId: LocusId? = null
            kotlin.runCatching {
                locusId = sbn.notification.locusId
            }

            val msg = "${if (posted) "收到消息" else "移除消息"}  $pkgName $timeStr title=$title  text=$text  desc=$desc " +
                "id=$id  key=$key  tag=$tag  groupKey=$groupKey  isAppGroup=$isAppGroup  isGroup=$isGroup  overrideGroupKey=$overrideGroupKey  isClearable=$isClearable  isOngoing=$isOngoing " +
                "userId=$userId  userDesc=$userDesc " +
                "notifyChannelId=$notifyChannelId  notifyShortcutId=$notifyShortcutId  notifyCategory=$notifyCategory  notifyVibrate=$notifyVibrate  notifySound=$notifySound  notifyDefaults=$notifyDefaults " +
                "notifyFlags=$notifyFlags  notifyColor=$notifyColor  notifyContentView=$notifyContentView  notifyGroup=$notifyGroup notifyGroupBehavior=$notifyGroupBehavior notifyVisibility=$notifyVisibility  notifyStyle=$notifyStyle  badgeIconType=$badgeIconType " +
                "smallIcon=$smallIcon largeIcon=$largeIcon icon=$icon  iconLevel=$iconLevel number=$number bubbleMetadata=$bubbleMetadata" +
                " locusId=$locusId"
            Log.d("onuszhao", msg)
            Log.d("onuszhao", "NotifyInfo " + notifyInfo)
            sbn.notification.extras.keySet().forEach {
                val value = sbn.notification.extras.get(it)
                Log.d("onuszhao", "extras  key=$it  value=$value")
            }
            (sbn.notification.extras.get("android.messagingStyleUser") as? Bundle)?.let { bundle ->
                bundle.keySet().forEach {
                    val value = bundle.get(it)
                    Log.d("onuszhao", "messagingStyleUser  bundle  key=$it  value=$value")
                }
            }
            val style = extras.getString(Notification.EXTRA_TEMPLATE)
            if (style != null && style.contains("MessagingStyle")) {
                val conversationTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)
                var user: Person? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    user = extras.getParcelable<android.app.Person>(Notification.EXTRA_MESSAGING_PERSON)
                }
                val isGroup = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION)
                Log.d("onuszhao", "MessagingStyle bundle  title=$conversationTitle  user=$user  isGroup=$isGroup")
                val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
                if (messages != null) {
                    for (messageParcelable in messages) {
                        if (messageParcelable is Bundle) {
                            messageParcelable.keySet().forEach {
                                val value = messageParcelable.get(it)
                                Log.d("onuszhao", "Messages bundle  key=$it  value=$value")
                            }
                        }
                    }
                }
            }

            page?.createResultLine(msg)

            // val msg2 = "${if (posted) "收到消息" else "移除消息"}   $pkgName $timeStr title=$title  text=$text  $sbn"
            // Log.d("onuszhao", msg2)
            // page?.createResultLine(msg2)
        }.onFailure {
            Log.d("onuszhao", "crash  key=${it.localizedMessage} + ${it.stackTrace.toString()}")
            it.printStackTrace()
        }

        // 反射PendingIntent中getIntent函数获取intent
        // val pendingIntent = sbn.notification.contentIntent      // 获取通知的PendingIntent
        // val intent = getIntent(pendingIntent)

        // 从intent获取extras
        // val intentExtras = intent?.extras

        // 获取intent中内容
//         String notificationType = "";
//         String articleCategory = "";
//         String imgUrl = "";
//         String articleUrl = "";
//         if (pkgName.equals("com.opera.app.news")) {
//             notificationType = getString(intentExtras, "notification_type");
//             articleCategory = getString(intentExtras, "newsfeed_category");
//             imgUrl = getString(intentExtras, "show_article_thumbnail_url");
//             if (imgUrl.equals("")) {
//                 imgUrl = getString(intentExtras, "news_icon_url");
//             }
//             //show_article_final_url中存在广告，故选择show_article_reader_mode_url
//             articleUrl = getString(intentExtras, "show_article_reader_mode_url");
//             if (articleUrl.equals("")) {
//                 getString(intent, pkg, "show_article_final_url");
//             }
//         } else if (pkgName.equals("com.hatsune.eagleee")) {
//             notificationType = "1";
//             String url = getDataStr (intent);
//             String paramStr = url . substring (url.indexOf("?") + 1);
//             String[] keyValues = paramStr . split ("&");
//             for (int i = 0; i < keyValues.length; i++) {
//                 String[] keyValue = keyValues [i].split("=");
//                 String key = keyValue [0];
//                 String value = keyValue [1];
//                 if (key.equals("newsId")) {
//                     articleUrl = "https://m.scoopernews.com/detail?newsId=" + value;
// //                        articleUrl = "http://www.scooper.news/detail?newsId=" + value;
//                 } else if (key.equals("NSI")) {
//                     articleCategory = value;
//                 }
//             }
//         } else if (pkgName.equals("com.opera.browser")) {
//             articleUrl = getString(intentExtras, "show_article_reader_mode_url");
//             if (articleUrl.equals("")) {
//                 testReflect(intent);
//             }
//         } else if (pkgName.equals("com.opera.mini.native")) {
//             articleUrl = getString(intentExtras, "show_article_reader_mode_url");
//             if (articleUrl.equals("")) {
//                 testReflect(intent);
//             }
//         } else if (pkgName.equals("com.allfootball.news")) {
//             testReflect(intent);
//         } else if (pkgName.equals("com.onus.demotest")) {
//             articleUrl = getDataStr(intent);
//         }
    }

//
//             Log.i(TAG, pkg + "--> " + time + "," + notificationType + "," + title + " **** " + text + "," + imgUrl + "," + articleUrl);
// //            if (!title.equals("") && !articleUrl.equals("")) {
// //                PushInfo pushInfo = new PushInfo();
// //                pushInfo.setCountry(country);
// //                pushInfo.setLanguage(language);
// //                pushInfo.setAppPkg(pkg);
// //                pushInfo.setAppName(appName);
// //                pushInfo.setTime(time);
// //                pushInfo.setType(notificationType);
// //                pushInfo.setCategory(articleCategory);
// //                pushInfo.setTitle(title);
// //                pushInfo.setText(text);
// //                pushInfo.setContentUrl(articleUrl);
// //                pushInfo.setImgUrl(imgUrl);
// //                uploadUtil.uploadNotificationInfo(pushInfo);
// //            }
//         }
//
//         // 获取当前通知栏消息数量，超过50则清空
//         int number = getActiveNotifications ().length;
//         if (number >= 50) {
//             cancelAllNotifications();
//             Log.i(TAG, "Number of messages: " + number + " , after cleaning: " + getActiveNotifications().length);
//         }
//
//         // 取消通知：API>=21用StatusBarNotification的getKey()方法来获取key并取消通知，否则用已废弃方法来取消通知
// //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
// //                cancelNotification(sbn.getKey());
// //            } else {
// //                cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
// //            }
//     }

    /**
     * Return the Intent for PendingIntent.
     * Return null in case of some (impossible) errors: see Android source.
     *
     * @throws IllegalStateException in case of something goes wrong.
     *                               See {@link Throwable#getCause()} for more details.
     *                               将应用放到系统目录下，重启手机即为系统应用
     *                               adb remount
     *                               adb push test.apk /system/app
     */
    private fun getIntent(pendingIntent: PendingIntent): Intent? {
        kotlin.runCatching {
            val getIntent = PendingIntent::class.java.getDeclaredMethod("getIntent")
            return getIntent.invoke(pendingIntent) as? Intent
        }
        return null
    }

    //     fun getString(extras:Bundle , key:String) : String {
//         val value = "";
//         if (extras != null) {
//             try {
//                 Iterator<String> iterator = extras . keySet ().iterator();
//                 while (iterator.hasNext()) {
//                     String tmpKey = iterator . next ();
//                     if (!TextUtils.isEmpty(tmpKey) && tmpKey.equals(key)) {
//                         value = extras.get(tmpKey).toString();
//                         break;
//                     }
//                 }
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//                 try {
//                     Class bundleClass = extras . getClass ();
//                     Class baseBundleClass = bundleClass . getSuperclass ();
//                     Field field = baseBundleClass . getDeclaredField ("mMap");
//                     field.setAccessible(true);
//                     ArrayMap<String, Object> mMap =(ArrayMap<String, Object>) field . get (extras);
//                     for (String tmpKey : mMap.keySet()) {
//                         if (tmpKey.equals(key)) {
//                             value = mMap.get(tmpKey).toString();
//                             break;
//                         }
//                     }
//                 } catch (Exception e1) {
//                     printException(e1);
//                     e1.printStackTrace();
//                 }
//             }
//         }
//         return value;
//     }
//
    private fun getString(intent: Intent, pkg: String, key: String): String {
        kotlin.runCatching {
            // 先获取Intent的mExtras对象，然后调用mExtras的unparcel()方法,填充值后，直接遍历mMap
            intent.javaClass.getDeclaredField("mExtras")?.let { extrasField ->
                extrasField.isAccessible = true
                (extrasField.get(intent) as? Bundle)?.let { extras ->
                    extras.javaClass.getDeclaredMethod("unparcel")?.let { unparcel ->
                        unparcel.isAccessible = true
                        unparcel.invoke(extras)
                    }
                    extras.javaClass.getDeclaredField("mMap")?.let { map ->
                        map.isAccessible = true
                        (map.get(extras) as? ArrayMap<*, *>)?.forEach { (k, v) ->
                            if (k.equals(key)) {
                                return v.toString()
                            }
                        }
                    }
                }
            }
        }
        return ""
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private fun getDataStr(intent: Intent): String {
        var dataStr = ""
        kotlin.runCatching {
            intent.javaClass.getDeclaredField("mData")?.let { dataField ->
                dataField.isAccessible = true
                (dataField.get(intent) as? Uri)?.let {
                    dataStr = it.toString();
                }
            }
        }
        return dataStr
    }

    /**
     * 获取通知小图标转化为Bitmap
     *
     * @param id      图标id
     * @param pkgName 图标所在的包名
     * @return bitmap
     */
    private fun getSmallIcon(pkgName: String, id: Int): Bitmap? {
        var smallIcon: Bitmap? = null
        var remotePkgContext: Context
        kotlin.runCatching {
            remotePkgContext = applicationContext.createPackageContext(pkgName, 0);
            val drawable = remotePkgContext.resources.getDrawable(id)
            if (drawable != null) {
                smallIcon = (drawable as? BitmapDrawable)?.bitmap
            }
        }
        return smallIcon
    }
//
//     //先获取sbn.getNotification().tickerText，如果为空，则尝试使用反射获取view上的内容
//     private Map<String, Object> getNotiInfo(Notification notification)
//     {
//         int key = 0;
//         if (notification == null)
//             return null;
//         RemoteViews views = notification . contentView;
//         if (views == null)
//             return null;
//         Class secretClass = views . getClass ();
//
//         try {
//             Map<String, Object> text = new HashMap<>();
//
//             Field outerFields [] = secretClass.getDeclaredFields();
//             for (int i = 0; i < outerFields.length; i++) {
//                 if (!outerFields[i].getName().equals("mActions"))
//                     continue;
//
//                 outerFields[i].setAccessible(true);
//
//                 ArrayList<Object> actions =(ArrayList<Object>) outerFields [i].get(views);
//                 for (Object action : actions) {
//                 Field innerFields [] = action.getClass().getDeclaredFields();
//                 Object value = null;
//                 Integer type = null;
//                 for (Field field : innerFields) {
//                 field.setAccessible(true);
//                 if (field.getName().equals("value")) {
//                     value = field.get(action);
//                 } else if (field.getName().equals("type")) {
//                     type = field.getInt(action);
//                 }
//             }
//                 // 经验所得 type 等于9 10为短信title和内容，不排除其他厂商拿不到的情况
//                 if (type != null && (type == 9 || type == 10)) {
//                     if (key == 0) {
//                         text.put("title", value != null ? value . toString () : "");
//                     } else if (key == 1) {
//                         text.put("text", value != null ? value . toString () : "");
//                     } else {
//                         text.put(Integer.toString(key), value != null ? value . toString () : null);
//                     }
//                     key++;
//                 }
//             }
//                 key = 0;
//
//             }
//             return text;
//         } catch (Exception e) {
//             printException(e);
//             e.printStackTrace();
//         }
//         return null;
//     }
//
//     private void testInfo(StatusBarNotification sbn)
//     {
//         try {
// //            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
// //            当API>18时，利用Notification.extras来获取通知内容(extras是在API 19时被加入的)
// //            当API=18时，利用反射获取Notification中的内容
//
//             //过滤包名
//             String pkg = sbn . getPackageName ();                  // 获取通知消息的包名
//             boolean isClearable = sbn . isClearable ();        // 获取通知消息是否可被清除
//             SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
//             String time = df . format (sbn.getPostTime());     // 获取通知消息的时间
//             int id = sbn . getId ();                           // 获取通知消息的id
//             String key = sbn . getKey ();                      // 获取通知消息的key
//             String tag = sbn . getTag ();                      // 获取通知消息的Tag，如果没有设置返回null
// //            String ticker = sbn.getNotification().tickerText.toString();
//             Bundle extras = sbn . getNotification ().extras;
//             String title = "";
//             if ((extras.getString(Notification.EXTRA_TITLE)) != null) {
//                 title = extras.getString(Notification.EXTRA_TITLE);                     // 获取通知消息的title
//             }
//             String text = "";
//             if (extras.getCharSequence(Notification.EXTRA_TEXT) != null) {
//                 text = extras.getCharSequence(Notification.EXTRA_TEXT).toString();      // 获取通知消息的内容
//             }
//             Bitmap smallIcon = null;
//             if ((extras.getInt(Notification.EXTRA_SMALL_ICON)) != 0) {
//                 int smallIconId = extras . getInt (Notification.EXTRA_SMALL_ICON);         // 获取通知消息的小图标id
//                 smallIcon = getSmallIcon(pkg, smallIconId);
//             }
//             Bitmap largeIcon = null;
//             if ((extras.getParcelable(Notification.EXTRA_LARGE_ICON)) != null) {
//                 largeIcon = extras.getParcelable(Notification.EXTRA_LARGE_ICON);        // 获取通知消息的大图标
//             }
//             String imgUrl = null;
//             if ((extras.getString(Notification.EXTRA_BACKGROUND_IMAGE_URI)) != null) {
//                 imgUrl = extras.getString(Notification.EXTRA_BACKGROUND_IMAGE_URI);
//             }
//             String contentUrl = null;
//             if ((extras.getString(Notification.EXTRA_AUDIO_CONTENTS_URI)) != null) {
//                 contentUrl = extras.getString(Notification.EXTRA_AUDIO_CONTENTS_URI);
//             }
//
//             Log.i(
//                 TAG, time + "--> pkg=" + pkg + ",id=" + id + ",key=" + key + ",tag=" + tag + ",isClearable=" + isClearable
//                     + " title=" + title + " **** text=" + text + ",smallIcon=" + (smallIcon == null) + ",largeIcon=" + (largeIcon == null)
//                     + ",imgUrl=" + imgUrl + ",contentUrl=" + contentUrl
//             );
//
//             // 反射PendingIntent中getIntent函数获取intent
//             PendingIntent pendingIntent = sbn . getNotification ().contentIntent;          //获取通知的PendingIntent
//             Intent intent = getIntent (pendingIntent);
//
//             // 从intent获取extras
//             Bundle intentExtras = intent . getExtras ();
//             Iterator<String> intentIterator = intentExtras . keySet ().iterator();
//             while (intentIterator.hasNext()) {
//                 String intentKey = intentIterator . next ();
//                 if (!TextUtils.isEmpty(intentKey)) {
//                     Object intentObj = intentExtras . get (intentKey);
//                     Log.i(TAG, "intent extras --> key=" + intentKey + ",value=" + intentObj.toString());
//                 }
//             }
//
//             // 反射extras获取map
//             Field intentExtrasField = intentExtras . getClass ().getSuperclass().getDeclaredField("mMap");
//             intentExtrasField.setAccessible(true);
//             ArrayMap<String, Object> superMap =(ArrayMap<String, Object>) intentExtrasField . get (intentExtras);
//             for (String supKey : superMap.keySet()) {
//                 Log.i(TAG, "intent superMap -->  key=" + supKey + ",value=" + superMap.get(supKey).toString());
//             }
//
//             // 反射获取mExtras，再反射mExtras获取map
//             Field declaredField = intent . getClass ().getDeclaredField("mExtras");
//             declaredField.setAccessible(true);
//             Bundle mExtras =(Bundle) declaredField . get (intent);
//             Method unparcel = mExtras . getClass ().getDeclaredMethod("unparcel");
//             unparcel.setAccessible(true);
//             unparcel.invoke(mExtras);
//             Field mMap = mExtras . getClass ().getDeclaredField("mMap");
//             mMap.setAccessible(true);
//             ArrayMap arrayMap =(ArrayMap) mMap . get (mExtras);
//             Iterator<String> it = arrayMap . keySet ().iterator();
//             while (it.hasNext()) {
//                 String tmpKey = it . next ();
//                 String tmpValue = arrayMap . get (key).toString();
//                 Log.i(TAG, "intent mMap -->   key:" + tmpKey + "  value:" + tmpValue);
//             }
//         } catch (Exception e) {
//             printException(e);
//             e.printStackTrace();
//         }
//     }
//
//     public void testReflect(Intent intent)
//     {
//         if (intent != null) {
//             // android中自定义的对象序列化的问题有两个选择一个是Parcelable，另外一个是Serializable
//             // 从intent获取extras
//             Bundle extras = intent . getExtras ();
//             try {
//                 // 方案1：直接从intent中获取extras
//                 Log.i(TAG, "方案1 start");
//                 // classLoader找不到，可以尝试设置
// //                extras.setClassLoader(extras.getClass().getClassLoader());
//                 Iterator<String> iterator = extras . keySet ().iterator();
//                 while (iterator.hasNext()) {
//                     String key = iterator . next ();
//                     if (!TextUtils.isEmpty(key)) {
//                         String value = extras . get (key).toString();
//                         Log.i(TAG, "方案1  -->  key:" + key + "  value:" + value);
//                     }
//                 }
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//             }
//
//             try {
//                 //方案2:从superclass中获取map
//                 Log.i(TAG, "方案2 start");
//                 Class baseBundleClass = extras . getClass ().getSuperclass();
//                 Field mMap = baseBundleClass . getDeclaredField ("mMap");
//                 mMap.setAccessible(true);
//                 ArrayMap<String, Object> tmpMap =(ArrayMap<String, Object>) mMap . get (extras);
//                 for (String tmpKey : tmpMap.keySet()) {
//                     String tmpValue = tmpMap . get (tmpKey).toString();
//                     Log.i(TAG, "方案2  -->  key:" + tmpKey + "  value:" + tmpValue);
//                 }
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//             }
//
//             try {
//                 // 方案3：反射intent，获取mExtras，再反射unparcel，获取mMap(此方案仅适用android 4.4及以下)
//                 Log.i(TAG, "方案3 start");
//                 Field mExtrasField = intent . getClass ().getDeclaredField("mExtras");
//                 mExtrasField.setAccessible(true);
//                 Bundle mExtras =(Bundle) mExtrasField . get (intent);
//                 Method unparcel = mExtras . getClass ().getDeclaredMethod("unparcel");
//                 unparcel.setAccessible(true);
//                 unparcel.invoke(mExtras);
//                 Field mMap = mExtras . getClass ().getDeclaredField("mMap");
//                 mMap.setAccessible(true);
//                 ArrayMap arrayMap =(ArrayMap) mMap . get (mExtras);
//                 Iterator<String> it = arrayMap . keySet ().iterator();
//                 while (it.hasNext()) {
//                     String tmpKey = it . next ();
//                     String tmpValue = arrayMap . get (tmpKey).toString();
//                     Log.i(TAG, "方案3  -->   key:" + tmpKey + "  value:" + tmpValue);
//                 }
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//             }
//
//             try {
//                 // 方案4：反射intent，获取mExtras，从superclass中再反射unparcel，获取mMap(此方案适用android 5.0及以上)
//                 Log.i(TAG, "方案4 start");
//                 Field mExtrasField = intent . getClass ().getDeclaredField("mExtras");
//                 mExtrasField.setAccessible(true);
//                 Bundle mExtras =(Bundle) mExtrasField . get (intent);
//                 Class baseBundleClass = mExtras . getClass ().getSuperclass();
//                 // 反射BaseBundle构造函数1
// //                Parcel mParcelledData = Parcel.obtain();
// //                Constructor baseBundleConstructor = baseBundleClass.getDeclaredConstructor(Parcel.class);
// //                baseBundleConstructor.setAccessible(true);
// //                BaseBundle baseBundle = (BaseBundle) baseBundleConstructor.newInstance(mParcelledData);
//                 // 反射BaseBundle构造函数2
//                 Constructor baseBundleConstructor = baseBundleClass . getDeclaredConstructor (BaseBundle.class);
//                 baseBundleConstructor.setAccessible(true);
//                 BaseBundle baseBundle =(BaseBundle) baseBundleConstructor . newInstance (extras);
//                 Method unparcel = baseBundleClass . getDeclaredMethod ("unparcel");
//                 unparcel.setAccessible(true);
//                 unparcel.invoke(baseBundle);
//                 Field mMap = baseBundleClass . getDeclaredField ("mMap");
//                 mMap.setAccessible(true);
//                 ArrayMap arrayMap =(ArrayMap) mMap . get (mExtras);
//                 Iterator<String> it = arrayMap . keySet ().iterator();
//                 while (it.hasNext()) {
//                     String tmpKey = it . next ();
//                     String tmpValue = arrayMap . get (tmpKey).toString();
//                     Log.i(TAG, "方案4  -->   key:" + tmpKey + "  value:" + tmpValue);
//                 }
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//             }
//
//             try {
//                 //方案5：反射intent中mData
//                 Log.i(TAG, "方案5 start");
//                 Field mDataField = intent . getClass ().getDeclaredField("mData");
//                 mDataField.setAccessible(true);
//                 Uri mData =(Uri) mDataField . get (intent);
//                 Log.i(TAG, "方案5  -->  mData:" + mData.toString());
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//             }
//
//             try {
//                 //方案6：反射intent中mData,再转化为intent，再从intent里面获取extra
//                 Log.i(TAG, "方案6 start");
//                 Field mDataField = intent . getClass ().getDeclaredField("mData");
//                 mDataField.setAccessible(true);
//                 Uri mData =(Uri) mDataField . get (intent);
//                 Field mExtrasField = intent . getClass ().getDeclaredField("mExtras");
//                 mExtrasField.setAccessible(true);
//                 Bundle mExtras =(Bundle) mExtrasField . get (intent);
//                 Method getIntent = mExtras . getClass ().getDeclaredMethod("getIntent", String.class);
//                 getIntent.setAccessible(true);
//                 getIntent.invoke(mExtras);
//                 Intent tmpIntent =(Intent) getIntent . invoke (mExtras, mData.toString());
//                 // 从intent中获取extras和map
//                 Bundle tmpExtras = tmpIntent . getExtras ();
//                 Iterator<String> tmpIterator = tmpExtras . keySet ().iterator();
//                 while (tmpIterator.hasNext()) {
//                     String tmpKey = tmpIterator . next ();
//                     if (!TextUtils.isEmpty(tmpKey)) {
//                         String tmpValue = tmpExtras . get (tmpKey).toString();
//                         Log.i(TAG, "方案6  -->  tmpKey:" + tmpKey + "  tmpValue:" + tmpValue);
//                     }
//                 }
//                 Class tmpBaseBundleClass = tmpExtras . getClass ().getSuperclass();
//                 Field tmpMap = tmpBaseBundleClass . getDeclaredField ("mMap");
//                 tmpMap.setAccessible(true);
//                 ArrayMap<String, Object> tmpArrMap =(ArrayMap<String, Object>) tmpMap . get (tmpExtras);
//                 for (String tmpArrKey : tmpArrMap.keySet()) {
//                     String tmpArrValue = tmpArrMap . get (tmpArrKey).toString();
//                     Log.i(TAG, "方案6  -->  tmpArrKey:" + tmpArrKey + "  tmpArrValue:" + tmpArrValue);
//                 }
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//             }
//
//             try {
//                 //方案7：反射intent中mData,再转化为old intent，再从intent里面获取extra
//                 Log.i(TAG, "方案7 start");
//                 Field mDataField = intent . getClass ().getDeclaredField("mData");
//                 mDataField.setAccessible(true);
//                 Uri mData =(Uri) mDataField . get (intent);
//                 Field mExtrasField = intent . getClass ().getDeclaredField("mExtras");
//                 mExtrasField.setAccessible(true);
//                 Bundle mExtras =(Bundle) mExtrasField . get (intent);
//                 Method getIntent = mExtras . getClass ().getDeclaredMethod("getIntentOld", String.class);
//                 getIntent.setAccessible(true);
//                 getIntent.invoke(mExtras);
//                 Intent tmpIntent =(Intent) getIntent . invoke (mExtras, mData.toString());
//                 // 从intent中获取extras和map
//                 Bundle tmpExtras = tmpIntent . getExtras ();
//                 Iterator<String> tmpIterator = tmpExtras . keySet ().iterator();
//                 while (tmpIterator.hasNext()) {
//                     String tmpKey = tmpIterator . next ();
//                     if (!TextUtils.isEmpty(tmpKey)) {
//                         String tmpValue = tmpExtras . get (tmpKey).toString();
//                         Log.i(TAG, "方案7  -->  tmpKey:" + tmpKey + "  tmpValue:" + tmpValue);
//                     }
//                 }
//                 Class tmpBaseBundleClass = tmpExtras . getClass ().getSuperclass();
//                 Field tmpMap = tmpBaseBundleClass . getDeclaredField ("mMap");
//                 tmpMap.setAccessible(true);
//                 ArrayMap<String, Object> tmpArrMap =(ArrayMap<String, Object>) tmpMap . get (tmpExtras);
//                 for (String tmpArrKey : tmpArrMap.keySet()) {
//                     String tmpArrValue = tmpArrMap . get (tmpArrKey).toString();
//                     Log.i(TAG, "方案7  -->  tmpArrKey:" + tmpArrKey + "  tmpArrValue:" + tmpArrValue);
//                 }
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//             }
//
//             try {
//                 //方案8：反射intent中mCategories
//                 Log.i(TAG, "方案8 start");
//                 Field mCategoriesField = intent . getClass ().getDeclaredField("mCategories");
//                 mCategoriesField.setAccessible(true);
//                 ArraySet<String> mCategories;
//                 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//                     mCategories = (ArraySet<String>) mCategoriesField . get (intent);
//                     Iterator<String> mCategoriesIterator = mCategories . iterator ();
//                     while (mCategoriesIterator.hasNext()) {
//                         String mCategory = mCategoriesIterator . next ();
//                         Log.i(TAG, "方案8  -->  mCategory:" + mCategory);
//                     }
//                 } else {
//                     Log.i(TAG, "方案8  -->  sdk build version too low");
//                 }
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//             }
//
//             try {
//                 //方案9：获取原始intent，再解析extras
//                 Log.i(TAG, "方案9 start");
//                 PendingIntent tmpPendingIntent =(PendingIntent)((Intent) intent . getExtras ().get("wrapped_intent")).getExtras().get("pending_intent");
//                 Intent tmpIntent = getIntent (tmpPendingIntent);
//                 Bundle tmpExtras = tmpIntent . getExtras ();
//                 Iterator<String> tmpIterator = tmpExtras . keySet ().iterator();
//                 while (tmpIterator.hasNext()) {
//                     String tmpKey = tmpIterator . next ();
//                     if (!TextUtils.isEmpty(tmpKey)) {
//                         String tmpValue = tmpExtras . get (tmpKey).toString();
//                         Log.i(TAG, "方案9  -->  tmpKey:" + tmpKey + "  tmpValue:" + tmpValue);
//                     }
//                 }
//                 // 关注是否存在NotificationNewsId，与url拼接即为可用url
//                 String newsId = tmpExtras . getString ("NotificationNewsId");
//                 Log.i(TAG, "方案9 -->   newsId:" + newsId);
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//             }
//
//             try {
//                 // 方案10：反射intent获取mExtras，再反射unparcel，再反射get获取原始intent，最后解析mExtras(此方案仅适用android 4.4及以下)
//                 Log.i(TAG, "方案10 start");
//                 Field mExtrasField = intent . getClass ().getDeclaredField("mExtras");
//                 mExtrasField.setAccessible(true);
//                 Bundle mExtras =(Bundle) mExtrasField . get (intent);
//                 Method unparcel = mExtras . getClass ().getDeclaredMethod("unparcel");
//                 unparcel.setAccessible(true);
//                 unparcel.invoke(mExtras);
//                 Method get = mExtras . getClass ().getDeclaredMethod("get", String.class);
//                 get.setAccessible(true);
//                 Intent tmpIntent =(Intent) get . invoke (mExtras, "wrapped_intent");
//                 Bundle tmpExtras = tmpIntent . getExtras ();
//                 PendingIntent tmpPendingIntent =(PendingIntent) tmpExtras . get ("pending_intent");
//                 Intent dstIntent = getIntent (tmpPendingIntent);
//                 Bundle dstExtras = dstIntent . getExtras ();
//                 Iterator<String> dstIterator = dstExtras . keySet ().iterator();
//                 while (dstIterator.hasNext()) {
//                     String dstKey = dstIterator . next ();
//                     if (!TextUtils.isEmpty(dstKey)) {
//                         String dstValue = tmpExtras . get (dstKey).toString();
//                         Log.i(TAG, "方案10  -->  dstKey:" + dstKey + "  dstValue:" + dstValue);
//                     }
//                 }
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//             }
//
//             try {
//                 // 方案11：反射intent获取mExtras，再反射unparcel，再反射get获取原始intent，最后解析mExtras(此方案仅适用android 4.4及以下)
//                 Log.i(TAG, "方案11 start");
//                 Field mExtrasField = intent . getClass ().getDeclaredField("mExtras");
//                 mExtrasField.setAccessible(true);
//                 Bundle mExtras =(Bundle) mExtrasField . get (intent);
//                 Method unparcel = mExtras . getClass ().getDeclaredMethod("unparcel");
//                 unparcel.setAccessible(true);
//                 unparcel.invoke(mExtras);
//                 Method get = mExtras . getClass ().getDeclaredMethod("get", String.class);
//                 get.setAccessible(true);
//                 Intent tmpIntent =(Intent) get . invoke (mExtras, "wrapped_intent");
//                 Field tmpExtrasField = tmpIntent . getClass ().getDeclaredField("mExtras");
//                 tmpExtrasField.setAccessible(true);
//                 Bundle tmpExtras =(Bundle) tmpExtrasField . get (tmpIntent);
//                 Method tmpUnparcel = tmpExtras . getClass ().getDeclaredMethod("unparcel");
//                 tmpUnparcel.setAccessible(true);
//                 tmpUnparcel.invoke(mExtras);
//                 Method tmpGet = tmpExtras . getClass ().getDeclaredMethod("get", String.class);
//                 tmpGet.setAccessible(true);
//                 PendingIntent dstPendingIntent =(PendingIntent) tmpGet . invoke (tmpExtras, "pending_intent");
//                 Intent dstIntent = getIntent (dstPendingIntent);
//                 Bundle dstExtras = dstIntent . getExtras ();
//                 Iterator<String> dstIterator = dstExtras . keySet ().iterator();
//                 while (dstIterator.hasNext()) {
//                     String dstKey = dstIterator . next ();
//                     if (!TextUtils.isEmpty(dstKey)) {
//                         String dstValue = tmpExtras . get (dstKey).toString();
//                         Log.i(TAG, "方案11  -->  dstKey:" + dstKey + "  dstValue:" + dstValue);
//                     }
//                 }
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//             }
//
//             try {
//                 // 方案12：反射intent获取mExtras，再反射unparcel，再反射get获取原始intent，最后解析mExtras(此方案适用android 5.0及以上)
//                 Log.i(TAG, "方案12 start");
//                 Field mExtrasField = intent . getClass ().getDeclaredField("mExtras");
//                 mExtrasField.setAccessible(true);
//                 Bundle mExtras =(Bundle) mExtrasField . get (intent);
//                 Class baseBundleClass = mExtras . getClass ().getSuperclass();
//                 // 反射BaseBundle构造函数1
// //                Parcel mParcelledData = Parcel.obtain();
// //                Constructor baseBundleConstructor = baseBundleClass.getDeclaredConstructor(Parcel.class);
// //                baseBundleConstructor.setAccessible(true);
// //                BaseBundle baseBundle = (BaseBundle) baseBundleConstructor.newInstance(mParcelledData);
//                 // 反射BaseBundle构造函数2
//                 Constructor baseBundleConstructor = baseBundleClass . getDeclaredConstructor (BaseBundle.class);
//                 baseBundleConstructor.setAccessible(true);
//                 BaseBundle baseBundle =(BaseBundle) baseBundleConstructor . newInstance (extras);
//                 Method unparcel = baseBundleClass . getDeclaredMethod ("unparcel");
//                 unparcel.setAccessible(true);
//                 unparcel.invoke(baseBundle);
//                 Method get = baseBundleClass . getDeclaredMethod ("get", String.class);
//                 get.setAccessible(true);
//                 Intent tmpIntent =(Intent) get . invoke (baseBundle, "wrapped_intent");
//                 Bundle tmpExtras = tmpIntent . getExtras ();
//                 PendingIntent tmpPendingIntent =(PendingIntent) tmpExtras . get ("pending_intent");
//                 Intent dstIntent = getIntent (tmpPendingIntent);
//                 Bundle dstExtras = dstIntent . getExtras ();
//                 Iterator<String> dstIterator = dstExtras . keySet ().iterator();
//                 while (dstIterator.hasNext()) {
//                     String dstKey = dstIterator . next ();
//                     if (!TextUtils.isEmpty(dstKey)) {
//                         String dstValue = tmpExtras . get (dstKey).toString();
//                         Log.i(TAG, "方案12  -->  dstKey:" + dstKey + "  dstValue:" + dstValue);
//                     }
//                 }
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//             }
//
//             try {
//                 // 方案13：反射intent获取mExtras，再反射unparcel，再反射get获取原始intent，最后解析mExtras(此方案适用android 5.0及以上)
//                 Log.i(TAG, "方案13 start");
//                 Field mExtrasField = intent . getClass ().getDeclaredField("mExtras");
//                 mExtrasField.setAccessible(true);
//                 Bundle mExtras =(Bundle) mExtrasField . get (intent);
//                 Class baseBundleClass = mExtras . getClass ().getSuperclass();
//                 // 反射BaseBundle构造函数1
// //                Parcel mParcelledData = Parcel.obtain();
// //                Constructor baseBundleConstructor = baseBundleClass.getDeclaredConstructor(Parcel.class);
// //                baseBundleConstructor.setAccessible(true);
// //                BaseBundle baseBundle = (BaseBundle) baseBundleConstructor.newInstance(mParcelledData);
//                 // 反射BaseBundle构造函数2
//                 Constructor baseBundleConstructor = baseBundleClass . getDeclaredConstructor (BaseBundle.class);
//                 baseBundleConstructor.setAccessible(true);
//                 BaseBundle baseBundle =(BaseBundle) baseBundleConstructor . newInstance (extras);
//                 Method unparcel = baseBundleClass . getDeclaredMethod ("unparcel");
//                 unparcel.setAccessible(true);
//                 unparcel.invoke(baseBundle);
//                 Method get = baseBundleClass . getDeclaredMethod ("get", String.class);
//                 get.setAccessible(true);
//                 Intent tmpIntent =(Intent) get . invoke (baseBundle, "wrapped_intent");
//                 Field tmpExtrasField = tmpIntent . getClass ().getDeclaredField("mExtras");
//                 tmpExtrasField.setAccessible(true);
//                 Bundle tmpExtras =(Bundle) tmpExtrasField . get (tmpIntent);
//                 Class tmpBaseBundleClass = tmpExtras . getClass ().getSuperclass();
//                 // 反射BaseBundle构造函数1
// //                Parcel tmpParcelledData = Parcel.obtain();
// //                Constructor tmpBaseBundleConstructor = baseBundleClass.getDeclaredConstructor(Parcel.class);
// //                tmpBaseBundleConstructor.setAccessible(true);
// //                BaseBundle tmpBaseBundle = (BaseBundle) tmpBaseBundleConstructor.newInstance(tmpParcelledData);
//                 // 反射BaseBundle构造函数2
//                 Constructor tmpBaseBundleConstructor = tmpBaseBundleClass . getDeclaredConstructor (BaseBundle.class);
//                 tmpBaseBundleConstructor.setAccessible(true);
//                 BaseBundle tmpBaseBundle =(BaseBundle) tmpBaseBundleConstructor . newInstance (tmpExtras);
//                 Method tmpUnparcel = tmpBaseBundleClass . getDeclaredMethod ("unparcel");
//                 tmpUnparcel.setAccessible(true);
//                 tmpUnparcel.invoke(tmpBaseBundle);
//                 Method tmpGet = tmpBaseBundleClass . getDeclaredMethod ("get", String.class);
//                 tmpGet.setAccessible(true);
//                 PendingIntent dstPendingIntent =(PendingIntent) tmpGet . invoke (tmpBaseBundle, "pending_intent");
//                 Intent dstIntent = getIntent (dstPendingIntent);
//                 Bundle dstExtras = dstIntent . getExtras ();
//                 Iterator<String> dstIterator = dstExtras . keySet ().iterator();
//                 while (dstIterator.hasNext()) {
//                     String dstKey = dstIterator . next ();
//                     if (!TextUtils.isEmpty(dstKey)) {
//                         String dstValue = tmpExtras . get (dstKey).toString();
//                         Log.i(TAG, "方案13  -->  dstKey:" + dstKey + "  dstValue:" + dstValue);
//                     }
//                 }
//             } catch (Exception e) {
//                 printException(e);
//                 e.printStackTrace();
//             }
//         }
//     }

}
