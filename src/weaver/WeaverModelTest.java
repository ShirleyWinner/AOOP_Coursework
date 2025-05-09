package weaver;

import org.junit.jupiter.api.*;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeaverModelTest {

    private WeaverModel model;   // fresh instance per test

    @BeforeEach
    void setUp() throws Exception {
        model = new WeaverModel(Paths.get("dictionary.txt"));
    }

    //Scenario 1  –  “Happy path”: player reaches the target
    /**
     * @requires model != null  ∧  model.isShowPath() == false
     * @ensures  history.size() ≥ 1
     * @ensures  history.getLast().equals(model.getTargetWord())
     * @ensures  evaluationHistory.size() == history.size()
     */
    @Test
    void testWinViaComputedPath() {
        model.setShowPath(true);               // enable BFS helper
        List<String> path = model.computePath();
        assertTrue(path.size() > 1, "Dictionary must contain a viable path");

        // replay the path (skip element 0 which is the start word itself)
        boolean win = false;
        for (int i = 1; i < path.size(); i++) {
            win = model.attempt(path.get(i));
        }
        /* post‑conditions */
        assertTrue(win, "last attempt must win");
        List<String> hist = model.getHistory();
        assertEquals(path.get(path.size() - 1), hist.get(hist.size() - 1));
        assertEquals(hist.size(), model.getEvaluationHistory().size());
    }

    //Scenario 2  –  Invalid intermediate word with flag on
    /**
     * @requires word.length() == 4  ∧  word ∉ dictionary
     * @ensures  IllegalArgumentException is thrown
     */
    @Test
    void testInvalidWordThrows() {
        model.setShowError(true);                  // make Model strict
        assertThrows(IllegalArgumentException.class,
                () -> model.attempt("zzzz")); // not in dictionary
    }

    //Scenario 3  –  Silent-ignore behaviour when showError=false
    /**
     * @requires model.setShowError(false)
     * @requires "zzzz" ∉ dictionary  ∧  "zzzz".length()==4
     * @ensures  model.getHistory().isEmpty()        // no state change
     * @ensures  model.getEvaluationHistory().isEmpty()
     * @ensures  attempt("zzzz") returns false
     */
    @Test
    void testInvalidWordSilentlyIgnored() {
        model.setShowError(false);                 // suppress exceptions

        boolean result = model.attempt("zzzz");    // not in dictionary
        assertFalse(result, "invalid word should not trigger a win");

        /* post-condition checks */
        assertTrue(model.getHistory().isEmpty(), "history must remain unchanged");
        assertTrue(model.getEvaluationHistory().isEmpty(), "no evaluations recorded");
    }
}
