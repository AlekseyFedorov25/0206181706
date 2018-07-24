import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Main extends JComponent {
    boolean midi; // Are we playing a midi file or a sampled one?

    Sequence sequence; // The contents of a MIDI file
    Sequencer sequencer; // We play MIDI Sequences with a Sequencer
    boolean playing = false; // whether the sound is current playing

    // Length and position of the sound are measured in milliseconds for
    // sampled sounds and MIDI "ticks" for MIDI sounds
    int audioLength; // Length of the sound.
    int audioPosition = 0; // Current position within the sound

    // The following fields are for the GUI
    JButton play; // The Play/Stop button
    JSlider progress; // Shows and sets current position in sound
    JLabel time; // Displays audioPosition as a number
    Timer timer; // Updates slider every 100 milliseconds

    // The main method just creates an SoundPlayer in a Frame and displays it
    public static void main(String[] args) throws IOException, MidiUnavailableException, InvalidMidiDataException {
        Main player;

        File file = new File("Believer.mid"); // This is the file we'll be playing
        // Determine whether it is midi or sampled audio
        boolean ismidi;
        try {
            // We discard the return value of this method; we just need to know
            // whether it returns successfully or throws an exception
            MidiSystem.getMidiFileFormat(file);
            ismidi = true;
        } catch (InvalidMidiDataException e) {
            ismidi = false;
        }

        // Create a SoundPlayer object to play the sound.
        player = new Main(file, ismidi);

        // Put it in a window and play it
        JFrame f = new JFrame("SoundPlayer");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(player, "Center");
        f.pack();
        f.setVisible(true);
    }

    // Create an SoundPlayer component for the specified file.
    public Main(File f, boolean isMidi) throws IOException, MidiUnavailableException, InvalidMidiDataException {
        if (isMidi) { // The file is a MIDI file
            midi = true;
            // First, get a Sequencer to play sequences of MIDI events
            // That is, to send events to a Synthesizer at the right time.
            sequencer = MidiSystem.getSequencer(); // Used to play sequences
            sequencer.open(); // Turn it on.

            // Get a Synthesizer for the Sequencer to send notes to
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open(); // acquire whatever resources it needs

            // The Sequencer obtained above may be connected to a Synthesizer
            // by default, or it may not. Therefore, we explicitly connect it.
            Transmitter transmitter = sequencer.getTransmitter();
            Receiver receiver = synth.getReceiver();
            transmitter.setReceiver(receiver);

            // Read the sequence from the file and tell the sequencer about it
            sequence = MidiSystem.getSequence(f);
            sequencer.setSequence(sequence);
            audioLength = (int) sequence.getTickLength(); // Get sequence length
        }

        // Now create the basic GUI
        play = new JButton("Play"); // Play/stop button
        progress = new JSlider(0, audioLength, 0); // Shows position in sound
        time = new JLabel("0"); // Shows position as a #

        // When clicked, start or stop playing the sound
        play.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (playing)
                    stop();
                else
                    play();
            }
        });

        // Whenever the slider value changes, first update the time label.
        // Next, if we're not already at the new position, skip to it.
        progress.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int value = progress.getValue();
                // Update the time label
                if (midi)
                    time.setText(value + "");
                // If we're not already there, skip there.
                if (value != audioPosition)
                    skip(value);
            }
        });

        // This timer calls the tick() method 10 times a second to keep
        // our slider in sync with the music.
        timer = new javax.swing.Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tick();
            }
        });

        // put those controls in a row
        Box row = Box.createHorizontalBox();
        row.add(play);
        row.add(progress);
        row.add(time);

        // And add them to this component.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(row);

        // Now add additional controls based on the type of the sound

    }

    /** Start playing the sound at the current position */
    public void play() {
        if (midi)
            sequencer.start();

        timer.start();
        play.setText("Stop");
        playing = true;
    }

    /** Stop playing the sound, but retain the current position */
    public void stop() {
        timer.stop();
        if (midi)
            sequencer.stop();

        play.setText("Play");
        playing = false;
    }

    /** Stop playing the sound and reset the position to 0 */
    public void reset() {
        stop();
        if (midi)
            sequencer.setTickPosition(0);

        audioPosition = 0;
        progress.setValue(0);
    }

    /** Skip to the specified position */
    public void skip(int position) { // Called when user drags the slider
        if (position < 0 || position > audioLength)
            return;
        audioPosition = position;
        if (midi)
            sequencer.setTickPosition(position);

        progress.setValue(position); // in case skip() is called from outside
    }

    /** Return the length of the sound in ms or ticks */
    public int getLength() {
        return audioLength;
    }

    // An internal method that updates the progress bar.
    // The Timer object calls it 10 times a second.
    // If the sound has finished, it resets to the beginning
    void tick() {
        if (midi && sequencer.isRunning()) {
            audioPosition = (int) sequencer.getTickPosition();
            progress.setValue(audioPosition);
        }  else
            reset();
    }

}