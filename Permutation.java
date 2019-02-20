package enigma;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Khang Nguyen
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = new ArrayList<>();
        String input = cycles.trim();
        if (input.charAt(input.length() - 1) != ')') {
            throw new EnigmaException("Bad conf");
        }
        Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(input);
        while (m.find()) {
            _cycles.add(m.group(1));
        }
    }
    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        _cycles.add(cycle);
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {

        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char output = _alphabet.toChar(wrap(p));
        for (String s: _cycles) {
            for (int index = 0; index < s.length(); index++) {
                if (output == s.charAt(index)) {
                    char next = s.charAt((index + 1) % s.length());
                    return _alphabet.toInt(next);
                }
            }
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char output = _alphabet.toChar(wrap(c));
        for (String s: _cycles) {
            for (int index = 0; index < s.length(); index++) {
                if (output == s.charAt(index)) {
                    char next = s.charAt((index - 1 + s.length()) % s.length());
                    return _alphabet.toInt(next);
                }
            }
        }
        return c;
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int input = _alphabet.toInt(p);
        int newInput = permute(input);
        return _alphabet.toChar(newInput);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    int invert(char c) {
        int output = _alphabet.toInt(c);
        int newOutput = invert(output);
        return _alphabet.toChar(newOutput);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (String str : _cycles) {
            if (str.length() == 1) {
                return false;
            }
        }
        return true;

    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;
    /** Cycles. */
    private ArrayList<String> _cycles;
}
