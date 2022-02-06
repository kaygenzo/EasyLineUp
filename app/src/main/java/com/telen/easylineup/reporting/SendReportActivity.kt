package com.telen.easylineup.reporting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.telen.easylineup.BaseActivity
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_message_loading.*
import timber.log.Timber
import java.lang.Exception
import java.util.concurrent.TimeUnit

class SendReportActivity: BaseActivity() {
    companion object {
        const val TIMEOUT_CLOSE_REPORT_SCREEN = 1500L
    }

    private val _disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_loading)
        loadingMessage.setText(R.string.report_sending_message)

        val jsonStringData = intent.getStringExtra(Intent.EXTRA_TEXT)
        val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        val jsonData = JsonParser.parseString(jsonStringData).asJsonObject

        _disposables.add(authenticate()
                .andThen(Completable.defer {
                    imageUri?.let {
                        getScreenshotUrl(it).flatMapCompletable { imageUrl ->
                            jsonData.addProperty("imageUrl", imageUrl)
                            storeFireStore(jsonData)
                        }
                    } ?: let {
                        Completable.error(Exception("No image found to join to report"))
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("Successfully stored remotely")
                    loading_progress.visibility = View.INVISIBLE
                    loading_status.apply {
                        setImageResource(R.drawable.ic_check_green_24dp)
                        visibility = View.VISIBLE
                    }
                    loadingMessage.setText(R.string.report_sending_message_success)
                    closeScreen()
                }, {
                    Timber.e(it)
                    loading_progress.visibility = View.INVISIBLE
                    loading_status.apply {
                        setImageResource(R.drawable.ic_warning_red_24dp)
                        visibility = View.VISIBLE
                    }
                    loadingMessage.setText(R.string.report_sending_message_failure)
                    closeScreen()
                })
        )
    }

    private fun closeScreen() {
        _disposables.add(Completable.defer {
            Completable.timer(TIMEOUT_CLOSE_REPORT_SCREEN, TimeUnit.MILLISECONDS)
        }.subscribe({
            finish()
        }, {
            finish()
        }))
    }

    override fun onDestroy() {
        super.onDestroy()
        _disposables.clear()
    }

    private fun authenticate(): Completable {
        return Completable.create { emitter ->
            val auth: FirebaseAuth = Firebase.auth
            auth.currentUser?.let {
                emitter.onComplete()
            } ?: let {
                auth.signInAnonymously()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                auth.currentUser?.let {
                                    emitter.onComplete()
                                } ?: let {
                                    emitter.onError(IllegalStateException())
                                }
                            } else {
                                task.exception?.let {
                                    emitter.onError(it)
                                }
                            }
                        }
            }
        }
    }

    private fun getScreenshotUrl(imageUri: Uri): Single<String> {
        return Single.create { emitter ->
            val firebaseStorage = Firebase.storage
            val storageRef = firebaseStorage.reference

            val screenshotRef = storageRef.child("${BuildConfig.reportStorageRoot}/${imageUri.lastPathSegment}")
            val uploadTask = screenshotRef.putFile(imageUri)
            uploadTask
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        else {
                            screenshotRef.downloadUrl
                        }
                    }
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result
                            Timber.d("downloadUri=$downloadUri")
                            emitter.onSuccess(downloadUri.toString())
                        } else {
                            task.exception?.let {
                                emitter.onError(it)
                            }
                        }
                    }
        }
    }

    private fun storeFireStore(json: JsonObject): Completable {
        return Completable.create { emitter ->
            val db = Firebase.firestore
            val mapData = hashMapOf<String, String>()
            json.entrySet().forEach {
                mapData[it.key] = it.value.asString
            }
            db.collection(BuildConfig.reportStorageRoot)
                    .add(mapData)
                    .addOnSuccessListener {
                        Timber.d("Successfully stored")
                        emitter.onComplete()
                    }
                    .addOnFailureListener {
                        emitter.onError(it)
                    }
        }
    }
}