//Klasa do obsługi odtwarzania dźwięku

package aei.polsl.pl.photoesmarker;

import android.content.Context;
import android.media.MediaPlayer;

class SoundPlayer {

    private static MediaPlayer player;

    static void setContextAndSound(Context context, int raw){
        player = MediaPlayer.create(context, raw);
    }

    static void playSoundOrStopPlayingIfAlreadyPlaying(){
        if (player.isPlaying()){
            player.stop();
            player.prepareAsync();
        }
        else
            player.start();
    }

}
