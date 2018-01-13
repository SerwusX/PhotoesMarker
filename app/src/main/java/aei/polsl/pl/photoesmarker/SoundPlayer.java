package aei.polsl.pl.photoesmarker;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * Created by Andrzej on 2018-01-13.
 */

public class SoundPlayer {

    private static MediaPlayer player;

    public static void setContextAndSound(Context context, int raw){
        player = MediaPlayer.create(context, raw);
    }

    public static void playSoundOrStopPlayingIfAlreadyPlaying(){
        if (player.isPlaying()){
            player.stop();
            player.prepareAsync();
        }
        else
            player.start();
    }

}