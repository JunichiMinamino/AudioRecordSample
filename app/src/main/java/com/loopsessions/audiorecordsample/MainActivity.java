package com.loopsessions.audiorecordsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "MainActivity";

	private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 100;

	public final static int SAMPLING_RATE = 44100;

	private int m_iSampleSize = 0;
	private AudioRecord m_audioRecord = null;
	private short[] m_sInputData;
	private short[] m_sOutputData;

	private boolean m_isPlaying = false;

	private float[] m_fInputBuffer;
	private float[] m_fOutputBuffer;

	private float m_fRateTest = 1.0f;
	private long[] m_lAveValue = {0, 0};

	private TextView[] textView = new TextView[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		int[] iResTextView = {R.id.textView01, R.id.textView02};
		for (int i = 0; i < 2; i++) {
			textView[i] = (TextView) findViewById(iResTextView[i]);
			textView[i].setTextSize(32.0f);
		}

		final String[] strButtonTitle = {"START", "STOP"};
		final Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (button.getText().toString().equals(strButtonTitle[0])) {
					startRecord();
					button.setText(strButtonTitle[1]);
				} else {
					stopRecord();
					button.setText(strButtonTitle[0]);
				}
			}
		});
		button.setText(strButtonTitle[0]);

		SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
		seekBar.setProgress(10);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
//				Log.v(TAG, "" + progress);	// 0 - 100
				m_fRateTest = (float)progress * 0.1f;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		checkPermission();
    }

	private void checkPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
				// put your code for Version>=Marshmallow

			} else {
				if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
					Toast.makeText(this, "App required access to audio", Toast.LENGTH_SHORT).show();
				}
				requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
			}

		} else {
			// put your code for Version < Marshmallow
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
			if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(getApplicationContext(), "Application will not have audio on record", Toast.LENGTH_SHORT).show();
			}
		}
	}

	// Recに必要なデータを確保
	private void initAudioRecord() {
		// AudioRecordで必要なバッファサイズを取得
		int bufSize = android.media.AudioRecord.getMinBufferSize(SAMPLING_RATE,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		Log.v(TAG, "bufSize:" + bufSize);

		m_iSampleSize = bufSize / 2;

		m_sInputData = new short[m_iSampleSize];
		m_sOutputData = new short[m_iSampleSize];

		initNativeData();
	}

	public void initNativeData() {
		m_fInputBuffer = new float[m_iSampleSize];
		m_fOutputBuffer = new float[m_iSampleSize];
	}

	private void startAudioRecord() {
		// AudioRecordオブジェクトを作成
		m_audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
				SAMPLING_RATE,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				m_iSampleSize * 2);

		// Rec開始
		m_audioRecord.startRecording();
	}

	private void startRecord() {
		initAudioRecord();
		startAudioRecord();
		m_isPlaying = true;

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (m_isPlaying) {
					// 読み込み
					m_audioRecord.read(m_sInputData, 0, m_iSampleSize);

					int numberFrames = m_iSampleSize;

					for (int i = 0; i < numberFrames; i++) {
						m_fInputBuffer[i] = (float)m_sInputData[i];
						m_lAveValue[0] += m_sInputData[i];
					}
					m_lAveValue[0] /= numberFrames;

					// Native処理
					processAudio(numberFrames, m_fInputBuffer, m_fOutputBuffer, m_fRateTest);

					for (int i = 0; i < numberFrames; i++) {
						m_sOutputData[i] = (short)m_fOutputBuffer[i];
						m_lAveValue[1] += m_sOutputData[i];
					}
					m_lAveValue[1] /= numberFrames;

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							textView[0].setText(String.valueOf(m_lAveValue[0]));
							textView[1].setText(String.valueOf(m_lAveValue[1]));
						}
					});
				}
			}
		});
		thread.start();
	}

	private void stopRecord() {
		m_isPlaying = false;

		m_audioRecord.stop();
		m_audioRecord.release();
		m_audioRecord = null;
	}


	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();
	public native float[] processAudio(long numberFrames, float[] fInputData, float[] fOutputData, float fRateTest);

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
