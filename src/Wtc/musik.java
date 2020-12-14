
package Wtc;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class musik extends Thread{
    
    void mulai()throws UnsupportedAudioFileException, IOException, LineUnavailableException
    {
        
        File file = new File("C:\\Users\\hp\\Downloads\\MATKUL S.5\\Matkul PBO\\code netbeans\\Wtc\\src\\resources\\music\\lagu wav.wav");
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);
        clip.start();
  
    }
    
}
