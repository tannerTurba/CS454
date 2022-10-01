

public class IllegalFileFormatException extends Throwable {
    public IllegalFileFormatException() {
        System.err.println("An invalid file format was given. Please try again.");
    }

    public IllegalFileFormatException(String receivedFormat, String expectedFormat) {
        System.err.println("A format of type " + expectedFormat + " was expected, but received " + receivedFormat + ".");
    }

}
