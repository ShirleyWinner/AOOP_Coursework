package weaver;

import javax.swing.*;
import java.util.Objects;

/** Controller mediates between View and Model – no GUI code besides dialogs. */
public final class WeaverController {
    private final WeaverModel model;

    public WeaverController(WeaverModel model){ this.model = Objects.requireNonNull(model);}

    public void submitWord(String word){
        if(word==null||word.length()!=4) return; // ignore incomplete input
        try{
            if(model.attempt(word)) JOptionPane.showMessageDialog(null,"Done!","Weaver",JOptionPane.INFORMATION_MESSAGE);
        }catch(IllegalArgumentException ex){ JOptionPane.showMessageDialog(null,ex.getMessage(),"Invalid",JOptionPane.ERROR_MESSAGE);}    }

    public void resetGame(){ model.resetGame(); }
    public void newGame(){ model.setRandomise(true); model.resetGame(); }

    /* Flag toggles wired to GUI check‑boxes */
    public void setShowError(boolean on){ model.setShowError(on); }
    public void setShowPath (boolean on){ model.setShowPath(on);  }
}
