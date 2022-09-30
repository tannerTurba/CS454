
public class IllegalFileFormatException extends Throwable {
    public IllegalFileFormatException() {
        System.err.println("An invalid file format was given. Please try again.");
    }

    public IllegalFileFormatException(String receivedFormat, String expectedFormat) {
        System.err.println(String.format("A format of type {0} was expected, but received {1}.", expectedFormat, receivedFormat));
    }

}
