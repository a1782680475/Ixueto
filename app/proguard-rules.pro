# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 阿里云视频播放器内核混淆设置
-keep class com.alivc.**{*;}
-keep class com.aliyun.**{*;}
-keep class com.cicada.**{*;}
-dontwarn com.alivc.**
-dontwarn com.aliyun.**
-dontwarn com.cicada.**
# jzvideo混淆设置
-keep public class cn.jzvd.JZMediaSystem {*; }
-keep class tv.danmaku.ijk.media.player.** {*; }
-dontwarn tv.danmaku.ijk.media.player.*
-keep interface tv.danmaku.ijk.media.player.** { *; }
# 百度离线采集sdk混淆设置
-keep class com.baidu.vis.unified.license.** {*;}
-keep class com.baidu.liantian.** {*;}
-keep class com.baidu.baidusec.** {*;}
-keep class com.baidu.idl.main.facesdk.** {*;}
# xpopup混淆设置
-dontwarn com.lxj.xpopup.widget.**
-keep class com.lxj.xpopup.widget.**{*;}
# ixueto混淆设置
-keep public class com.xktech.ixueto.data.remote.entity.** { *; }
-keep public class com.xktech.ixueto.data.remote.service.** { *; }
-keep public class com.xktech.ixueto.data.local.dao.** { *; }
-keep public class com.xktech.ixueto.data.local.serializer.** { *; }
-keep public class com.xktech.ixueto.model.** { *; }
-keep public class com.xktech.ixueto.datastore.** { *; }
-keep public class com.xktech.ixueto.components.player.mediaInterface.**{*;}
-keep public class com.xktech.ixueto.components.videoPlayer.VideoPlayer {*; }
-dontwarn java.rmi.RemoteException
-dontwarn pub.devrel.**
-keep class androidx.datastore.*.** {*;}
-keep class com.google.protobuf.**{*;}
