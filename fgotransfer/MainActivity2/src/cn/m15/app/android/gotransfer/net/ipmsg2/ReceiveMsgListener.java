package cn.m15.app.android.gotransfer.net.ipmsg2;


/**
 * 接收消息监听的listener接口
 * 
 */
public interface ReceiveMsgListener {
    public boolean receive(ChatMessage msg);

}
