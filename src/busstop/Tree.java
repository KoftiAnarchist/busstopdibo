/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package busstop;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marian
 */
public class Tree {

    Branch mainbranch;

    public void Tree() {
    }

    public void createTree(ReverseIndex index) {
        mainbranch = new Branch(index.minsta, index.maxsta, true);
        Iterator it = index.map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Station sta = (Station) pair.getValue();
            mainbranch.addValue(new StaShort(sta.id, sta.index));
        }
    }

    public void dumpTree(String indexFile, String dataFile) throws FileNotFoundException, IOException {
        DataOutputStream os = new DataOutputStream(new FileOutputStream(indexFile));
        DataOutputStream os2 = new DataOutputStream(new FileOutputStream(dataFile));
        mainbranch.writeBinary(os, os2, 0);
    }

    public void loadTree(String indexFile, String dataFile) throws FileNotFoundException, IOException {
        DataInputStream is = new DataInputStream(new FileInputStream(indexFile));
        mainbranch = new Branch(is, dataFile);
    }

    public boolean findDirectPath(long sta1, long sta2) {
        StaShort stat1 = mainbranch.getSta(sta1);
        if (stat1 != null) {
            StaShort stat2 = mainbranch.getSta(sta2);
            if (stat2 != null) {
                try {
                    Station station1 = new Station("stadata.bin", stat1.index);
                    Station station2 = new Station("stadata.bin", stat2.index);
                    int ind1 = 0;
                    int ind2 = 0;
                    if (station1.routes.size() > 0 && station2.routes.size() > 0) {
                        while (true) {
                            if (ind1 >= station1.routes.size() || ind2 >= station2.routes.size()) {
                                break;
                            }
                            if (station1.routes.get(ind1).id == station2.routes.get(ind2).id) {
                                //If the bus route queue is not important, 
                                //then please comment the code below
                                //and just write "return true;"
                                if (station1.routes.get(ind1).queue > station2.routes.get(ind2).queue) {
                                    ind1++;
                                    continue;
                                } else {
                                    return true;
                                }
                            }
                            if (station1.routes.get(ind1).id > station2.routes.get(ind2).id) {
                                ind2++;
                                continue;
                            }
                            if (station1.routes.get(ind1).id < station2.routes.get(ind2).id) {
                                ind1++;
                                continue;
                            }
                        }
                    }
                    return false;

                } catch (IOException ex) {
                    Logger.getLogger(Tree.class.getName()).log(Level.SEVERE, null, ex);
                }
                return false;
            }
        }
        return false;
    }

}

class Branch {

    final static int MAX_BRANCH_SIZE = 2;
    long min;
    long max;
    long index;
    boolean finbranch;
    Branch subbranches[] = null;
    String dataFile;
    boolean dataRead;
    HashMap<Long, StaShort> stations = null;

    Branch(long min, long max, boolean finbranch) {
        subbranches = null;
        this.min = min;
        this.max = max;
        index = 0;
        this.finbranch = finbranch;
        if (finbranch) {
            stations = new HashMap<>();
        }
    }

    Branch(DataInputStream is, String dataFile) throws IOException {
        min = is.readLong();
        max = is.readLong();
        finbranch = is.readBoolean();
        index = is.readLong();
        this.dataFile = dataFile;
        dataRead = false;
        if (!finbranch) {
            subbranches = new Branch[2];
            subbranches[0] = new Branch(is, dataFile);
            subbranches[1] = new Branch(is, dataFile);
        }
    }

    public boolean inBranch(long staid) {
        if (staid < max && staid >= min) {
            return true;
        }
        return false;
    }

    public void addValue(StaShort sta) {
        if (inBranch(sta.id)) {
            if (finbranch) {
                if (stations.size() >= MAX_BRANCH_SIZE) {
                    subbranches = new Branch[2];
                    subbranches[0] = new Branch(min, min + (max - min) / 2, true);
                    subbranches[1] = new Branch(min + (max - min) / 2, max, true);
                    Iterator it = stations.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        subbranches[0].addValue((StaShort) pair.getValue());
                        subbranches[1].addValue((StaShort) pair.getValue());
                        it.remove();
                    }
                    stations = null;
                    finbranch = false;

                    subbranches[0].addValue(sta);
                    subbranches[1].addValue(sta);
                } else {
                    stations.put(sta.id, sta);
                }
            } else {
                subbranches[0].addValue(sta);
                subbranches[1].addValue(sta);
            }
        }
    }

    public void readBinaryData() throws FileNotFoundException, IOException {
        RandomAccessFile file = new RandomAccessFile(dataFile, "r");
        file.seek(index);
        long stasize = file.readLong();
        stations = new HashMap<>();
        for (int i = 0; i < stasize; i++) {
            long id = file.readLong();
            long index = file.readLong();
            stations.put(id, new StaShort(id, index));
        }

    }

    public StaShort getSta(long staid) {
        if (finbranch) {
            if (dataRead == false && stations == null) {
                try {
                    readBinaryData();
                } catch (IOException ex) {
                    Logger.getLogger(Branch.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println(ex.getMessage());
                }
                dataRead = true;
            }
            if (stations != null) {
                if (stations.containsKey(staid)) {
                    return stations.get(staid);
                } else {
                    return null;
                }
            } else {
                return null;
            }

        } else {
            if (subbranches[0].inBranch(staid)) {
                return subbranches[0].getSta(staid);
            }
            if (subbranches[1].inBranch(staid)) {
                return subbranches[1].getSta(staid);
            }
        }
        return null;
    }

    public long writeBinary(DataOutputStream os, DataOutputStream os2, long index) throws IOException {
        os.writeLong(min);
        os.writeLong(max);
        os.writeBoolean(finbranch);
        if (finbranch) {
            Iterator it = stations.entrySet().iterator();
            os.writeLong(index);
            os2.writeLong(stations.size());
            index+=8;
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                os2.writeLong(((StaShort) pair.getValue()).id);
                os2.writeLong(((StaShort) pair.getValue()).index);
                index += 16;
            }
        } else {
            os.writeLong(0L);
            index = subbranches[0].writeBinary(os, os2, index);
            index = subbranches[1].writeBinary(os, os2, index);
        }
        return index;
    }
}

class StaShort {

    long id;
    long index;

    StaShort(long id, long index) {
        this.id = id;
        this.index = index;
    }
}
