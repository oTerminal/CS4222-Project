import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

//hi

public class SillyGuitar {

    // These are simply the screens of our game, basically what screen in our sketch we will be in.
    public static enum GameState {
        TUTORIAL,
        INSTRUMENT;
    }

    public static enum TuningState {
        DEFAULT_WRONG,
        CUSTOM,
        
    }

    // This class will control what screen to change to and what is currently showing.
    public static class ScreenManager extends JPanel{
        CardLayout cardLayout;
        SplashScreen splashScreen;
        GuitarScreen guitarScreen;

        ScreenManager() {
            cardLayout = new CardLayout();
            setLayout(cardLayout);

            splashScreen = new SplashScreen(this);
            guitarScreen = new GuitarScreen();

            add(splashScreen, GameState.TUTORIAL.name());
            add(guitarScreen, GameState.INSTRUMENT.name());

            cardLayout.show(this, GameState.TUTORIAL.name());
        }

        void switchTo(GameState state)
        {
            cardLayout.show(this, state.name());
        }

    }

    // This is the simply the class for the splash screen. 
    public static class SplashScreen extends JPanel{
        ScreenManager screenManager;

        SplashScreen(ScreenManager screenManager) {
            this.screenManager = screenManager;

            JButton continueBtn = new JButton("Continue");
            // Listeners learnt in lecture
            continueBtn.addActionListener(e -> screenManager.switchTo(GameState.INSTRUMENT));
            add(continueBtn);
        }
    }

    // Self explanatory: screen where the guitar can be played.
    public static class GuitarScreen extends JPanel {
        PopupManager popupManager;
        StringPanel stringPanel;

        GuitarScreen() {
            popupManager = new PopupManager();
            stringPanel = new StringPanel();

            add(stringPanel);

            JButton continueBtn = new JButton("Continue");
            // Listeners learnt in lecture
            continueBtn.addActionListener(e -> JOptionPane.showMessageDialog(null, popupManager.triggerPopup()));
            add(continueBtn);

            
        }
    }

    public static class StringPanel extends JPanel {
        SoundEngine soundEngine;

        //AI - Start
        private final int[] yPositions = {50, 100, 150, 200, 250, 300};
        private final double[] frequencies = {110.0, 146.83, 196.00, 246.94, 329.63, 440.0};

        public StringPanel() {
            soundEngine = new SoundEngine();

            setPreferredSize(new Dimension(800, 400));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    for (int i = 0; i < yPositions.length; i++) {
                        final int idx = i;   // ✅ Make a final copy for the thread/lambda // HAD TO GIVE ERROR FOR THIS LINE
                        if (Math.abs(e.getY() - yPositions[i]) < 10) {
                            new Thread(() -> {
                                try {
                                    soundEngine.playKarplusStrong(frequencies[idx], 1.5);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }).start();
                        }
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g; // Not AI

            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));  // Not AI

            for (int y : yPositions) {
                g2d.drawLine(50, y, 750, y);
            }
        }

        /** Plays a plucked string using the Karplus–Strong algorithm. */

    } //AI - Finish

    public static class TuningPanel extends JPanel {

    }

    public static class GoofyButton extends JButton {

    }

    public static class SoundEngine {

        //AI - Start (But I put this code into this class.)
        public void playKarplusStrong(double freq, double durationSeconds) throws LineUnavailableException {
            final float sampleRate = 44100;
            int bufferSize = (int) (sampleRate / freq);

            // Fill buffer with noise (initial excitation)
            double[] buffer = new double[bufferSize];
            Random rand = new Random();
            for (int i = 0; i < bufferSize; i++) {
                buffer[i] = rand.nextDouble() - 0.5;
            }

            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, true);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();

            int samplesToPlay = (int) (durationSeconds * sampleRate);
            byte[] audio = new byte[2];

            int index = 0;
            while (samplesToPlay-- > 0) {
                // Karplus–Strong update
                double first = buffer[index];
                double next = buffer[(index + 1) % buffer.length];
                double newSample = 0.996 * 0.5 * (first + next);
                buffer[index] = newSample;

                index = (index + 1) % buffer.length;

                // Convert to 16-bit audio
                short s = (short) (newSample * Short.MAX_VALUE);
                audio[0] = (byte) (s >> 8);
                audio[1] = (byte) (s);

                line.write(audio, 0, 2);
            }

            line.drain();
            line.close();
        } // AI - End

    }

    public static class Cursor {

    }

    public static class PiSequence {

    }

    public static class PopupManager {
        private String[] funFacts;
        Random random;
        String currentPopup;

        PopupManager() {
            random = new Random();
            funFacts =  new String[] {
                "Did you know that 1 in 12 men and 1 in 200 women in the world are affected by Color Blindness!!!!!!",
                "gurt", 
                "hi"};
        }

        String triggerPopup() {
            int index = random.nextInt(funFacts.length);
            return funFacts[index];
        }
    
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SillyGuitar");
        ScreenManager screenManager = new ScreenManager();

        frame.add(screenManager);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }
}