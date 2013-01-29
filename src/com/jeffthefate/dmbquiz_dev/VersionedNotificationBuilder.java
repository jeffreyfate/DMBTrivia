package com.jeffthefate.dmbquiz_dev;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public abstract class VersionedNotificationBuilder {
    
    public abstract VersionedNotificationBuilder create(Context context);
    public abstract VersionedNotificationBuilder setSmallIcon(int icon);
    public abstract VersionedNotificationBuilder setLargeIcon(Bitmap icon);
    public abstract VersionedNotificationBuilder setWhen(long when);
    public abstract VersionedNotificationBuilder setContentTitle(
            CharSequence title);
    public abstract VersionedNotificationBuilder setContentText(
            CharSequence text);
    public abstract VersionedNotificationBuilder setTicker(CharSequence ticker);
    public abstract VersionedNotificationBuilder setContentInfo(
            CharSequence info);
    public abstract VersionedNotificationBuilder setContentIntent(
            PendingIntent intent);
    public abstract Notification getNotification();
    
    public static VersionedNotificationBuilder newInstance() {
        final int sdkVersion = Build.VERSION.SDK_INT;
        VersionedNotificationBuilder builder = null;
        if (sdkVersion < Build.VERSION_CODES.HONEYCOMB)
            builder = new GingerbreadBuilder();
        else if (sdkVersion >= Build.VERSION_CODES.HONEYCOMB &&
                sdkVersion < Build.VERSION_CODES.JELLY_BEAN)
            builder = new HoneycombBuilder();
        else
            builder = new JellyBeanBuilder();

        return builder;
    }
    
    private static class GingerbreadBuilder extends
            VersionedNotificationBuilder {
        
        NotificationCompat.Builder builder;

        @Override
        public VersionedNotificationBuilder create(Context context) {
            builder = new NotificationCompat.Builder(context);
            return this;
        }

        @Override
        public VersionedNotificationBuilder setSmallIcon(int icon) {
            builder.setSmallIcon(icon);
            return this;
        }
        
        @Override
        public VersionedNotificationBuilder setLargeIcon(Bitmap icon) {
            builder.setLargeIcon(icon);
            return this;
        }

        @Override
        public VersionedNotificationBuilder setWhen(long when) {
            builder.setWhen(when);
            return this;
        }

        @Override
        public VersionedNotificationBuilder setContentTitle(
                CharSequence title) {
            builder.setContentTitle(title);
            return this;
        }

        @Override
        public VersionedNotificationBuilder setContentText(CharSequence text) {
            builder.setContentText(text);
            return this;
        }
        
        @Override
        public VersionedNotificationBuilder setTicker(CharSequence ticker) {
            builder.setTicker(ticker);
            return this;
        }
        
        @Override
        public VersionedNotificationBuilder setContentInfo(CharSequence info) {
            builder.setContentInfo(info);
            return this;
        }

        @Override
        public VersionedNotificationBuilder setContentIntent(
                PendingIntent intent) {
            builder.setContentIntent(intent);
            return this;
        }

        @Override
        public Notification getNotification() {
            return builder.build();
        }
    }
    
    private static class HoneycombBuilder extends GingerbreadBuilder {
        Notification.Builder builder;

        @Override
        public VersionedNotificationBuilder create(Context context) {
            builder = new Notification.Builder(context);
            return this;
        }
        
        @Override
        public VersionedNotificationBuilder setSmallIcon(int icon) {
            builder.setSmallIcon(icon);
            return this;
        }
        
        @Override
        public VersionedNotificationBuilder setLargeIcon(Bitmap icon) {
            builder.setLargeIcon(icon);
            return this;
        }

        @Override
        public VersionedNotificationBuilder setWhen(long when) {
            builder.setWhen(when);
            return this;
        }

        @Override
        public VersionedNotificationBuilder setContentTitle(
                CharSequence title) {
            builder.setContentTitle(title);
            return this;
        }

        @Override
        public VersionedNotificationBuilder setContentText(CharSequence text) {
            builder.setContentText(text);
            return this;
        }
        
        @Override
        public VersionedNotificationBuilder setTicker(CharSequence ticker) {
            builder.setTicker(ticker);
            return this;
        }
        
        @Override
        public VersionedNotificationBuilder setContentInfo(CharSequence info) {
            builder.setContentInfo(info);
            return this;
        }

        @Override
        public VersionedNotificationBuilder setContentIntent(
                PendingIntent intent) {
            builder.setContentIntent(intent);
            return this;
        }

        @Override
        public Notification getNotification() {
            return builder.build();
        }
    }
    
    private static class JellyBeanBuilder extends HoneycombBuilder {
        @Override
        public Notification getNotification() {
            return builder.build();
        }        
    }
    
}
