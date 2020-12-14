package Wtc;

import static Wtc.inputNama.stm;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.WindowConstants;

public abstract class Board extends JPanel implements ActionListener {

    private Dimension d;
    private final Font smallFont = new Font("Helvetica", Font.BOLD, 14);

    private Image ii;
    private final Color dotColor = new Color(192, 192, 0);
    private Color mazeColor;

    private boolean inGame = false;
    private boolean dying = false;

    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int MOUSE_ANIM_DELAY = 2;
    private final int MOUSE_ANIM_COUNT = 4;
    private final int MAX_CATS = 12;
    private final int MOUSE_SPEED = 6;

    private int mouseAnimCount = MOUSE_ANIM_DELAY;
    private int mouseAnimDir = 1;
    private int mouseAnimPos = 0;
    private int N_CATS = 6;
    private int mousesLeft, score;
    private int[] dx, dy;
    private int[] cat_x, cat_y, cat_dx, cat_dy, catSpeed;

    private Image cat;
    private Image mouse1, mouse1up, mouse2up, mouse3up, mouse4up;
    private Image mouse1down, mouse2down, mouse3down, mouse4down;
    private Image mouse1left, mouse2left, mouse3left, mouse4left;
    private Image mouse1right, mouse2right, mouse3right, mouse4right;

    private int mouse_x, mouse_y, moused_x, moused_y;
    private int req_dx, req_dy, view_dx, view_dy;

