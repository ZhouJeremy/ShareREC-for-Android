package com.mob.glesdemo;

import java.util.Random;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import cn.sharerec.recorder.OnRecorderStateListener;
import cn.sharerec.recorder.Recorder;
import cn.sharerec.recorder.Recorder.LevelMaxFrameSize;
import cn.sharerec.recorder.impl.GLRecorder;

public class MainActivity extends Activity implements OnClickListener, OnRecorderStateListener, 
		OnSeekBarChangeListener, OnPreparedListener {
	private int maxProgress;
	private GLRecorder recorder;
	private TestGLSurfaceView svHost;
	private MediaPlayer mp;
	private boolean paused;
	private boolean fboState;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TestGLSurfaceView.setOpenglVer(getIntent().getFlags() == 11);

		// 初始化UI (init UI)
		setContentView(R.layout.main_activity);
		findViewById(R.id.btnFBO).setOnClickListener(this);
		findViewById(R.id.btnStart).setOnClickListener(this);
		findViewById(R.id.btnStop).setOnClickListener(this);
		findViewById(R.id.btnVideosCenter).setOnClickListener(this);
		final SeekBar sbSpeed = (SeekBar) findViewById(R.id.sbSpeed);
		maxProgress = sbSpeed.getMax() / 2;
		sbSpeed.setProgress(maxProgress);
		sbSpeed.setOnSeekBarChangeListener(this);
		
		playMusic();
		
		// 初始化ShareRec (init ShareRec)
		svHost = (TestGLSurfaceView) findViewById(R.id.svHost);
		recorder = svHost.getRecorder();
		recorder.setDebuggable();
		recorder.setMaxFrameSize(LevelMaxFrameSize.LEVEL_1920_1080);
		recorder.setBitRate(1572864 * 4);

		Toast.makeText(this, R.string.drag_the_seekbar, Toast.LENGTH_SHORT).show();
		sbSpeed.setProgress(sbSpeed.getProgress() + maxProgress / 20);
	}
	
	private void playMusic() {
		try {
			String[] songs = new String[] {
					"http://git.oschina.net/alexyu.yxj/MyTmpFiles/raw/master/Pianoboy%20-%20The%20Truth%20That%20You%20Leave.mp3",
					"http://git.oschina.net/alexyu.yxj/MyTmpFiles/raw/master/Run%20Away%20with%20Me.mp3",
					"http://git.oschina.net/alexyu.yxj/MyTmpFiles/raw/master/%E5%A4%9C%E7%9A%84%E9%92%A2%E7%90%B4%E6%9B%B2%20-%205.mp3"
			};
			Random rnd = new Random(System.currentTimeMillis());
			String song = songs[rnd.nextInt(songs.length)];
			
			mp = new MediaPlayer();
			Uri uri = Uri.parse(song);
			mp.setDataSource(this, uri);
			mp.setLooping(true);
			mp.setOnPreparedListener(this);
			mp.prepareAsync();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void onPrepared(MediaPlayer mp) {
		if (!paused) {
			mp.start();
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnFBO: switchFBOState(); break;
			case R.id.btnStart: startRecorder(); break;
			case R.id.btnStop: stopRecorder(); break;
			case R.id.btnVideosCenter: showVideoCenter(); break;
		}
	}
	
	protected void onPause() {
		// 暂停录像 (pause ShareRec)
		recorder.pauseRecorder();
		svHost.onPause();
		paused = true;
		if (mp != null && mp.isPlaying()) {
			mp.pause();
		}
		super.onPause();
	}
	
	protected void onResume() {
		super.onResume();
		paused = false;
		if (mp != null && !mp.isPlaying()) {
			mp.start();
		}
		svHost.onResume();
		// 恢复录像 (resume ShareRec)
		recorder.resumeRecorder();
	}
	
	protected void onDestroy() {
		super.onDestroy();
		System.exit(0);
	}
	
	// 开启或关闭FBO(turning on / off FBO)
	private void switchFBOState() {
		if (getIntent().getFlags() == 11) {
			Toast.makeText(this, "Eh... He he!", Toast.LENGTH_SHORT).show();
		} else {
			fboState = !fboState;
			svHost.setFboEnable(fboState);
			Button btnFBO = (Button) findViewById(R.id.btnFBO);
			btnFBO.setText(fboState ? R.string.dis_fbo : R.string.en_fbo);
		}
	}
	
	// 启动录像 (start ShareRec)
	private void startRecorder() {
		if (recorder.isAvailable()) {
			recorder.setOnRecorderStateListener(this);
			recorder.startRecorder();
		} else {
			Toast.makeText(this, R.string.not_availiable, Toast.LENGTH_SHORT)
					.show();
		}
	}
	
	// 停止录像 (stop ShareRec)
	private void stopRecorder() {
		recorder.stopRecorder();
	}

	// 显示视频中心 (show video center)
	private void showVideoCenter() {
		recorder.showVideoCenter();
	}
	
	public void onStateChange(Recorder recorder, int state) {
		if (state == Recorder.STATE_STOPPED) {
			// show share page
			this.recorder.setText("GLES Demo");
			this.recorder.addCustomAttr("score", "5000");
			this.recorder.addCustomAttr("name", "ShareRec Developer");
			this.recorder.addCustomAttr("brand", "hehe!");
			this.recorder.addCustomAttr("level", "10");
			this.recorder.showShare();
		}
	}
	
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (svHost != null) {
			svHost.setSpeed(progress - maxProgress);
		}
	}
	
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}
	
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}
	
}