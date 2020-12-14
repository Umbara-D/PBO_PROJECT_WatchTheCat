package Wtc;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public  class WatchTheCat extends javax.swing.JFrame {

    public WatchTheCat() {

        initUI();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        dispose();
    }
    
    private void initUI() {

        add(new Board() {});

        setTitle("WATCH THE CAT");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 420);
        setLocationRelativeTo(null);
    }

    public static void main() {
        String skor="";
        
        inputNama ex = new inputNama(skor);
        ex.setVisible(true);
            
    }
}