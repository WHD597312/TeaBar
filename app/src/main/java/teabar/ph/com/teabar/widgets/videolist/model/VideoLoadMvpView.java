package teabar.ph.com.teabar.widgets.videolist.model;

import android.media.MediaPlayer;
import teabar.ph.com.teabar.widgets.videolist.widget.TextureVideoView;


/**
 * @author Wayne
 */
public interface VideoLoadMvpView {

    TextureVideoView getVideoView();

    void videoBeginning();

    void videoStopped();

    void videoPrepared(MediaPlayer player);

    void videoResourceReady(String videoPath);
}
