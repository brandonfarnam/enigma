package enigma;

import java.util.Collection;
import java.util.ArrayList;


import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Khang Nguyen
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _pawls = pawls;
        _allRotors = allRotors;
        _numRotors = numRotors;
        _rotors = new ArrayList<>();
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _rotors.clear();
        if (rotors.length != _numRotors) {
            throw new EnigmaException("Misnamed rotors");
        } else {
            for (String str : rotors) {
                for (Rotor rotor : _allRotors) {
                    if (rotor.name().equals(str)) {
                        _rotors.add(rotor);
                    }
                }
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 upper-case letters. The first letter refers to the
     *  leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != _numRotors - 1) {
            throw new EnigmaException("Initial positions string wrong length");
        } else {
            for (int index = 0; index < setting.length(); index++) {
                if (!_alphabet.contains(setting.charAt(index))) {
                    throw new EnigmaException("Initial positions "
                            + "string not in alphabet");
                }
                _rotors.get(index + 1).set(setting.charAt(index));
            }
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing

     *  the machine. */
    int convert(int c) {
        for (int i = _numRotors - _pawls; i < _numRotors - 1; i++) {
            if ((_rotors.get(i).atNotch() && _rotors.get(i - 1).rotates()
                    || _rotors.get(i + 1).atNotch())) {
                _rotors.get(i).advance();
            }
        }
        _rotors.get(_numRotors - 1).advance();
        int input = c;
        if (_plugboard != null) {
            input = _plugboard.permute(c);
        }
        for (int index = _numRotors - 1; index >= 0; index--) {
            input = _rotors.get(index).convertForward(input);
        }
        for (int index = 1; index < _numRotors; index++) {
            input = _rotors.get(index).convertBackward(input);
        }
        int output = input;

        if (_plugboard != null) {
            output = _plugboard.permute(input);
        }
        return output;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String output = "";
        msg = msg.replaceAll("\\s", "");
        for (int i = 0; i < msg.length(); i++) {
            if (!_alphabet.contains(msg.charAt(i))) {
                output += msg.charAt(i);
            } else {
                output += _alphabet.toChar(convert
                        (_alphabet.toInt(msg.charAt(i))));
            }
        }
        return output;
    }
    /** Return Rotors.*/
    public ArrayList<Rotor> getRotor() {
        return _rotors;
    }
    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;
    /** Number of Rotors.*/
    private int _numRotors;
    /** Number of Pawl.*/
    private int _pawls;
    /** List of rotors.*/
    private Collection<Rotor> _allRotors;
    /** Permutation.*/
    private Permutation _plugboard;
    /** List of rotors used.*/
    private ArrayList<Rotor> _rotors;

}
