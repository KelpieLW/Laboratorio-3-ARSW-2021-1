/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.blacklistvalidator;

import edu.eci.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int nHilos){
        
        LinkedList<Integer> blackListOcurrences=new LinkedList<>();
        AtomicInteger checkedListsCount=new AtomicInteger(0);
        AtomicInteger ocurrencesCount=new AtomicInteger(0);
        boolean isReported=false;
        
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        ArrayList <Thread> hilos = new ArrayList<Thread>();

        int divisiones=skds.getRegisteredServersCount()/nHilos;
        int indiceInicio=0;
        int indiceFinal=divisiones;

        for (int i = 0; i <nHilos; i++) {

            if (i==nHilos-1){

                indiceFinal+=skds.getRegisteredServersCount()%nHilos;

            }

            hilos.add(new BlackListInThread (ipaddress, indiceInicio, indiceFinal, blackListOcurrences, checkedListsCount, ocurrencesCount));
            indiceInicio+=divisiones;
            indiceFinal+=divisiones;

        }

        


        for (int i=0; i<hilos.size(); i++) {
            hilos.get(i).start();
        }

        for (int i=0; i<hilos.size(); i++){
            try {
                hilos.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


        while(true) {
            if (ocurrencesCount.get() >= BLACK_LIST_ALARM_COUNT) {
                skds.reportAsNotTrustworthy(ipaddress);
                for (int i = 0; i <nHilos; i++) {
                    hilos.get(i).stop();
                }
                isReported=true;

                break;
            }
            if (checkedListsCount.get()==skds.getRegisteredServersCount()){
                break;
            }

        }
        if (!isReported) {
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});
        
        return blackListOcurrences;
    }
    
    
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    
    
    
}
