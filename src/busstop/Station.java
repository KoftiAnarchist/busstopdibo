/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package busstop;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author marian
 */
public class Station {
    long id;
    long length;
    long index;
    List<InBusRoute> routes;
    
    public Station(long id){
        this.id = id;
        routes = new ArrayList<InBusRoute>();
    }
    
    public Station(String dataFile,long index) throws FileNotFoundException, IOException{
           RandomAccessFile file = new RandomAccessFile(dataFile, "r");
           file.seek(index);
           length = file.readLong();
           id = file.readLong();
           this.index = index;
           long routecount = (length-8)/16;
           routes = new ArrayList<InBusRoute>();
           for(int i=0;i<routecount;i++){
               long rtid = file.readLong();
               long queue = file.readLong();
               routes.add(new InBusRoute(rtid, queue));
            }
    
    }
    
    public void addBusStop(long id,long queue){
        routes.add(new InBusRoute(id,queue));
    }
    
    public void sortData(){
        Collections.sort(routes,new Comparator<InBusRoute>() {
        @Override
        public int compare(InBusRoute busroute1, InBusRoute busroute2)
        {
            Long id1 = busroute1.id;
            Long id2 = busroute2.id;
            return  id1.compareTo(id2);
        }
    });
    }
    
    public long write(DataOutputStream s) throws IOException{
        length = 8 + 8*2*routes.size();
        s.writeLong(length);
        s.writeLong(id);
        for (InBusRoute route : routes) {
            s.writeLong(route.id);
            s.writeLong(route.queue);
        }
        return length+8;
    }
    
 
    
}

class InBusRoute{
    long id;
    long queue;

    InBusRoute(long id, long queue) {
        this.id = id;
        this.queue = queue;
    }
}
