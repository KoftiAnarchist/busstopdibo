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
    final static boolean create = false;
    public static void main(String[] args) {
        try {
            if (create) {
                ReverseIndex rindex = new ReverseIndex("bustop.txt");
                rindex.createReverseIndex();
                rindex.sortIndex();
                rindex.writeBinary("stadata.bin", "staindex.bin");

                Tree tre = new Tree();
                tre.createTree(rindex);
                tre.dumpTree("treeindex.bin", "treedata.bin");
                System.out.println("Sonuc: " + tre.findDirectPath(5L, 148L));
            } else {
                Tree tre = new Tree();
                tre.loadTree("treeindex.bin", "treedata.bin");
                System.out.println("Sonuc: " + tre.findDirectPath(11L, 12L));
            }

        } catch (IOException ex) {
            Logger.getLogger(BusStop.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
