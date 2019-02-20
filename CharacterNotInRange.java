package enigma;
import static enigma.EnigmaException.*;

/** Create Character not in range.
 *   @author Khang Nguyen
 */
class CharacterNotInRange extends Alphabet {
    /** An alphabet consisting of all characters in String, inclusive. */
    /**
     * @param str this is string input
     */
    CharacterNotInRange(String str) {
        _range = str;
    }

    @Override
    int size() {
        return _range.length();
    }

    @Override
    boolean contains(char ch) {
        for (int i = 0; i < _range.length(); i++) {
            if (_range.charAt(i) == ch) {
                return true;
            }
        }
        return false;
    }

    @Override
    char toChar(int index) {
        return (_range.charAt(index));
    }

    @Override
    int toInt(char ch) {
        for (int i = 0; i < _range.length(); i++) {
            if (ch == _range.charAt(i)) {
                return i;
            }
        }
        throw new EnigmaException("character out of range");
    }

    /**Member variable.*/
    private String _range;
}
