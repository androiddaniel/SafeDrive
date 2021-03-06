package com.insomniac.safedrive;

/**
 * Created by Sanjeev on 3/6/2018.
 */


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;

/**
 * Provides helper methods to request permissions from components other than Activities.
 */
public class PermissionHelper {
    private static final String TAG = PermissionHelper.class.getSimpleName();
    public static final String KEY_PERMISSIONS = "permissions";
    public static final String KEY_GRANT_RESULTS = "grantResults";
    public static final String KEY_REQUEST_CODE = "requestCode";
    public static final String KEY_RESULT_RECEIVER = "resultReceiver";


    /**
     * Requests permissions to be granted to this application.
     *
     * This method is a wrapper around
     * {@link android.support.v4.app.ActivityCompat#requestPermissions(android.app.Activity, String[], int)}
     * which works in a similar way, except it can be called from non-activity contexts. When called, it
     * displays a notification with a customizable title and text. When the user taps the notification, an
     * activity is launched in which the user is prompted to allow or deny the request.
     *
     * After the user has made a choice,
     * {@link android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])}
     * is called, reporting whether the permissions were granted or not.
     *
     * @param context The context from which the request was made. The context supplied must implement
     * {@link android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback} and will receive the
     * result of the operation.
     * @param permissions The requested permissions
     * @param requestCode Application specific request code to match with a result reported to
     * {@link android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])}
     * @param notificationTitle The title for the notification
     * @param notificationText The text for the notification
     * @param notificationIcon Resource identifier for the notification icon
     */
    public static <T extends Context & OnRequestPermissionsResultCallback> void requestPermissions(final T context, String[] permissions, int requestCode, String notificationTitle, String notificationText, int notificationIcon) {
        ResultReceiver resultReceiver = new ResultReceiver(new Handler(Looper.getMainLooper())) {
            @Override
            protected void onReceiveResult (int resultCode, Bundle resultData) {
                String[] outPermissions = resultData.getStringArray(KEY_PERMISSIONS);
                int[] grantResults = resultData.getIntArray(KEY_GRANT_RESULTS);
                context.onRequestPermissionsResult(resultCode, outPermissions, grantResults);
            }
        };

        Intent permIntent = new Intent(context, PermissionRequestActivity.class);
        permIntent.putExtra(KEY_RESULT_RECEIVER, resultReceiver);
        permIntent.putExtra(KEY_PERMISSIONS, permissions);
        permIntent.putExtra(KEY_REQUEST_CODE, requestCode);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(permIntent);

        PendingIntent permPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(notificationIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setOngoing(true)
                //.setCategory(Notification.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setWhen(0)
                .setContentIntent(permPendingIntent)
                .setStyle(null);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(requestCode, builder.build());
    }


    /**
     * A blank { Activity} on top of which permission request dialogs can be displayed
     */
    public static class PermissionRequestActivity extends AppCompatActivity {
        ResultReceiver resultReceiver;
        String[] permissions;
        int requestCode;

        /**
         * Called when the user has made a choice in the permission dialog.
         *
         * This method wraps the responses in a {@link Bundle} and passes it to the {@link ResultReceiver}
         * specified in the {@link Intent} that started the activity, then closes the activity.
         */
        @Override
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            Bundle resultData = new Bundle();
            resultData.putStringArray(KEY_PERMISSIONS, permissions);
            resultData.putIntArray(KEY_GRANT_RESULTS, grantResults);
            resultReceiver.send(requestCode, resultData);
            finish();
        }


        /**
         * Called when the activity is started.
         *
         * This method obtains several extras from the {@link Intent} that started the activity: the request
         * code, the requested permissions and the {@link ResultReceiver} which will receive the results.
         * After that, it issues the permission request.
         */
        @Override
        protected void onStart() {
            super.onStart();

            resultReceiver = this.getIntent().getParcelableExtra(KEY_RESULT_RECEIVER);
            permissions = this.getIntent().getStringArrayExtra(KEY_PERMISSIONS);
            requestCode = this.getIntent().getIntExtra(KEY_REQUEST_CODE, 0);

            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
    }
}
