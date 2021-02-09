package edu.eci.blacklistvalidator;

import edu.eci.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class BlackListInThread extends Thread{
    int start;
    int end;
    String ipaddress;
    LinkedList blackList;
    AtomicInteger checkedListsCount;
    AtomicInteger ocurrencesCount;
    private static final int BLACK_LIST_ALARM_COUNT=5;
    HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();


    public BlackListInThread(String ipaddress, int start, int end, LinkedList blackListOcurrences, AtomicInteger checkedListsCount, AtomicInteger ocurrencesCount){

        this.start=start;
        this.end=end;
        this.ipaddress=ipaddress;
        this.blackList=blackListOcurrences;
        this.ocurrencesCount=ocurrencesCount;
        this.checkedListsCount=checkedListsCount;


    }

    @Override
    public void run(){


        for (int i=start;i<end && ocurrencesCount.get()<BLACK_LIST_ALARM_COUNT;i++){
            synchronized (this){
                checkedListsCount.getAndIncrement();
                if (skds.isInBlackListServer(i, ipaddress)){
                    blackList.add(i);
                    ocurrencesCount.getAndIncrement();

                }
            }

        }
    }

    public AtomicInteger showOcurrences(){
        return ocurrencesCount;
    }
}
