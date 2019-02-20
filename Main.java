package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.Set;


import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Khang Nguyen
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        _rotorNames = new HashSet<>();
        _movingRotorNames = new HashSet<>();
        Machine machine = readConfig();
        boolean isSetUp = false;

        while (_input.hasNextLine()) {
            String next = _input.nextLine();
            if (next.length() > 0) {
                if (!(next.substring(0, 1).equals("*"))) {
                    if (isSetUp) {
                        String out = machine.convert(next.toUpperCase());
                        printMessageLine(out);
                    } else {
                        throw new EnigmaException(
                                "The input might not "
                                        + "start with a setting");
                    }
                } else {
                    setUp(machine, next.substring(1));
                    isSetUp = true;
                }
            } else {
                printMessageLine("\n");
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alpha = _config.nextLine();
            if (alpha.contains("(") || alpha.contains(")")
                    || alpha.contains("*")) {
                throw new EnigmaException("Need Alphabet");
            }

            if (alpha.length() > 1 && alpha.charAt(1) == '-') {
                _alphabet = new CharacterRange(alpha.charAt(0),
                        alpha.charAt((alpha.length() - 1)));
            } else {
                _alphabet = new CharacterNotInRange(alpha);
            }
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Wrong confiq set up");
            }
            int totalRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Wrong confiq set up");
            }
            _totalMovingRotors = _config.nextInt();
            _config.nextLine();
            ArrayList<Rotor> rotors = new ArrayList<>();
            _buffer = _config.nextLine();
            while (_config.hasNextLine()) {
                String temp = _config.nextLine();
                Scanner strScanner = new Scanner(temp);
                while (strScanner.hasNext()
                        && strScanner.next().charAt(0) == '(') {
                    _buffer += " " + temp;
                    temp = "";
                    if (_config.hasNextLine()) {
                        temp = _config.nextLine();
                    }
                    strScanner = new Scanner(temp);
                }
                rotors.add(readRotor());
                _buffer = temp;
            }
            if (!_buffer.equals("")) {
                rotors.add(readRotor());
            }
            return new Machine(_alphabet, totalRotors,
                    _totalMovingRotors, rotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String temp = _buffer.toUpperCase();
            Scanner stringScanner = new Scanner(temp);
            String name = stringScanner.next();
            String rotorType = stringScanner.next();
            String cycle = stringScanner.nextLine();
            _buffer = "";
            _rotorNames.add(name);
            if (rotorType.charAt(0) == 'M') {
                String notch = rotorType.substring(1);
                _movingRotorNames.add(name);
                return new MovingRotor(
                        name, new Permutation(cycle, _alphabet), notch);
            } else if (rotorType.charAt(0) == 'N') {
                return new FixedRotor(name, new Permutation(cycle, _alphabet));
            } else if (rotorType.charAt(0) == 'R') {
                return new Reflector(name, new Permutation(cycle, _alphabet));
            } else {
                return null;
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description" + excp.toString());
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {

        Scanner stringScanner = new Scanner(settings);
        String next = "";
        boolean isReadingRotor = true;
        List<String> rotorNames = new ArrayList<>();
        String []setting = settings.split(" ");
        if (setting.length - 1 < M.numRotors()) {
            throw new EnigmaException("The setting line can "
                    + "contain the wrong number of arguments.");
        }

        String[] rotors = new String[M.numRotors()];
        for (int i = 1; i < M.numRotors() + 1; i++) {
            rotors[i - 1] = setting[i];
        }

        for (int i = 0; i < rotors.length - 1; i++) {
            for (int j = i + 1; j < rotors.length; j++) {
                if (rotors[i] == rotors[j]) {
                    throw new EnigmaException("A rotor might be "
                            + "repeated in the setting line.");
                }
            }
        }

        int numRotors = 0;
        int numMovingRotors = 0;
        while (stringScanner.hasNext() && isReadingRotor) {
            next = stringScanner.next();
            if (_rotorNames.contains(next)) {
                rotorNames.add(next);
                numRotors++;
                if (_movingRotorNames.contains(next)) {
                    numMovingRotors++;
                }
            } else {
                isReadingRotor = false;
            }
        }
        if (numMovingRotors == M.numPawls() && numRotors == M.numRotors()) {
            M.insertRotors(rotorNames.toArray(new String[rotorNames.size()]));
        } else {
            throw new EnigmaException("Wrong number of arugments");
        }

        if (!M.getRotor().get(0).reflecting()) {
            throw new EnigmaException("First Rotor should be a reflector");
        }

        M.setRotors(next);
        if (stringScanner.hasNextLine()) {
            M.setPlugboard(new Permutation(
                    stringScanner.nextLine(), _alphabet));
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 0; i < msg.length(); i += 1) {
            if (i % 6 == 0) {
                msg = msg.substring(0, i) + " "
                        + msg.substring(i, msg.length());
            }
        }
        String output = msg.trim();
        _output.println(output);
    }
    /** Alphabet used in this machine. */
    private Alphabet _alphabet;
    /** Source of input messages.*/
    private Scanner _input;
    /** Source of machine configuration.*/
    private Scanner _config;
    /** File for encoded/decoded messages.*/
    private PrintStream _output;
    /** An Collection contains all rotors.*/
    private Set<String> _rotorNames;
    /**Javadoc.*/
    private Set<String> _movingRotorNames;
    /** Javadoc.*/
    private String _buffer;
    /**Javadoc.*/
    private int _totalMovingRotors;
}
