package uz.nurlibaydev.workmanagerguide.workers

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import uz.nurlibaydev.workmanagerguide.R
import uz.nurlibaydev.workmanagerguide.network.FileApi
import uz.nurlibaydev.workmanagerguide.utils.WorkerKeys
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

/**
 *  Created by Nurlibay Koshkinbaev on 18/01/2023 13:18
 */

class DownloadWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        startForegroundService()
        delay(5000L)
        val response = FileApi.instance.downloadImage()
        response.body()?.let { body ->
            return withContext(Dispatchers.IO) {
                val file = File(context.cacheDir, "image.jpg")
                val outputStream = FileOutputStream(file)
                outputStream.use {
                    try {
                        it.write(body.bytes())
                    } catch (e: IOException) {
                        return@withContext Result.failure(
                            workDataOf(WorkerKeys.ERROR_MSG to e.localizedMessage)
                        )
                    }
                }
                Result.success(
                    workDataOf(
                        WorkerKeys.IMG_URI to file.toURI().toString()
                    )
                )
            }
        }
        if (!response.isSuccessful) {
            if (response.code().toString().startsWith("5")) {
                return Result.retry()
            }
            return Result.failure(
                workDataOf(
                    WorkerKeys.ERROR_MSG to "Network Error"
                )
            )
        }
        return Result.failure(
            workDataOf(
                WorkerKeys.ERROR_MSG to "Unknown error"
            )
        )
    }

    private suspend fun startForegroundService() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(), NotificationCompat.Builder(
                    context, "download_channel"
                ).setSmallIcon(R.drawable.ic_launcher_background).setContentText("Downloading...").setContentTitle("Download in progress").build()
            )
        )

//        val channelId =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                createNotificationChannel("my_service", "My Background Service")
//            } else {
//                ""
//            }
//
//        val notificationBuilder = NotificationCompat.Builder(context, channelId)
//        val notification = notificationBuilder.setOngoing(true)
//            .setSmallIcon(R.mipmap.ic_launcher)
//            .setPriority(NotificationManager.IMPORTANCE_HIGH)
//            .setCategory(Notification.CATEGORY_SERVICE)
//            .build()
//        setForeground(
//            ForegroundInfo(101, notification)
//        )
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun createNotificationChannel(channelId: String, channelName: String): String {
//        val channel = NotificationChannel(
//            channelId,
//            channelName, NotificationManager.IMPORTANCE_NONE
//        )
//        channel.lightColor = Color.BLUE
//        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
//        val service = getSystemService(context, NotificationManager::class.java) as NotificationManager
//        service.createNotificationChannel(channel)
//        return channelId
//    }
}