    private final short levelData[] = {
            19, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 24, 16, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 18, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, 24, 20,
            25, 16, 16, 16, 24, 24, 28, 0, 25, 24, 24, 16, 20, 0, 21,
            1, 17, 16, 20, 0, 0, 0, 0, 0, 0, 0, 17, 20, 0, 21,
            1, 17, 16, 16, 18, 18, 22, 0, 19, 18, 18, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 18, 16, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0, 21,
            1, 25, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 18, 20,
            9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 25, 24, 24, 24, 28
    };

    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};
    private final int maxSpeed = 6;

    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    public Board() {

        loadImages();
        initVariables();
        initBoard();
    }

    private void initBoard() {

        addKeyListener(new TAdapter());

        setFocusable(true);

        setBackground(Color.black);
    }

    private void initVariables() {

        screenData = new short[N_BLOCKS * N_BLOCKS];
        mazeColor = new Color(5, 100, 5);
        d = new Dimension(400, 400);
        cat_x = new int[MAX_CATS];
        cat_dx = new int[MAX_CATS];
        cat_y = new int[MAX_CATS];
        cat_dy = new int[MAX_CATS];
        catSpeed = new int[MAX_CATS];
        dx = new int[4];
        dy = new int[4];

        timer = new Timer(40, this);
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        initGame();
    }

    private void doAnim() {

        mouseAnimCount--;

        if (mouseAnimCount <= 0) {
            mouseAnimCount = MOUSE_ANIM_DELAY;
            mouseAnimPos = mouseAnimPos + mouseAnimDir;

            if (mouseAnimPos == (MOUSE_ANIM_COUNT - 1) || mouseAnimPos == 0) {
                mouseAnimDir = -mouseAnimDir;
            }
        }
    }

    private void playGame(Graphics2D g2d) {

        if (dying) {

            death();

        } else {

            moveMouse();
            drawMouse(g2d);
            moveCats(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {

        g2d.setColor(new Color(0, 32, 48));
        g2d.fillRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);
        g2d.setColor(Color.white);
        g2d.drawRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);

        String s = "Press s to start.";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s, (SCREEN_SIZE - metr.stringWidth(s)) / 2, SCREEN_SIZE / 2);
    }

    private void drawScore(Graphics2D g) {

        int i;
        String s;

        g.setFont(smallFont);
        g.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);

        for (i = 0; i < mousesLeft; i++) {
            g.drawImage(mouse3left, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    private void checkMaze() {

        short i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {

            if ((screenData[i] & 48) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            score += 50;

            if (N_CATS < MAX_CATS) {
                N_CATS++;
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }
            
            try {
                        String sekornye = Integer.toString(score);
                        new inputNama(sekornye).setVisible(true);
                } 
            catch (Exception ex) 
                {
                        Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
                }
                         
            
            initLevel();
        }
        
    }

    private void death() {

        mousesLeft--;
        if (mousesLeft == 0) {
   
            inGame = false;
            
//            try {
//                    new inputNama().setVisible(true);
//                } 
//            catch (Exception ex) {
//                    Logger.getLogger(inputNama.class.getName()).log(Level.SEVERE, null, ex);
//                }
            
            System.exit(0);
             
        }
         
            
//        System.exit(0);
//        continueLevel();
    }
    
    private void moveCats(Graphics2D g2d) {

        short i;
        int pos;
        int count;

        for (i = 0; i < N_CATS; i++) {
            if (cat_x[i] % BLOCK_SIZE == 0 && cat_y[i] % BLOCK_SIZE == 0) {
                pos = cat_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (cat_y[i] / BLOCK_SIZE);

                count = 0;

                if ((screenData[pos] & 1) == 0 && cat_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && cat_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && cat_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && cat_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        cat_dx[i] = 0;
                        cat_dy[i] = 0;
                    } else {
                        cat_dx[i] = -cat_dx[i];
                        cat_dy[i] = -cat_dy[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    cat_dx[i] = dx[count];
                    cat_dy[i] = dy[count];
                }

            }

            cat_x[i] = cat_x[i] + (cat_dx[i] * catSpeed[i]);
            cat_y[i] = cat_y[i] + (cat_dy[i] * catSpeed[i]);
            drawCat(g2d, cat_x[i] + 1, cat_y[i] + 1);

            if (mouse_x > (cat_x[i] - 12) && mouse_x < (cat_x[i] + 12)
                    && mouse_y > (cat_y[i] - 12) && mouse_y < (cat_y[i] + 12)
                    && inGame) {

                if(dying = true){
                        
                        java.awt.EventQueue.invokeLater(new Runnable() 
                        {
                            
                            public void run() 
                            {
                                
                                try {
                                    String sekornye = Integer.toString(score);
                                    new inputNama(sekornye).setVisible(true);
                                } 
                                
                                catch (Exception ex) {
                                    Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }  
                            
                        });
                    
                    }
                    
            }
        }
    }

    private void drawCat(Graphics2D g2d, int x, int y) {

        g2d.drawImage(cat, x, y, this);
    }

    private void moveMouse() {

        int pos;
        short ch;

        if (req_dx == -moused_x && req_dy == -moused_y) {
            moused_x = req_dx;
            moused_y = req_dy;
            view_dx = moused_x;
            view_dy = moused_y;
        }

        if (mouse_x % BLOCK_SIZE == 0 && mouse_y % BLOCK_SIZE == 0) {
            pos = mouse_x / BLOCK_SIZE + N_BLOCKS * (int) (mouse_y / BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
                
            }

            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    moused_x = req_dx;
                    moused_y = req_dy;
                    view_dx = moused_x;
                    view_dy = moused_y;
                }
            }

            // Check for standstill
            if ((moused_x == -1 && moused_y == 0 && (ch & 1) != 0)
                    || (moused_x == 1 && moused_y == 0 && (ch & 4) != 0)
                    || (moused_x == 0 && moused_y == -1 && (ch & 2) != 0)
                    || (moused_x == 0 && moused_y == 1 && (ch & 8) != 0)) {
                moused_x = 0;
                moused_y = 0;
            }
        }
        mouse_x = mouse_x + MOUSE_SPEED * moused_x;
        mouse_y = mouse_y + MOUSE_SPEED * moused_y;
    }

    private void drawMouse(Graphics2D g2d) {

        if (view_dx == -1) {
            drawMouseLeft(g2d);
        } else if (view_dx == 1) {
            drawMouseRight(g2d);
        } else if (view_dy == -1) {
            drawMouseUp(g2d);
        } else {
            drawMouseDown(g2d);
        }
    }

    private void drawMouseUp(Graphics2D g2d) {

        switch (mouseAnimPos) {
            case 1:
                g2d.drawImage(mouse2up, mouse_x + 1, mouse_y + 1, this);
                break;
            case 2:
                g2d.drawImage(mouse3up, mouse_x + 1, mouse_y + 1, this);
                break;
            case 3:
                g2d.drawImage(mouse4up, mouse_x + 1, mouse_y + 1, this);
                break;
            default:
                g2d.drawImage(mouse1, mouse_x + 1, mouse_y + 1, this);
                break;
        }
    }

    private void drawMouseDown(Graphics2D g2d) {

        switch (mouseAnimPos) {
            case 1:
                g2d.drawImage(mouse2down, mouse_x + 1, mouse_y + 1, this);
                break;
            case 2:
                g2d.drawImage(mouse3down, mouse_x + 1, mouse_y + 1, this);
                break;
            case 3:
                g2d.drawImage(mouse4down, mouse_x + 1, mouse_y + 1, this);
                break;
            default:
                g2d.drawImage(mouse1, mouse_x + 1, mouse_y + 1, this);
                break;
        }
    }

    private void drawMouseLeft(Graphics2D g2d) {

        switch (mouseAnimPos) {
            case 1:
                g2d.drawImage(mouse2left, mouse_x + 1, mouse_y + 1, this);
                break;
            case 2:
                g2d.drawImage(mouse3left, mouse_x + 1, mouse_y + 1, this);
                break;
            case 3:
                g2d.drawImage(mouse4left, mouse_x + 1, mouse_y + 1, this);
                break;
            default:
                g2d.drawImage(mouse1left, mouse_x + 1, mouse_y + 1, this);
                break;
        }
    }

    private void drawMouseRight(Graphics2D g2d) {

        switch (mouseAnimPos) {
            case 1:
                g2d.drawImage(mouse2right, mouse_x + 1, mouse_y + 1, this);
                break;
            case 2:
                g2d.drawImage(mouse3right, mouse_x + 1, mouse_y + 1, this);
                break;
            case 3:
                g2d.drawImage(mouse4right, mouse_x + 1, mouse_y + 1, this);
                break;
            default:
                g2d.drawImage(mouse1right, mouse_x + 1, mouse_y + 1, this);
                break;
        }
    }

    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2d.setColor(mazeColor);
                g2d.setStroke(new BasicStroke(2));

                if ((screenData[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) {
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) {
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) {
                    g2d.setColor(dotColor);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }

                i++;
            }
        }
    }

    private void initGame() {

        mousesLeft = 0;
        score = 0;
        initLevel();
        N_CATS = 6;
        currentSpeed = 3;
    }

    private void initLevel() {

        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
        }

        continueLevel();
    }

    private void continueLevel() {

        short i;
        int dx = 1;
        int random;

        for (i = 0; i < N_CATS; i++) {

            cat_y[i] = 4 * BLOCK_SIZE;
            cat_x[i] = 4 * BLOCK_SIZE;
            cat_dy[i] = 0;
            cat_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            catSpeed[i] = validSpeeds[random];
        }

        mouse_x = 7 * BLOCK_SIZE;
        mouse_y = 11 * BLOCK_SIZE;
        moused_x = 0;
        moused_y = 0;
        req_dx = 0;
        req_dy = 0;
        view_dx = -1;
        view_dy = 0;
        dying = false;
    }

    private void loadImages() {

        cat = new ImageIcon("src/resources/images/cat.png").getImage();
        mouse1 = new ImageIcon("src/resources/images/mouse.png").getImage();
        mouse1up = new ImageIcon("src/resources/images/up1.png").getImage();
        mouse2up = new ImageIcon("src/resources/images/up2.png").getImage();
        mouse3up = new ImageIcon("src/resources/images/up3.png").getImage();
        mouse4up = new ImageIcon("src/resources/images/up4.png").getImage();
        mouse1down = new ImageIcon("src/resources/images/down1.png").getImage();
        mouse2down = new ImageIcon("src/resources/images/down2.png").getImage();
        mouse3down = new ImageIcon("src/resources/images/down3.png").getImage();
        mouse4down = new ImageIcon("src/resources/images/down4.png").getImage();
        mouse1left = new ImageIcon("src/resources/images/left1.png").getImage();
        mouse2left = new ImageIcon("src/resources/images/left2.png").getImage();
        mouse3left = new ImageIcon("src/resources/images/left3.png").getImage();
        mouse4left = new ImageIcon("src/resources/images/left4.png").getImage();
        mouse1right = new ImageIcon("src/resources/images/right1.png").getImage();
        mouse2right = new ImageIcon("src/resources/images/right2.png").getImage();
        mouse3right = new ImageIcon("src/resources/images/right3.png").getImage();
        mouse4right = new ImageIcon("src/resources/images/right4.png").getImage();

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);
        doAnim();

        if (inGame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }

        g2d.drawImage(ii, 5, 5, this);
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                } else if (key == KeyEvent.VK_PAUSE) {
                    if (timer.isRunning()) {
                        timer.stop();
                    } else {
                        timer.start();
                    }
                }
            } else {
                if (key == 's' || key == 'S') {
                    inGame = true;
                    initGame();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == Event.LEFT || key == Event.RIGHT
                    || key == Event.UP || key == Event.DOWN) {
                req_dx = 0;
                req_dy = 0;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        repaint();
    }
}
