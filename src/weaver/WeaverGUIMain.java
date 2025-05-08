package weaver;
import java.nio.file.Paths;
public final class WeaverGUIMain { public static void main(String[] args) throws Exception {
    WeaverModel m=new WeaverModel(Paths.get("dictionary.txt"));
    WeaverController c=new WeaverController(m);
    new WeaverGUIView(m,c);
}}
