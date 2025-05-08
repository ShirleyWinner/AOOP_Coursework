package weaver;
import java.nio.file.Paths; import java.util.Scanner;
public final class WeaverCLIMain {
    public static void main(String[] args) throws Exception{
        WeaverModel model=new WeaverModel(Paths.get("dictionary.txt"));
        Scanner sc=new Scanner(System.in);
        System.out.printf("Start: %s  Target: %s\n",model.getStartWord(),model.getTargetWord());
        while(true){
            System.out.print("Enter 4‑letter word → ");
            String w=sc.nextLine().trim();
            try{ if(model.attempt(w)){ System.out.println("Done!"); break; } }
            catch(IllegalArgumentException ex){ System.out.println(ex.getMessage()); }
        }
    }
}
