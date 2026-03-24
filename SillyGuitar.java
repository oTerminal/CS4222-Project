import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SillyGuitar {

    // These are simply the screens of our game, basically what screen in our sketch we will be in.
    public static enum GameState {
        TUTORIAL,
        INSTRUMENT;
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
    public static class GuitarScreen extends JPanel{

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