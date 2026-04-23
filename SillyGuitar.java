import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;

public class SillyGuitar {

    // These are simply the screens of our game, basically what screen in our sketch
    // we will be in.
    public static enum GameState {
        TUTORIAL,
        INSTRUMENT;
    }

    public static enum TuningState {
        DEFAULT_WRONG,
        CUSTOM,

    }
    
    public static SoundEngine soundEngine = new SoundEngine();
    // This class will control what screen to change to and what is currently
    // showing.
    public static class ScreenManager extends JPanel {
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

        void switchTo(GameState state) {
            cardLayout.show(this, state.name());
        }

    }

    // This is the simply the class for the splash screen.
    public static class SplashScreen extends JPanel {
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
        PiSequence piSequence;


        GuitarScreen() {
            popupManager = new PopupManager();
            stringPanel = new StringPanel();
            piSequence = new PiSequence();

            add(stringPanel);
            add(piSequence);

            JButton continueBtn = new JButton("Continue");
            // Listeners learnt in lecture
            continueBtn.addActionListener(e -> popupManager.triggerRandomPopup());
            add(continueBtn);
        }
    }

    public static class StringPanel extends JPanel {
        PopupManager popupManager;
        // AI - Start
        // Horizontal fret lines (x‑positions)
        private final int[] frets = { 150, 250, 350, 450, 550, 650 };
        private final int[] yPositions = { 50, 100, 150, 200, 250, 300, 350 };
        private final double[] frequencies = { 110.0, 146.83, 196.00, 246.94, 329.63, 432, 440 };
        private final double[] gMajorFreq = { 92.50, 116.54, 146.83, 196.00, 246.94, 369.99 };

