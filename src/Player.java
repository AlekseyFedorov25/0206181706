import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.MidiMessage;

public class Player implements Receiver {

    public void send(MidiMessage message, long timeStamp) {
        if (message.getStatus() > 127 && message.getStatus() < 144) { // Note OFF
            byte pin = (byte) (2 * (message.getStatus() - 127));

            System.out.println("Got note OFF on pin: " + (pin & 0xFF));
        } else if (message.getStatus() > 143 && message.getStatus() < 160) { // Note ON
            byte pin = (byte) (2 * (message.getStatus() - 143));
            System.out.println("Got note ON on pin: " + (pin & 0xFF) + " with period " + (message.getMessage()[1] & 0xff));
            System.out.println(message.getLength() + " " + message.getMessage()[message.getLength()-1]);

            if (message.getMessage()[2] == 0) {
                System.out.printf("Zero velocity\n");
                System.out.println();
            } else {
                System.out.printf("%s || %s || %s \n", pin,(message.getMessage()[1] & 0xff), message.getStatus() - 144);
                System.out.println();
            }
        } else if (message.getStatus() > 223 && message.getStatus() < 240) {}
    }

    public void close() {

    }
}
