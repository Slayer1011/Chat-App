package com.application.chat;

public class InstanceHelper implements SeenIndicatorListener,OnlineIndicatorListener{
    private static InstanceHelper instanceHelper;
    private  static InstanceHelper instanceHelper2;
    private InstanceHelper() {
    }
    public static synchronized InstanceHelper getSeenIndicatorListener(){
        if (instanceHelper==null){
            instanceHelper=new InstanceHelper();
        }
        return instanceHelper;
    }
    public static synchronized InstanceHelper getOnlineIndicatorListener(){
        if (instanceHelper2==null){
            instanceHelper2=new InstanceHelper();
        }
        return instanceHelper2;
    }
    @Override
    public void onSeenIndicatorChange(boolean seen,String sender,String reciver,String remoteUserId) {

    }
    @Override
    public void listenOnline(boolean status) {

    }
}
