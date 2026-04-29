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
            setLayout(new BorderLayout());
            popupManager = new PopupManager();
            stringPanel = new StringPanel();
            piSequence = new PiSequence();

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            rightPanel.add(piSequence);
            rightPanel.add(piSequence.label);

            JPanel eastWrapper = new JPanel(new BorderLayout());
            eastWrapper.add(rightPanel, BorderLayout.NORTH);
            add(eastWrapper, BorderLayout.EAST);
            add(stringPanel, BorderLayout.CENTER);

            TuningPanel tuningPanel = new TuningPanel(stringPanel);
            add(tuningPanel, BorderLayout.SOUTH);
        }
    }

    public static class StringPanel extends JPanel {
        SoundEngine soundEngine;
        double[]frequencies = {82.0, 110.0, 147.0, 196.0, 247.0, 330.0};
        //AI - Start
        private final int[] yPositions = {50, 100, 150, 200, 250, 300};
        private final double[] gMajorFreq = {196.00, 246.94, 392.00, 493.88, 587.33, 783.99};
        private final int[] frets = {150, 250, 350, 450, 550, 650, 750};
        
        PopupManager popupManager;
        Random random = new Random();
        private boolean ghostVisible = false;
        private int ghostY = 350;
        private double ghostFrequency = 300;
        private javax.swing.Timer ghostTimer;
        private static final int NOTE_DELAY_MS = 150;

        public StringPanel() {
            popupManager = new PopupManager();
            soundEngine = new SoundEngine();
            setPreferredSize(new Dimension(800, 400));

            ghostTimer = new javax.swing.Timer(1000 + random.nextInt(1000), null);
            ghostTimer.addActionListener(e -> {
            if (!ghostVisible) {
            int gap = random.nextInt(yPositions.length -1); 
            ghostY = (yPositions[gap] + yPositions[gap + 1]) / 2; 
            ghostFrequency = 80 + random.nextInt(500);
            ghostVisible = true;
            repaint();
            new Thread(() -> {
                try {
                    Thread.sleep(NOTE_DELAY_MS);
                    soundEngine.playNote(ghostFrequency);
                } catch (Exception ex) { ex.printStackTrace(); }
            }).start();
            javax.swing.Timer hideTimer = new javax.swing.Timer(2000, ev -> {
            ghostVisible = false;
            repaint();
            });
            hideTimer.setRepeats(false);
            hideTimer.start();
            }
            ghostTimer.setDelay(random.nextInt(750));
            });
            ghostTimer.start();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    for (int i = 0; i < yPositions.length; i++) {
                        final int idx = i;   // ✅ Make a final copy for the thread/lambda // HAD TO GIVE ERROR FOR THIS LINE
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

                            new Thread(() -> {
                                try {
                                    Thread.sleep(NOTE_DELAY_MS);
                                    soundEngine.playNote(noteFreq);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }).start();

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
                            Thread.sleep(NOTE_DELAY_MS);
                            soundEngine.playNote(frequencies[0]);
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
                            Thread.sleep(NOTE_DELAY_MS);
                            soundEngine.playNote(frequencies[1]);
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
                            Thread.sleep(NOTE_DELAY_MS);
                            soundEngine.playNote(frequencies[2]);
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
                            Thread.sleep(NOTE_DELAY_MS);
                            soundEngine.playNote(frequencies[3]);
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
                            Thread.sleep(NOTE_DELAY_MS);
                            soundEngine.playNote(frequencies[4]);
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
                            soundEngine.playNote(frequencies[5]);
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
            inputMap.put(KeyStroke.getKeyStroke("G"), "GMajorAction");

            actionMap.put("EAction", EAction);
            actionMap.put("BAction", BAction);
            actionMap.put("GAction", GAction);
            actionMap.put("DAction", DAction);
            actionMap.put("AAction", AAction);
            actionMap.put("eAction", eAction);
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
            if (ghostVisible) {
                g2d.setColor(Color.RED); 
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine(50, ghostY, 750, ghostY);
            }
        }

    } // AI - Finish

    public static class TuningPanel extends JPanel {
        private final StringPanel stringPanel;
        private final JTextField[] tuningFields = new JTextField[6];

        // The intentionally wrong tuning the Reset button returns to
        private static final double[] ORIGINAL_WRONG_TUNING = {100.0, 140.0, 190.0, 240.0, 320.0, 430.0};
        private static final String[] STRING_NAMES = {"E2", "B2", "G3", "D3", "A4", "e4"};

        TuningPanel(StringPanel stringPanel) {
            this.stringPanel = stringPanel;
            setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
            setBorder(BorderFactory.createTitledBorder("Tuning"));

            // Tuning pegs: one label + text field per string (text fields do nothing)
            for (int i = 0; i < 6; i++) {
                add(new JLabel(STRING_NAMES[i] + ":"));
                JTextField field = new JTextField(5);
                field.setText(String.format("%.1f", stringPanel.frequencies[i]));
                tuningFields[i] = field;
                add(field);
            }

            // Randomise tuning button
            JButton randomBtn = new JButton("Randomise Tuning");
            randomBtn.addActionListener(e -> {
                Random r = new Random();
                for (int i = 0; i < stringPanel.frequencies.length; i++) {
                    stringPanel.frequencies[i] = 60 + r.nextDouble() * 600;
                    tuningFields[i].setText(String.format("%.1f", stringPanel.frequencies[i]));
                }
            });

            // Reset to original wrong tuning button
            JButton resetBtn = new JButton("Reset Tuning (Original)");
            resetBtn.addActionListener(e -> {
                for (int i = 0; i < stringPanel.frequencies.length; i++) {
                    stringPanel.frequencies[i] = ORIGINAL_WRONG_TUNING[i];
                    tuningFields[i].setText(String.format("%.1f", ORIGINAL_WRONG_TUNING[i]));
                }
            });

            add(randomBtn);
            add(resetBtn);
        }

    }

    public static class GoofyButton extends JButton {

    }

    public static class SoundEngine {

        PopupManager popupManager = new PopupManager();

        private static final float SAMPLE_RATE = 44100f;
        private static final int BUFFER_SIZE = 512;
        private static final double MASTER_GAIN = 0.2; // prevents clipping

        private SourceDataLine line;
        private volatile boolean running = true;

        // Active plucked strings
        private final java.util.List<KarplusString> strings = new java.util.concurrent.CopyOnWriteArrayList<>();

        public SoundEngine() {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
                line = AudioSystem.getSourceDataLine(format);
                line.open(format, BUFFER_SIZE * 2);
                line.start();

                Thread audioThread = new Thread(this::audioLoop);
                audioThread.setDaemon(true);
                audioThread.start();

            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
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
            if (strings.size() < 100) {
                strings.add(new KarplusString(frequency));
            }
            // popupManager.triggerRandomPopup();

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

    public static class Cursor {

    }

    public static class PiSequence extends JTextField {
        JLabel label;

        PiSequence() {
            label = new JLabel("0%");
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
                return Math.abs(100 - (pi.length() - inputLength));
            }

            return Math.abs(0);
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

        frame.add(screenManager);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }
}