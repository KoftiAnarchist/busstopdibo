/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package busstop;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marian
 */
public class BusStop {
    public static void main(String[] args) {
        try {
            ReverseIndex rindex = new ReverseIndex("bustop.txt");
            rindex.createReverseIndex();
            rindex.sortIndex();
            rindex.writeBinary("stadata.bin","staindex.bin");
            
            Tree tre = new Tree();
            tre.createTree(rindex);
            System.out.println("Sonuc: "+tre.findDirectPath(5L, 148L));
            
        } catch (IOException ex) {
            Logger.getLogger(BusStop.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
