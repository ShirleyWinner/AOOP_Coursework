package weaver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/** Swing View implementing Observer; renders grid + virtual keyboard. */
public final class WeaverGUIView extends JFrame implements Observer {
    private static final Color COLOR_CORRECT = new Color(106,170,100);
    private static final Color COLOR_PRESENT = new Color(201,180,88);
    private static final Color COLOR_ABSENT  = new Color(120,124,126);
    private static final Color COLOR_EMPTY   = new Color(215,218,220);
    private static final Font  TILE_FONT = new Font("SansSerif",Font.BOLD,24);
    private static final int   MAX_ROWS = 20;

    private final WeaverModel       model;
    private final WeaverController  controller;

    private final JLabel[][] tiles = new JLabel[MAX_ROWS][4];
    private int currentRow = 0;
    private final JTextField input = new JTextField(4);

    public WeaverGUIView(WeaverModel m, WeaverController c){
        this.model = m; this.controller = c;
        m.addObserver(this);

        setTitle("Weaver – GUI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        //Board panel
        JPanel board = new JPanel(new GridLayout(MAX_ROWS,4,4,4));
        for(int r=0;r<MAX_ROWS;r++){
            for(int col=0;col<4;col++){
                JLabel lab=new JLabel("",SwingConstants.CENTER);
                lab.setOpaque(true);
                lab.setPreferredSize(new Dimension(50,50));
                lab.setFont(TILE_FONT);
                lab.setBackground(COLOR_EMPTY);
                lab.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                tiles[r][col]=lab;
                board.add(lab);
            }
        }
        add(board,BorderLayout.CENTER);

        //Control panel
        JPanel south=new JPanel(new FlowLayout());
        south.add(new JLabel("Word:"));
        input.setFont(TILE_FONT);
        south.add(input);
        JButton submit=new JButton("Enter");
        south.add(submit);
        JButton reset=new JButton("Reset");
        south.add(reset);
        JButton newGame=new JButton("New Game");
        south.add(newGame);
        JCheckBox showPath=new JCheckBox("Show path");
        south.add(showPath);
        add(south,BorderLayout.SOUTH);

        submit.addActionListener(e->{controller.submitWord(input.getText());input.setText("");});
        reset.addActionListener(e->controller.resetGame());
        newGame.addActionListener(e->controller.newGame());
        showPath.addActionListener(e->controller.setShowPath(showPath.isSelected()));

        // allow ENTER key on text field
        input.addActionListener(submit.getActionListeners()[0]);

        // physical keyboard listener (letters only)
        addKeyListener(new KeyAdapter(){
            @Override public void keyTyped(KeyEvent e){
                char ch=Character.toLowerCase(e.getKeyChar());
                if(ch>='a'&&ch<='z'&&input.getText().length()<4) input.setText(input.getText()+ch);
                if(ch=='\n' && input.getText().length()==4) submit.doClick();
            }});
        setFocusable(true);

        //Virtual keyboard
        JPanel keyPanel=new JPanel(); keyPanel.setLayout(new BoxLayout(keyPanel,BoxLayout.Y_AXIS));
        String[] rows={"qwertyuiop","asdfghjkl","zxcvbnm"};
        for(String row:rows){
            JPanel kpRow=new JPanel(new FlowLayout(FlowLayout.CENTER,2,2));
            for(char ch:row.toCharArray()){
                JButton b=new JButton(String.valueOf(Character.toUpperCase(ch)));
                b.setPreferredSize(new Dimension(40,48));
                b.setFont(TILE_FONT);
                b.setBackground(COLOR_EMPTY);
                b.setFocusPainted(false);
                b.addActionListener(e->{ if(input.getText().length()<4) input.setText(input.getText()+((JButton)e.getSource()).getText().toLowerCase()); });
                kpRow.add(b);
            }
            keyPanel.add(kpRow);
        }
        add(keyPanel,BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        refreshView();
    }

    // Observer callback
    @Override public void update(Observable o, Object arg){ refreshView(); }

    private void refreshView(){
        List<String> hist=model.getHistory();
        List<WeaverModel.TileState[]> evalHist=model.getEvaluationHistory();

        // reset tiles
        for(int r=0;r<MAX_ROWS;r++) for(int c=0;c<4;c++){ tiles[r][c].setText(" "); tiles[r][c].setBackground(COLOR_EMPTY);}

        currentRow=hist.size();
        // first row = start word grey
        String start=model.getStartWord();
        for(int i=0;i<4;i++){ tiles[0][i].setText(String.valueOf(Character.toUpperCase(start.charAt(i)))); tiles[0][i].setBackground(COLOR_EMPTY); }
        // fill history rows starting at row 1
        for(int r=0;r<hist.size();r++){
            String w=hist.get(r);
            WeaverModel.TileState[] states=evalHist.get(r);
            for(int c=0;c<4;c++){
                JLabel lab=tiles[r+1][c];
                lab.setText(String.valueOf(Character.toUpperCase(w.charAt(c))));
                switch(states[c]){
                    case CORRECT -> lab.setBackground(COLOR_CORRECT);
                    case PRESENT -> lab.setBackground(COLOR_PRESENT);
                    case ABSENT  -> lab.setBackground(COLOR_ABSENT);
                    default      -> lab.setBackground(COLOR_EMPTY);
                }
            }
        }
        // last row target word grey bottom (like screenshot)
        int targetRow=MAX_ROWS-1;
        String tgt=model.getTargetWord();
        for(int i=0;i<4;i++){ tiles[targetRow][i].setText(String.valueOf(Character.toUpperCase(tgt.charAt(i)))); tiles[targetRow][i].setBackground(COLOR_EMPTY);}

        repaint();
    }
}
