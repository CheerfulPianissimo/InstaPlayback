package com.example.instaplayback

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import android.media.audiofx.AcousticEchoCanceler
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    companion object {
        private const val STORAGE_PERMISSION_CODE = 101
        private const val AUDIO_PERMISSION_CODE=225
    }

    var isRecording = false
    var am: AudioManager? = null
    var record: AudioRecord? = null
    var track: AudioTrack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRecordAndTrack()
        am = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Defining Buttons
        val playbackB: Button? = findViewById(R.id.playback)
        object : Thread() {
            override fun run() {
                recordAndPlay()
            }
        }.start()

        // Set Buttons on Click Listeners
        playbackB?.setOnClickListener {
            if (!isRecording) {
                startRecordAndPlay()
            }else{
                stopRecordAndPlay()
            }
        }
    }

    // Function to check and request permission.
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode ==AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Audio Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Audio Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun initRecordAndTrack() {
        val min = AudioRecord.getMinBufferSize(
            8000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        checkPermission(
            Manifest.permission.RECORD_AUDIO,
            STORAGE_PERMISSION_CODE)
        record = AudioRecord(
            MediaRecorder.AudioSource.VOICE_PERFORMANCE,
            8000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            min
        )
        /*if (AcousticEchoCanceler.isAvailable()) {
            val echoCanceler = AcousticEchoCanceler.create(record!!.audioSessionId)
            echoCanceler.enabled = true
        }*/
        val maxJitter = AudioTrack.getMinBufferSize(
            8000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        track = AudioTrack(
            AudioManager.MODE_IN_COMMUNICATION,
            8000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxJitter,
            AudioTrack.MODE_STREAM
        )
    }

    private fun recordAndPlay() {
        val lin = ShortArray(1024)
        am!!.mode = AudioManager.MODE_IN_COMMUNICATION
        while (true) {
            if (isRecording) {
                var num = record!!.read(lin, 0, 1024)
                track!!.write(lin, 0, num)
            }
        }
    }

    private fun startRecordAndPlay() {
        record!!.startRecording()
        track!!.play()
        isRecording = true
    }

    private fun stopRecordAndPlay() {
        record!!.stop()
        track!!.pause()
        isRecording = false
    }
}