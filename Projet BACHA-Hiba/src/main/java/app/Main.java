package app;

import codagebits.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Map<String,String> p = parseArgs(args);
        String mode   = p.getOrDefault("mode", "sans"); // "sans" | "avec"
        int n         = Integer.parseInt(p.getOrDefault("taille", "20"));
        int graine    = Integer.parseInt(p.getOrDefault("graine", "1"));
        int max       = Integer.parseInt(p.getOrDefault("max", "4095"));

        int[] donnees = gen(n, graine, max);

        // ➡️ AFFICHAGE DU TABLEAU ORIGINAL
        System.out.println("Tableau original ("+n+" éléments) : " + Arrays.toString(donnees));

        CodeurBits codeur = FactoryCodeurBits.creer(mode);

        int[] compresse = codeur.compresser(donnees);

        int mid = n/2;
        int valeur = (n > 0 ? codeur.acceder(mid) : -1);

        int[] decompresse = codeur.decompresser(compresse);

        System.out.printf("mode=%s n=%d get(%d)=%d ok=%b%n",
                mode, n, mid, valeur, Arrays.equals(donnees, decompresse));
    }

    private static Map<String,String> parseArgs(String[] a){
        Map<String,String> m = new HashMap<>();
        for(int i=0;i<a.length;i++) if(a[i].startsWith("--")){
            String k = a[i].substring(2);
            if(i+1<a.length && !a[i+1].startsWith("--")) m.put(k,a[++i]); else m.put(k,"");
        }
        return m;
    }

    private static int[] gen(int n,int seed,int max){
        Random r=new Random(seed); int[] t=new int[n];
        for(int i=0;i<n;i++) t[i]=r.nextInt(max+1);
        return t;
    }
}
