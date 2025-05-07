package weaver;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 *   @invariant startWord != null  && targetWord != null
 *   @invariant startWord.length() == 4 && targetWord.length() == 4
 *   @invariant dictionary != null && !dictionary.isEmpty()
 *   @invariant (forall String w; history.contains(w); w.length() == 4)
 *   @invariant history.size() == evaluationHistory.size()
 * </pre>
 */
public final class WeaverModel extends Observable {
    //Coursework Flags (FR3)
    private boolean flagShowError = true;
    private boolean flagShowPath  = false;
    private boolean flagRandomise = false;

    /** Immutable list of ALL valid four‑letter English words. */
    private final List<String> dictionary;

    private String startWord;                 // current start node
    private String targetWord;                // goal node
    private final List<String> history = new ArrayList<>();           // attempts
    private final List<TileState[]> evaluationHistory = new ArrayList<>(); // colours

    /** Visual state enum for tile feedback. */
    public enum TileState { CORRECT, PRESENT, ABSENT, UNSET }


    /**
     * Construct a model and load dictionary.
     * <pre>
     *   @requires dictPath != null && Files.exists(dictPath)
     *   @ensures  getDictionary().size() > 0
     *   @ensures  startWord != null && targetWord != null
     * </pre>
     * @param dictPath path to <code>dictionary.txt</code>.
     */
    public WeaverModel(Path dictPath) throws IOException {
        Objects.requireNonNull(dictPath, "Dictionary path must not be null");
        // assert precondition at runtime in debug builds
        assert Files.exists(dictPath) : "Dictionary file must exist";

        dictionary = Files.lines(dictPath)
                .map(String::trim)
                .filter(w -> w.length() == 4)
                .map(String::toLowerCase)
                .collect(Collectors.toUnmodifiableList());

        assert !dictionary.isEmpty() : "Dictionary must contain at least one word";
        resetGame();
    }

    //Public API

    /** Read‑only access for tests. */
    public List<String> getDictionary() { return dictionary; }

    public void setShowError(boolean on) { flagShowError = on; }
    public void setShowPath (boolean on) { flagShowPath  = on; notifyChange(); }
    public void setRandomise(boolean on) { flagRandomise = on; }
    public boolean isShowPath() { return flagShowPath; }

    public String getStartWord() { return startWord; }
    public String getTargetWord(){ return targetWord; }
    public List<String> getHistory()            { return List.copyOf(history); }
    public List<TileState[]> getEvaluationHistory(){ return List.copyOf(evaluationHistory); }

    /**
     * Reset to initial game state.
     * <pre>
     *   @ensures history.isEmpty()
     *   @ensures evaluationHistory.isEmpty()
     *   @ensures startWord.length() == 4 && targetWord.length() == 4
     *   @ensures !startWord.equals(targetWord)
     * </pre>
     */
    public void resetGame() {
        history.clear();
        evaluationHistory.clear();

        if (flagRandomise) {
            Random r = new Random();
            startWord  = dictionary.get(r.nextInt(dictionary.size()));
            do { targetWord = dictionary.get(r.nextInt(dictionary.size())); }
            while (targetWord.equals(startWord));
        } else {
            startWord  = "soul";
            targetWord = "mate";
        }

        // Post‑condition checks
        assert startWord.length() == 4 && targetWord.length() == 4;
        assert !startWord.equals(targetWord);
        assert history.isEmpty() && evaluationHistory.isEmpty();

        notifyChange();
    }

    /**
     * Submit an attempt and record evaluation.
     * <pre>
     *   @requires word != null && word.length() == 4
     *   @ensures  (\result == true) ==> word.equals(targetWord)
     *   @ensures  history.contains(word)
     * </pre>
     * @param word 4‑letter lowercase candidate.
     * @return true if player wins.
     */
    public boolean attempt(String word) {
        Objects.requireNonNull(word, "Attempt word must not be null");
        word = word.toLowerCase(Locale.ROOT).trim();
        assert word.length() == 4 : "Caller must supply a 4‑letter word";

        if (!isValidIntermediate(word)) {
            if (flagShowError) {
                throw new IllegalArgumentException("Invalid intermediate word: " + word);
            }
            return false;
        }

        history.add(word);
        evaluationHistory.add(evaluate(word));
        assert history.size() == evaluationHistory.size();

        notifyChange();
        return word.equals(targetWord);
    }

    /**
     * Compute a (possibly non‑optimal) path for testing.
     * <pre>
     *   @requires isShowPath() == true
     *   @ensures  (\result.isEmpty()) || \result.get(0).equals(startWord)
     * </pre>
     */
    public List<String> computePath() {
        if (!flagShowPath) return List.of();

        record Node(String word, Node prev){}
        Queue<Node> q = new ArrayDeque<>();
        Set<String>  seen = new HashSet<>();
        q.add(new Node(startWord, null));
        seen.add(startWord);

        while (!q.isEmpty()) {
            Node n = q.remove();
            if (n.word.equals(targetWord)) {
                List<String> path = new ArrayList<>();
                for (Node p = n; p != null; p = p.prev) path.add(p.word);
                Collections.reverse(path);
                return path;
            }
            for (String w : dictionary) {
                if (!seen.contains(w) && differsByOne(n.word, w)) {
                    seen.add(w);
                    q.add(new Node(w, n));
                }
            }
        }
        return List.of();
    }

    //Private Helpers

    private boolean isValidIntermediate(String w) {
        if (w.length() != 4 || !dictionary.contains(w)) return false;
        String prev = history.isEmpty() ? startWord : history.get(history.size() - 1);
        return differsByOne(prev, w);
    }

    private static boolean differsByOne(String a, String b) {
        int diff = 0;
        for (int i = 0; i < a.length(); i++) if (a.charAt(i) != b.charAt(i)) diff++;
        return diff == 1;
    }

    private TileState[] evaluate(String attempt) {
        TileState[] res = new TileState[4];
        char[] tgt = targetWord.toCharArray();
        boolean[] matched = new boolean[4];

        for (int i = 0; i < 4; i++) {
            if (attempt.charAt(i) == tgt[i]) { res[i] = TileState.CORRECT; matched[i] = true; }
        }
        for (int i = 0; i < 4; i++) if (res[i] == null) {
            char c = attempt.charAt(i);
            boolean present = false;
            for (int j = 0; j < 4; j++) if (!matched[j] && c == tgt[j]) { present = true; matched[j] = true; break; }
            res[i] = present ? TileState.PRESENT : TileState.ABSENT;
        }
        return res;
    }

    private void notifyChange() { setChanged(); notifyObservers(); }
}