        public StringPanel() {
            popupManager = new PopupManager();

            setPreferredSize(new Dimension(800, 400));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Detect which string was clicked
                    for (int i = 0; i < yPositions.length; i++) {
                        final int idx = i; // required for thread/lambda
                        if (Math.abs(e.getY() - yPositions[i]) < 10) {
                            // Detect fret clicked (if any)
                            int fretIndex = -1;
                            for (int f = 0; f < frets.length; f++) {
                                if (e.getX() < frets[f]) {
                                    fretIndex = f; // the first fret to the right is the one you pressed
                                    break;
                                }
                            }

                            if (e.getX() > frets[frets.length - 1]) {
                                fretIndex = frets.length; // extra frets beyond the last one (rare, but fine)
                            }

                            final int fIdx = fretIndex;

                            double base = frequencies[idx];
                            int semitone = (fIdx >= 0) ? fIdx : 0;
                            double noteFreq = base * Math.pow(2, semitone / 12.0);

                            SillyGuitar.soundEngine.playNote(noteFreq);

                        }
                    }
                }
            });

            InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = getActionMap();

            Action EAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread(() -> {
                        try {
                            SillyGuitar.soundEngine.playNote(frequencies[0]);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
            };

            Action BAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread(() -> {
                        try {
                            SillyGuitar.soundEngine.playNote(frequencies[1]);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
            };

            Action GAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread(() -> {
                        try {
                            SillyGuitar.soundEngine.playNote(frequencies[2]);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
            };

            Action DAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread(() -> {
                        try {
                            SillyGuitar.soundEngine.playNote(frequencies[3]);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
            };

            Action AAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread(() -> {
                        try {
                            SillyGuitar.soundEngine.playNote(frequencies[4]);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
            };

            Action eAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread(() -> {
                        try {
                            SillyGuitar.soundEngine.playNote(frequencies[5]);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
            };

            Action ghostAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread(() -> {
                        try {
                            SillyGuitar.soundEngine.playNote(frequencies[6]);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
            };
            
            Action GMajorAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    soundEngine.playNote(gMajorFreq[0]);
                    soundEngine.playNote(gMajorFreq[1]);
                    soundEngine.playNote(gMajorFreq[2]);
                    soundEngine.playNote(gMajorFreq[3]);
                    soundEngine.playNote(gMajorFreq[4]);
                    soundEngine.playNote(gMajorFreq[5]);
                }
            };

            inputMap.put(KeyStroke.getKeyStroke("1"), "EAction");
            inputMap.put(KeyStroke.getKeyStroke("2"), "BAction");
            inputMap.put(KeyStroke.getKeyStroke("3"), "GAction");
            inputMap.put(KeyStroke.getKeyStroke("4"), "DAction");
            inputMap.put(KeyStroke.getKeyStroke("5"), "AAction");
            inputMap.put(KeyStroke.getKeyStroke("6"), "eAction");
            inputMap.put(KeyStroke.getKeyStroke("7"), "ghostAction");
            inputMap.put(KeyStroke.getKeyStroke("G"), "GMajorAction");

            actionMap.put("EAction", EAction);
            actionMap.put("BAction", BAction);
            actionMap.put("GAction", GAction);
            actionMap.put("DAction", DAction);
            actionMap.put("AAction", AAction);
            actionMap.put("eAction", eAction);
            actionMap.put("ghostAction", ghostAction);
            actionMap.put("GMajorAction", GMajorAction);
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g; // Not AI

            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3)); // Not AI

            for (int y : yPositions) {
                g2d.drawLine(50, y, 750, y);
            }

            g2d.setColor(Color.GRAY);
            for (int x : frets) {
                g2d.drawLine(x, yPositions[0] - 20, x, yPositions[yPositions.length - 1] + 20);
            }

        }

    } // AI - Finish

    public static class TuningPanel extends JPanel {

    }

    public static class GoofyButton extends JButton {

    }

    public static class SoundEngine {

        private static final float SAMPLE_RATE = 44100f;
        private static final int BUFFER_SIZE = 512;
        private static final double MASTER_GAIN = 0.2; // prevents clipping

        private SourceDataLine line;
        private volatile boolean running = true;
        FloatControl vol;
        float volume = 100;

        // Active plucked strings
        private final java.util.List<KarplusString> strings = new java.util.concurrent.CopyOnWriteArrayList<>();

        public SoundEngine() {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
                line = AudioSystem.getSourceDataLine(format);
                line.open(format, BUFFER_SIZE * 2);
                line.start();
                
                vol = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);//using AI
                updateVolume();

                Thread audioThread = new Thread(this::audioLoop);
                audioThread.setDaemon(true);
                audioThread.start();

            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }
        
        public void updateVolume() {
                float min = vol.getMinimum();// -80.0 dB
                float max = vol.getMaximum();// 6.0206 dB
                float gain = min + (volume / 100.0f) * (max - min);
                vol.setValue(gain);
        }

        /** Main audio loop */
        private void audioLoop() {
            byte[] outBuffer = new byte[BUFFER_SIZE * 2];

            while (running) {
                for (int i = 0; i < BUFFER_SIZE; i++) {
                    double mix = 0.0;

                    for (KarplusString s : strings) {
                        mix += s.nextSample();
                        if (s.isFinished()) {
                            strings.remove(s);
                        }
                    }

                    // normalize + clip
                    mix *= MASTER_GAIN;
                    mix = Math.max(-1.0, Math.min(1.0, mix));

                    short sample = (short) (mix * Short.MAX_VALUE);

                    outBuffer[i * 2] = (byte) (sample >> 8);
                    outBuffer[i * 2 + 1] = (byte) (sample);
                }
                line.write(outBuffer, 0, outBuffer.length);
            }
        }

        /** Public API: pluck a string */
        public void playNote(double frequency) {
            // limit polyphony (like a real guitar)
            if (strings.size() < 8) {
                strings.add(new KarplusString(frequency));
            }
        }

        /** One Karplus–Strong string */
        private static class KarplusString {
            private final double[] buffer;
            private int index = 0;
            private int life;

            KarplusString(double freq) {
                int size = Math.max(2, (int) (SAMPLE_RATE / freq));
                buffer = new double[size];
                java.util.Random r = new java.util.Random();

                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = (r.nextDouble() - 0.5);
                }
                life = (int) (SAMPLE_RATE * 0.7); // ~0.7s decay
            }

            double nextSample() {
                double first = buffer[index];
                double next = buffer[(index + 1) % buffer.length];
                double value = 0.996 * 0.5 * (first + next);

                buffer[index] = value;
                index = (index + 1) % buffer.length;
                life--;

                return value;
            }

            boolean isFinished() {
                return life <= 0;
            }
        }
    }

    public static class customCursor {
        Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon("guitarPick.png").getImage(), new Point (0, 0), "Custom cursor");
    }

     public static class PiSequence extends JPanel {
        PiSequence() {
            JTextField textField = new JTextField("Enter PI Digits...");
            JLabel label = new JLabel("0%");
            textField.setPreferredSize(new Dimension(250, 40));
            
            textField.addActionListener(e -> {
                String input = textField.getText();//using AI
                label.setText((100 - volumeCalc(input)) + "%");
                textField.setText("Enter PI Digits...");
                textField.transferFocus();//using AI
            });
            
            add(textField);
            add(label);
        }

        int volumeCalc(String input) {
            String pi = "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706";
            int inputLength = Math.min(input.length(), pi.length());
            String truePi = pi.substring(0, inputLength);

            if (truePi.equals(input)) {
                int volume = pi.length() - inputLength;
                soundEngine.volume = volume;
                soundEngine.updateVolume();
                return volume;
            }
            else{
                int volume = 100;
                soundEngine.volume = volume;
                soundEngine.updateVolume();
                return 100;
            }
        }
    }

    public static class PopupManager {
        private String[] funFacts;
        Random random;
        String currentPopup;

        PopupManager() {
            random = new Random();
            funFacts = new String[] {
                    "Did you know that 1 in 12 men and 1 in 200 women in the world are affected by Color Blindness!!!!!!",
                    "gurt",
                    "hi" };
        }

        void triggerRandomPopup() {
            if (Math.random() > 0.7) {
                int index = random.nextInt(funFacts.length);
                JOptionPane.showMessageDialog(null, funFacts[index], "Fun Facts!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SillyGuitar");
        ScreenManager screenManager = new ScreenManager();
        //customCursor cur = new customCursor();
        
        //frame.setCursor(cur.cursor);
        frame.add(screenManager);
        frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }
}