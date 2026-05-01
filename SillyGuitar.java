import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
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

    // Splash screen.
    public static class SplashScreen extends JPanel {
        ScreenManager screenManager;

        SplashScreen(ScreenManager screenManager) {
            this.screenManager = screenManager;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Color.WHITE);

            // Fonts & colors
            Font titleFont = new Font("SF Pro Display", Font.BOLD, 36);
            Font bodyFont = new Font("SF Pro Text", Font.PLAIN, 17);
            Color bodyColor = new Color(99, 99, 102);

            // Title
            JLabel title = new JLabel("Welcome to Silly Guitar!");
            title.setFont(titleFont);
            title.setForeground(new Color(29, 29, 31));

            // Instructions
            String[] instructions = {
                    "• Press keys 1-6 to play strings",
                    "• Reduce the volume by 1% by entering the digits of π (3.14159…)",
                    "• Change tuning at the bottom"
            };

            JPanel textBlock = new JPanel();
            textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
            textBlock.setAlignmentX(CENTER_ALIGNMENT);
            textBlock.setBackground(Color.WHITE);
            textBlock.add(title);
            textBlock.add(Box.createRigidArea(new Dimension(0, 10)));

            for (String text : instructions) {
                JLabel label = new JLabel(text);
                label.setFont(bodyFont);
                label.setForeground(bodyColor);
                textBlock.add(label);
            }

            // Warning
            JLabel warning = new JLabel("Warning: Random fact popups and random notes will play!");
            warning.setFont(new Font("Arial", Font.BOLD, 14));
            warning.setForeground(Color.RED);
            warning.setAlignmentX(CENTER_ALIGNMENT);

            // Continue button
            JButton continueBtn = new JButton("Continue");
            continueBtn.setAlignmentX(CENTER_ALIGNMENT);
            continueBtn.addActionListener(e -> screenManager.switchTo(GameState.INSTRUMENT));
            continueBtn.setBackground(new Color(0, 122, 255));
            continueBtn.setForeground(Color.WHITE);
            continueBtn.setFont(new Font("SF Pro Text", Font.BOLD, 15));
            continueBtn.setBorderPainted(false);
            continueBtn.setFocusPainted(false);
            continueBtn.setOpaque(true);
            continueBtn.setPreferredSize(new Dimension(160, 44));

            // Layout
            add(Box.createVerticalGlue());
            add(textBlock);
            add(Box.createVerticalGlue());
            add(warning);
            add(Box.createRigidArea(new Dimension(0, 20)));
            add(continueBtn);
            add(Box.createRigidArea(new Dimension(0, 10)));
        }
    }

    // Self explanatory: screen where the guitar can be played.
    public static class GuitarScreen extends JPanel {
        PopupManager popupManager;
        StringPanel stringPanel;
        PiSequence piSequence;

        GuitarScreen() {
            setLayout(new BorderLayout());
            setBackground(new Color(101, 67, 33));
            popupManager = new PopupManager();
            stringPanel = new StringPanel();
            piSequence = new PiSequence();

            // add(stringPanel);
            // add(piSequence);
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            rightPanel.setBackground(new Color(101, 67, 33));
            rightPanel.add(piSequence);

            JPanel eastWrapper = new JPanel(new BorderLayout());
            eastWrapper.setBackground(new Color(101, 67, 33));
            eastWrapper.add(rightPanel, BorderLayout.NORTH);
            add(eastWrapper, BorderLayout.EAST);
            add(stringPanel, BorderLayout.CENTER);

            TuningPanel tuningPanel = new TuningPanel(stringPanel);
            add(tuningPanel, BorderLayout.SOUTH);
        }
    }

    public static class StringPanel extends JPanel {
        // double[] frequencies = { 82.41, 110.0, 146.83, 196.0, 246.94, 329.63 };
        double[] frequencies = { 100.0, 140.0, 190.0, 240.0, 320.0, 430.0 };
        // AI - Start
        private final int[] yPositions = { 50, 100, 150, 200, 250, 300 };
        // Open chord voicings (standard tuning: E2, A2, D3, G3, B3, E4)
        private final double[] gMajorFreq = { 98.0, 123.47, 146.83, 196.0, 246.94, 392.0 };
        private final double[] cMajorFreq = { 130.81, 164.81, 196.0, 261.63, 329.63 };
        private final double[] dMajorFreq = { 146.83, 220.0, 293.66, 369.99 };
        private final double[] aMajorFreq = { 110.0, 164.81, 220.0, 277.18, 329.63 };
        private final double[] eMajorFreq = { 82.41, 123.47, 164.81, 207.65, 246.94, 329.63 };
        private final double[] aMinorFreq = { 110.0, 164.81, 196.0, 261.63, 329.63 };
        private final int[] frets = { 150, 250, 350, 450, 550, 650, 750 };

        PopupManager popupManager;
        Random random = new Random();
        private boolean ghostVisible = false;
        private int ghostY = 350;
        private double ghostFrequency;
        private javax.swing.Timer ghostTimer;
        private static int NOTE_DELAY_MS = 0;

        public StringPanel() {
            popupManager = new PopupManager();
            NOTE_DELAY_MS = random.nextInt(200, 600);
            setPreferredSize(new Dimension(800, 400));
            setBackground(new Color(101, 67, 33));

            // Start with a shorter base delay so the ghost appears more often
            ghostTimer = new javax.swing.Timer(300 + random.nextInt(700), null);
            ghostTimer.addActionListener(e -> {
                if (!ghostVisible) {
                    int gap = random.nextInt(yPositions.length - 1);
                    ghostY = (yPositions[gap] + yPositions[gap + 1]) / 2;
                    ghostFrequency = random.nextInt(100, 500);
                    ghostVisible = true;
                    repaint();
                    new Thread(() -> {
                        try {
                            Thread.sleep(NOTE_DELAY_MS);
                            SillyGuitar.soundEngine.playNote(ghostFrequency);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                    // Keep the ghost visible for a shorter time so it can reappear sooner
                    javax.swing.Timer hideTimer = new javax.swing.Timer(800, ev -> {
                        ghostVisible = false;
                        repaint();
                    });
                    hideTimer.setRepeats(false);
                    hideTimer.start();
                }
                // Use a small-but-not-zero random delay between appearances
                ghostTimer.setDelay(200 + random.nextInt(400));
            });
            ghostTimer.start();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    for (int i = 0; i < yPositions.length; i++) {
                        final int idx = i; // ✅ Make a final copy for the thread/lambda // HAD TO GIVE ERROR FOR THIS
                                           // LINE
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
                                    SillyGuitar.soundEngine.playNote(noteFreq);
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
                            Thread.sleep(NOTE_DELAY_MS);
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
                            Thread.sleep(NOTE_DELAY_MS);
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
                            Thread.sleep(NOTE_DELAY_MS);
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
                            Thread.sleep(NOTE_DELAY_MS);
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
                            Thread.sleep(NOTE_DELAY_MS);
                            SillyGuitar.soundEngine.playNote(frequencies[5]);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
            };

            Action GMajorAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SillyGuitar.soundEngine.playNote(gMajorFreq[0]);
                    SillyGuitar.soundEngine.playNote(gMajorFreq[1]);
                    SillyGuitar.soundEngine.playNote(gMajorFreq[2]);
                    SillyGuitar.soundEngine.playNote(gMajorFreq[3]);
                    SillyGuitar.soundEngine.playNote(gMajorFreq[4]);
                    SillyGuitar.soundEngine.playNote(gMajorFreq[5]);
                }
            };

            Action CMajorAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SillyGuitar.soundEngine.playNote(cMajorFreq[0]);
                    SillyGuitar.soundEngine.playNote(cMajorFreq[1]);
                    SillyGuitar.soundEngine.playNote(cMajorFreq[2]);
                    SillyGuitar.soundEngine.playNote(cMajorFreq[3]);
                    SillyGuitar.soundEngine.playNote(cMajorFreq[4]);
                }
            };

            Action DMajorAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SillyGuitar.soundEngine.playNote(dMajorFreq[0]);
                    SillyGuitar.soundEngine.playNote(dMajorFreq[1]);
                    SillyGuitar.soundEngine.playNote(dMajorFreq[2]);
                    SillyGuitar.soundEngine.playNote(dMajorFreq[3]);
                }
            };

            Action AMajorAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SillyGuitar.soundEngine.playNote(aMajorFreq[0]);
                    SillyGuitar.soundEngine.playNote(aMajorFreq[1]);
                    SillyGuitar.soundEngine.playNote(aMajorFreq[2]);
                    SillyGuitar.soundEngine.playNote(aMajorFreq[3]);
                    SillyGuitar.soundEngine.playNote(aMajorFreq[4]);
                }
            };

            Action EMajorAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SillyGuitar.soundEngine.playNote(eMajorFreq[0]);
                    SillyGuitar.soundEngine.playNote(eMajorFreq[1]);
                    SillyGuitar.soundEngine.playNote(eMajorFreq[2]);
                    SillyGuitar.soundEngine.playNote(eMajorFreq[3]);
                    SillyGuitar.soundEngine.playNote(eMajorFreq[4]);
                    SillyGuitar.soundEngine.playNote(eMajorFreq[5]);
                }
            };

            Action AMinorAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SillyGuitar.soundEngine.playNote(aMinorFreq[0]);
                    SillyGuitar.soundEngine.playNote(aMinorFreq[1]);
                    SillyGuitar.soundEngine.playNote(aMinorFreq[2]);
                    SillyGuitar.soundEngine.playNote(aMinorFreq[3]);
                    SillyGuitar.soundEngine.playNote(aMinorFreq[4]);
                }
            };

            inputMap.put(KeyStroke.getKeyStroke("2"), "EAction");
            inputMap.put(KeyStroke.getKeyStroke("4"), "BAction");
            inputMap.put(KeyStroke.getKeyStroke("6"), "GAction");
            inputMap.put(KeyStroke.getKeyStroke("3"), "DAction");
            inputMap.put(KeyStroke.getKeyStroke("5"), "AAction");
            inputMap.put(KeyStroke.getKeyStroke("1"), "eAction");
            inputMap.put(KeyStroke.getKeyStroke("G"), "GMajorAction");
            inputMap.put(KeyStroke.getKeyStroke("C"), "CMajorAction");
            inputMap.put(KeyStroke.getKeyStroke("D"), "DMajorAction");
            inputMap.put(KeyStroke.getKeyStroke("A"), "AMajorAction");
            inputMap.put(KeyStroke.getKeyStroke("E"), "EMajorAction");
            inputMap.put(KeyStroke.getKeyStroke("M"), "AMinorAction");

            actionMap.put("EAction", EAction);
            actionMap.put("BAction", BAction);
            actionMap.put("GAction", GAction);
            actionMap.put("DAction", DAction);
            actionMap.put("AAction", AAction);
            actionMap.put("eAction", eAction);
            actionMap.put("GMajorAction", GMajorAction);
            actionMap.put("CMajorAction", CMajorAction);
            actionMap.put("DMajorAction", DMajorAction);
            actionMap.put("AMajorAction", AMajorAction);
            actionMap.put("EMajorAction", EMajorAction);
            actionMap.put("AMinorAction", AMinorAction);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g; // Not AI

            g2d.setColor(new Color(218, 165, 32));
            g2d.setStroke(new BasicStroke(3)); // Not AI

            for (int y : yPositions) {
                g2d.drawLine(50, y, 750, y);
            }

            g2d.setColor(new Color(192, 192, 192));
            for (int x : frets) {
                g2d.drawLine(x, yPositions[0] - 20, x, yPositions[yPositions.length - 1] + 20);
            }
            if (ghostVisible) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine(50, ghostY, 750, ghostY);
            }

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            for (int i = 0; i < yPositions.length; i++) {
                g2d.drawString(TuningPanel.STRING_NUMS[random.nextInt(TuningPanel.STRING_NAMES.length)], 10,
                        yPositions[i] + 5); // +5 to vertically centre with the line
            }

        }

    } // AI - Finish

    public static class TuningPanel extends JPanel {
        private final JTextField[] tuningFields = new JTextField[6];

        // The intentionally wrong tuning the Reset button returns to
        private static final double[] ORIGINAL_WRONG_TUNING = { 100.0, 140.0, 190.0, 240.0, 320.0, 430.0 };
        static final String[] STRING_NAMES = { "E2", "B2", "G3", "D3", "A4", "e4" };
        static final String[] STRING_NUMS = { "1", "2", "3", "4", "5", "6" };

        TuningPanel(StringPanel stringPanel) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
            setBackground(new Color(101, 67, 33));
            setBorder(BorderFactory.createTitledBorder("Tuning"));

            // Tuning pegs: one label + text field per string (text fields do nothing)
            for (int i = 0; i < 6; i++) {
                JLabel lbl = new JLabel(STRING_NAMES[i] + ":");
                lbl.setForeground(Color.WHITE);
                add(lbl);
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

            // Fix tuning button
            JButton fixBtn = new JButton("Fix Tuning");
            fixBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    SillyGuitar.soundEngine.playHoverSound();
                }
            });
            fixBtn.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    JRootPane root = SwingUtilities.getRootPane(fixBtn);
                    if (root == null)
                        return;
                    JPanel glass = (JPanel) root.getGlassPane();
                    glass.setLayout(null);
                    glass.setVisible(true);

                    if (fixBtn.getParent() != glass) {
                        fixBtn.getParent().remove(fixBtn);
                        glass.add(fixBtn);
                    }

                    Random r = new Random();
                    int newX = r.nextInt(Math.max(1, root.getWidth() - fixBtn.getWidth()));
                    int newY = r.nextInt(Math.max(1, root.getHeight() - fixBtn.getHeight()));
                    fixBtn.setBounds(newX, newY, fixBtn.getPreferredSize().width, fixBtn.getPreferredSize().height);
                }
            });

            add(randomBtn);
            add(resetBtn);
            add(fixBtn);
        }

    }

    public static class SoundEngine {

        private static final float SAMPLE_RATE = 44100f;
        private static final int BUFFER_SIZE = 512;
        private static final double MASTER_GAIN = 0.2; // prevents clipping

        private SourceDataLine line;
        private volatile boolean running = true;
        FloatControl vol;
        float volume = 0;
        PopupManager popupManager;

        // Active plucked strings
        private final java.util.List<KarplusString> strings = new java.util.concurrent.CopyOnWriteArrayList<>();

        public SoundEngine() {
            popupManager = new PopupManager();

            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
                line = AudioSystem.getSourceDataLine(format);
                line.open(format, BUFFER_SIZE * 2);
                line.start();

                vol = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);// using AI
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
            if (strings.size() < 50) {
                strings.add(new KarplusString(frequency));
            }
            popupManager.triggerRandomPopup();
        }

        public void playHoverSound() {
            try {
                java.net.URL soundUrl = this.getClass().getResource("goofy_sfx.wav");
                AudioInputStream audioInputStream;

                if (soundUrl != null) {
                    audioInputStream = AudioSystem.getAudioInputStream(soundUrl);
                } else {
                    audioInputStream = AudioSystem.getAudioInputStream(new File("goofy_sfx.wav"));
                }

                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception ex) {
                ex.printStackTrace();
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
        Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon("guitarPick.png").getImage(),
                new Point(0, 0), "Custom cursor");
    }

    public static class PiSequence extends JPanel {
        PiSequence() {
            setBackground(new Color(101, 67, 33));
            JTextField textField = new JTextField("Enter PI Digits...");
            JLabel label = new JLabel("100%");
            textField.setPreferredSize(new Dimension(250, 40));

            textField.addActionListener(e -> {
                String input = textField.getText();// using AI
                label.setText((100 - volumeCalc(input)) + "%");
                textField.setText("Enter PI Digits...");
                textField.transferFocus();// using AI
            });
            
            add(new JLabel("Volume: "));
            add(textField);
            add(label);
        }

        int volumeCalc(String input) {
            String pi = "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706";
            int inputLength = Math.min(input.length(), pi.length());
            String truePi = pi.substring(0, inputLength);

            if (truePi.equals(input)) {
                int volume = inputLength;
                soundEngine.volume = volume;
                soundEngine.updateVolume();
                return volume;
            } else {
                int volume = 0;
                soundEngine.volume = volume;
                soundEngine.updateVolume();
                return volume;
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
        customCursor cur = new customCursor();

        frame.setCursor(cur.cursor);
        frame.add(screenManager);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }
}