package at.htlhl.chess.gui.util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public enum SoundEffect {
    MOVE("sounds/move.wav"),
    CAPTURE("sounds/capture.wav"),
    THE_ROOK("sounds/the_rook.wav");

    private final String resourcePath;

    SoundEffect(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * Retrieves the file path for the sound effect.
     *
     * @return The file path as a String.
     * @throws IllegalStateException if the resource cannot be found.
     */
    public String getSoundFilePath() {
        String fullPath = "/at/htlhl/chess/gui/" + resourcePath;
        URL resourceUrl = ChessBoardInteractionHandler.class.getResource(fullPath);
        if (resourceUrl == null)
            throw new IllegalStateException("Sound resource not found: " + fullPath);
        return resourceUrl.getPath();
    }

    /**
     * Plays the sound associated with this SoundEffect.
     */
    public void playSound() {
        try {
            String soundFilePath = getSoundFilePath();
            File soundFile = new File(soundFilePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, audioInputStream.getFormat()));
            clip.open(audioInputStream);
            clip.start();

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP)
                    clip.close();
            });

            audioInputStream.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}