/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package busstop;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marian
 */
public class ReverseIndex {

    public HashMap<Long, Station> map;
    String File;
    long bustopcount;
    long maxsta;
    long minsta;

    ReverseIndex(String inputFile) {
        File = inputFile;
        map = new HashMap<Long, Station>();
    }

    public void createReverseIndex() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(File));
            String line = br.readLine();
            bustopcount = Integer.parseInt(line.trim());
            long read = 0;
            line = br.readLine();
            while (line != null) {
                String parts[] = line.split(" ");
                long routeid = Long.parseLong(parts[0]);

                for (int i = 1; i < parts.length && i < 1000; i++) {
                    long sta = Long.parseLong(parts[i]);
                    if (read == 1 && i == 1) {
                        maxsta = sta;
                        minsta = sta;
                    } else {
                        if (sta > maxsta) {
                            maxsta = sta;
                        }
                        if (sta < minsta) {
                            minsta = sta;
                        }
                    }
                    if (map.containsKey(sta)) {
                        Station st = map.get(sta);
                        st.addBusStop(routeid, i);
                        map.put(sta, st);
                    } else {
                        Station st = new Station(sta);
                        st.addBusStop(routeid, i);
                        map.put(sta, st);
                    }
                }
                read++;
                if (read > 100000) {
                    break;
                }
                line = br.readLine();

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReverseIndex.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReverseIndex.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(ReverseIndex.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void sortIndex() {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            ((Station) pair.getValue()).sortData();
        }
    }

    public void writeBinary(String dataName, String indexname) throws FileNotFoundException, IOException {
        DataOutputStream os = new DataOutputStream(new FileOutputStream(dataName));
        DataOutputStream os2 = new DataOutputStream(new FileOutputStream(indexname));
        Iterator it = map.entrySet().iterator();
        Long index = 0L;
        long length;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Station st = (Station) pair.getValue();
            st.index = index;
            length = st.write(os);
            index += length;
            os2.writeLong(st.id);
            os2.writeLong(st.index);
        }
        os.close();
        os2.close();
    }

}
