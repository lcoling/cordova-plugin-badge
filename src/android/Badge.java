/*
 * Copyright (c) 2013-2015 by appPlant UG. All rights reserved.
 *
 * @APPPLANT_LICENSE_HEADER_START@
 *
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apache License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://opensource.org/licenses/Apache-2.0/ and read it before using this
 * file.
 *
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 *
 * @APPPLANT_LICENSE_HEADER_END@
 */

package de.appplant.cordova.plugin.badge;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

public class Badge extends CordovaPlugin {

    /**
     * The ID for the notification
     */
    private final int ID = -450793490;

    /**
     * The name for the shared preferences key
     */
    static final String KEY = "badge";

    /**
     * Bundle identifier for the autoCancel value
     */
    static final String EXTRA_AUTO_CANCEL = "EXTRA_AUTO_CANCEL";

    /**
     * Executes the request.
     *
     * @param action   The action to execute.
     * @param args     The exec() arguments.
     * @param callback The callback context used when
     *                 calling back into JavaScript.
     *
     * @return
     *      Returning false results in a "MethodNotFound" error.
     *
     * @throws JSONException
     */
    @Override
    public boolean execute (String action, JSONArray args, CallbackContext callback)
            throws JSONException {

        if (action.equalsIgnoreCase("clearBadge")) {
            clearBadge();

            return true;
        }

        if (action.equalsIgnoreCase("setBadge")) {
            int number        = args.optInt(0);
            String title      = args.optString(1, "%d new messages");
            String smallIcon  = args.optString(2);
            boolean autoClear = args.optBoolean(3, false);
            String largeIcon  = args.optString(4);
            String text       = args.optString(5);

            clearBadge();

            setBadge(number, title, text, largeIcon, smallIcon, autoClear, callback);

            return true;
        }

        if (action.equalsIgnoreCase("getBadge")) {
            getBadge(callback);

            return true;
        }

        if (action.equalsIgnoreCase("hasPermission")) {
            hasPermission(callback);
            return true;
        }

        if (action.equalsIgnoreCase("registerPermission")) {
            hasPermission(callback);
            return true;
        }

        return false;
    }

    /**
     * Sets the badge of the app icon.
     *
     * @param badge
     *      The new badge number
     * @param title
     *      The notifications title
     * @param largeIcon
     *      The notifications large icon
     * @param smallIcon
     *      The notifications small icon
     * @param autoCancel
     *      The autoCancel value
     * @param callbackContext
      *     The current callbackContext
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setBadge (final int badge, final String title, final String text, final String largeIcon,
                           final String smallIcon, final boolean autoCancel, final CallbackContext callbackContext) {

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                //Do not notify on Android for now...
                /*if (badge > 0) {
                    Context context = cordova.getActivity().getApplicationContext();
                    Intent intent = new Intent(context, LaunchActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                    intent.putExtra(EXTRA_AUTO_CANCEL, autoCancel);

                    PendingIntent contentIntent = PendingIntent.getActivity(
                            context, ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                    String title_ = String.format(title, badge);

                    Builder notification = new NotificationCompat.Builder(context)
                            .setContentTitle(title_)
                            .setContentText(text)
                            .setNumber(badge)
                            .setTicker(title_)
                            .setAutoCancel(autoCancel)
                            .setSmallIcon(getResIdForSmallIcon(smallIcon))
                            .setLargeIcon(getIconBitmap(context, largeIcon))
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                            .setContentIntent(contentIntent);

                    saveBadge(badge);
                    getNotificationManager().notify(ID, notification.build());
                }*/
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            }
        });
    }

    /**
     * Clears the badge of the app icon.
     */
    private void clearBadge () {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                saveBadge(0);
                getNotificationManager().cancel(ID);
            }
        });
    }

    /**
     * Retrieves the badge of the app icon.
     *
     * @param callback
     *      The function to be exec as the callback
     */
    private void getBadge (final CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                SharedPreferences settings = getSharedPreferences();
                int badge = settings.getInt(KEY, 0);
                PluginResult result;

                result = new PluginResult(PluginResult.Status.OK, badge);

                callback.sendPluginResult(result);
            }
        });
    }

    /**
     * Persist the badge of the app icon so that `getBadge` is able to return
     * the badge number back to the client.
     *
     * @param badge
     *      The badge of the app icon
     */
    private void saveBadge (int badge) {
        Editor editor = getSharedPreferences().edit();

        editor.putInt(KEY, badge);
        editor.apply();
    }

    /**
     * Informs if the app has the permission to show badges.
     *
     * @param callback
     *      The function to be exec as the callback
     */
    private void hasPermission (final CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                PluginResult result;

                result = new PluginResult(PluginResult.Status.OK, true);

                callback.sendPluginResult(result);
            }
        });
    }

    /**
     * The Local storage for the application.
     */
    private SharedPreferences getSharedPreferences () {
        Context context = cordova.getActivity().getApplicationContext();

        return context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
    }

    /**
     * @return
     *      The NotificationManager for the app
     */
    private NotificationManager getNotificationManager () {
        Context context = cordova.getActivity().getApplicationContext();

        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Icon bitmap for the local notification.
     */
    public Bitmap getIconBitmap(Context context, String iconUri) {
        Bitmap bmp;

        try{
            Uri uri = Uri.parse(iconUri);
            bmp = getIconFromUri(context, uri);
        } catch (Exception e){
            bmp = getIconFromDrawable(context, iconUri);
        }

        return bmp;
    }

    /**
     * Convert URI to Bitmap.
     *
     * @param uri
     *      Internal image URI
     */
    private Bitmap getIconFromUri (Context context, Uri uri) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        return BitmapFactory.decodeStream(input);
    }

    /**
     * @return
     *      The resource ID for the icon
     */
    private Bitmap getIconFromDrawable(Context context, String largeIcon) {

        String pkgName = cordova.getActivity().getPackageName();

        int resId = getResId(pkgName, largeIcon);
        if (resId == 0) {
            resId = getDrawableIcon(context);
        }

        Resources res = context.getResources();
        Bitmap icon = BitmapFactory.decodeResource(res, resId);

        return icon;
    }

    /**
     * @return
     *      The resource ID of the app icon
     */
    private int getDrawableIcon (Context context) {

        Resources res = context.getResources();
        String pkgName = context.getPackageName();

        int resId;
        resId = res.getIdentifier("icon", "drawable", pkgName);

        return resId;
    }


    /**
     * @return
     *      The resource ID for the small icon
     */
    private int getResIdForSmallIcon (String smallIcon) {
        int resId;

        String pkgName = cordova.getActivity().getPackageName();

        resId = getResId(pkgName, smallIcon);

        if (resId == 0) {
            resId = getResId("android", smallIcon);
        }

        if (resId == 0) {
            resId = getResId("android", "ic_dialog_email");
        }

        return resId;
    }

    /**
     * Returns numerical icon Value
     *
     * @param className
     *      The class name prefix either from Android or the app
     * @param iconName
     *      The resource name
     */
    private int getResId (String className, String iconName) {
        int icon = 0;

        try {
            Class<?> klass  = Class.forName(className + ".R$drawable");

            icon = (Integer) klass.getDeclaredField(iconName).get(Integer.class);
        } catch (Exception ignored) {}

        return icon;
    }
}
