package cn.m15.gotransfer.sdk.net.ipmsg;


/**
 * 接收消息监听的listener接口
 * 
 */
public interface ReceiveMsgListener {
    public boolean receive(ChatMessage msg);

}
