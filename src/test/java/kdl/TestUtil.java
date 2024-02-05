package kdl;

import kdl.parse.KDLParseContext;
import kdl.parse.KDLParser;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.IOException;
import java.io.StringReader;

@SuppressWarnings("rawtypes")
public class TestUtil {
    public static final KDLParser parser = new KDLParser();

    public static KDLParseContext strToContext(String str) {
        final StringReader reader = new StringReader(str);
        return new KDLParseContext(reader);
    }

    public static String readRemainder(KDLParseContext context) {
        final StringBuilder stringBuilder = new StringBuilder();
        try {
            int read = context.read();
            while (read != KDLParser.EOF) {
                stringBuilder.appendCodePoint(read);
                read = context.read();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stringBuilder.toString();
    }

    public static Matcher<Runnable> throwsException(Class exceptionClass) {
      return new ThrowsExceptionMatcher(exceptionClass);
    }
}

@SuppressWarnings("rawtypes")
class ThrowsExceptionMatcher extends TypeSafeMatcher<Runnable> {

  private final Class exceptionClass;
  private Exception actualException;

  public ThrowsExceptionMatcher(Class exceptionClass) {
    this.exceptionClass = exceptionClass;
  }

  @Override
  protected boolean matchesSafely(Runnable fn) {
    try {
      fn.run();
      return false;
    } catch (Exception e) {
      actualException = e;
      return e.getClass() == exceptionClass;
    }
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("throws exception ");
    description.appendValue(exceptionClass.toString());
  }

  @Override
  protected void describeMismatchSafely(Runnable item, Description description) {
    if (actualException != null) {
      description.appendText("threw exception ");
      description.appendValue(actualException);
    } else {
      description.appendText("threw no exception");
    }
  }
}
