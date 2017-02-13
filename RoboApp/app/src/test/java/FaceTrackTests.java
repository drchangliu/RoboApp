import com.robodoot.dr.RoboApp.FdActivity;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class FaceTrackTests {

    @Test
    public void multiplicationOfZeroIntegersShouldReturnZero() {
        FdActivity tester = new FdActivity(); // MyClass is tested

        // assert statements
        assertThat(tester.emotionalReaction(0.05f), is(false));
        assertThat(tester.emotionalReaction(0.5f), is(false));
        assertThat(tester.emotionalReaction(0.8f), is(true));
    }
}

/*******************************************
 Results:
 I tested the function that provides the emotional response based upon a smile probability.
 3 cases tested:

 1: smile probability less than 0.1 should return false
 test successful

 2) smile probability less than 0.75 should also return false
 test successful

 3) smile probability greater than 0.75 should return true
 test successful

 I acheived 100% success, and ensured this callback function will return the correct result
 100% coverage and 100% pass rate acheived
********************************************/