import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MIDItools {

    // Aufgabe 1
    public static byte getNote(char note, int octave, boolean sharp) {
        int base;
        switch (Character.toUpperCase(note)) {
            case 'C': base = 0; break;
            case 'D': base = 2; break;
            case 'E': base = 4; break;
            case 'F': base = 5; break;
            case 'G': base = 7; break;
            case 'A': base = 9; break;
            case 'B': base = 11; break;
            default:
                return 0;
        }
        int semitones = base + (sharp ? 1 : 0);
        int midiNumber = (octave + 1) * 12 + semitones;
        if (midiNumber < 0 || midiNumber > 127) return 0;
        return (byte) midiNumber;
    }

    // Aufgabe 2
    public static byte[] getHeader(byte speed) {
        return new byte[] {
                0x4D, 0x54, 0x68, 0x64,    // "MThd"
                0x00, 0x00, 0x00, 0x06,    // Header-Länge = 6
                0x00, 0x00,                // Format 0
                0x00, 0x01,                // 1 Track
                0x00, speed                // Division = speed
        };
    }

    // Aufgabe 3
    public static byte[] getNoteEvent(byte delay, boolean noteOn, byte note, byte velocity) {
        byte status = (byte) ( (noteOn ? 0x90 : 0x80) /*| 0x0*/ );
        return new byte[] { delay, status, note, velocity };
    }

    // Aufgabe 4
    public static byte[] addNoteToTrack(byte[] trackdata, byte[] noteEvent) {
        byte[] result = new byte[trackdata.length + noteEvent.length];
        System.arraycopy(trackdata,  0, result, 0, trackdata.length);
        System.arraycopy(noteEvent,   0, result, trackdata.length, noteEvent.length);
        return result;
    }

    // Aufgabe 5 (korrigiert: Tempo-Bytes = 15, nicht 16)
    public static byte[] getTrack(byte instrument, byte[] trackdata) {
        byte[] tempoBytes = new byte[] {
                0x00, (byte)0xFF, 0x58, 0x04,
                0x04, 0x02,        0x18, 0x08,
                0x00, (byte)0xFF, 0x51, 0x03,
                0x07, (byte)0xA1, 0x20
        };
        int length = tempoBytes.length    // Tempo-/Meta-Daten
                + 2                   // 00 C0
                + 1                   // Instrument-Byte
                + trackdata.length;   // alle Note-Events

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // Chunk ID "MTrk"
            out.write(new byte[]{ 0x4D, 0x54, 0x72, 0x6B });
            // 3-Byte-Placeholder und 1-Byte Länge
            out.write(0x00);
            out.write(0x00);
            out.write(0x00);
            out.write((byte) length);
            // Tempo-/Meta-Daten
            out.write(tempoBytes);
            // Instrument setzen auf Channel 0
            out.write(new byte[]{ 0x00, (byte)0xC0 });
            out.write(instrument);
            // die Note-Events
            out.write(trackdata);
            // Track-Ende
            out.write(new byte[]{ (byte)0xFF, 0x2F, 0x00 });
            return out.toByteArray();
        } catch (IOException e) {
            // sollte mit ByteArrayOutputStream nie passieren
            return new byte[0];
        }
    }
}


