import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
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
            add(piSequence.label);

            JButton continueBtn = new JButton("Continue");
            // Listeners learnt in lecture
            continueBtn.addActionListener(e -> popupManager.triggerRandomPopup());
            add(continueBtn);
        }
    }

    public static class StringPanel extends JPanel {
        SoundEngine soundEngine;
        PopupManager popupManager;

        // AI - Start
        // Horizontal fret lines (x‑positions)
        private final int[] frets = { 150, 250, 350, 450, 550, 650 };
        private final int[] yPositions = { 50, 100, 150, 200, 250, 300, 350 };
        private final double[] frequencies = { 82.0, 110.0, 147.0, 196.0, 247.0, 330.0, 67.0 };

        public StringPanel() {
            popupManager = new PopupManager();
            soundEngine = new SoundEngine();

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
                            new Thread(() -> {
                                try {
                                    double base = frequencies[idx];

                                    // Increase pitch by semitones based on fret
                                    double noteFreq = (fIdx >= 0)
                                            ? base * Math.pow(2, fIdx / 12.0)
                                            : base;
                                    soundEngine.playKarplusStrong(noteFreq, 1.5);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }).start();
                        }
                    }
                }
            });

            // keyboard input for playing the guitar using Key Bindings
            InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = getActionMap();

            Action EAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread(() -> {
                        try {
                            soundEngine.playKarplusStrong(frequencies[0], 1.5);
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
                            soundEngine.playKarplusStrong(frequencies[1], 1.5);
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
                            soundEngine.playKarplusStrong(frequencies[2], 1.5);
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
                            soundEngine.playKarplusStrong(frequencies[3], 1.5);
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
                            soundEngine.playKarplusStrong(frequencies[4], 1.5);
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
                            soundEngine.playKarplusStrong(frequencies[5], 1.5);
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
                            soundEngine.playKarplusStrong(frequencies[6], 1.5);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
            };

            inputMap.put(KeyStroke.getKeyStroke("1"), "EAction");
            inputMap.put(KeyStroke.getKeyStroke("2"), "BAction");
            inputMap.put(KeyStroke.getKeyStroke("3"), "GAction");
            inputMap.put(KeyStroke.getKeyStroke("4"), "DAction");
            inputMap.put(KeyStroke.getKeyStroke("5"), "AAction");
            inputMap.put(KeyStroke.getKeyStroke("6"), "eAction");
            inputMap.put(KeyStroke.getKeyStroke("7"), "ghostAction");

            actionMap.put("EAction", EAction);
            actionMap.put("BAction", BAction);
            actionMap.put("GAction", GAction);
            actionMap.put("DAction", DAction);
            actionMap.put("AAction", AAction);
            actionMap.put("eAction", eAction);
            actionMap.put("ghostAction", ghostAction);
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

        // AI - Start (But I put this code into this class.)
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

    public static class customCursor {
        Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon("guitarPick.png").getImage(),
                new Point(0, 0), "Custom cursor");
    }

    // class that takes input from the user and displays the changed volume
    public static class PiSequence extends JTextField {
        JLabel label;

        PiSequence() {
            label = new JLabel("100%");
            setText("Enter PI Digits...");
            setPreferredSize(new Dimension(250, 40));
            addActionListener(e -> {
                String input = getText();
                label.setText(Volume(input) + "%");
                setText("Enter PI Digits...");
                transferFocus();
            });
        }

        int Volume(String input) {
            String pi = "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706";
            int inputLength = Math.min(input.length(), pi.length());
            String truePi = pi.substring(0, inputLength);

            if (truePi.equals(input)) {
                return (pi.length() - inputLength);
            }

            return 100;
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
            int index = random.nextInt(funFacts.length);

            JOptionPane.showMessageDialog(null, funFacts[index], "Fun Facts!", JOptionPane.ERROR_MESSAGE);
        }

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SillyGuitar");
        ScreenManager screenManager = new ScreenManager();
        customCursor cur = new customCursor();

        //frame.setCursor(cur.cursor);
        frame.add(screenManager);
        frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }
}
