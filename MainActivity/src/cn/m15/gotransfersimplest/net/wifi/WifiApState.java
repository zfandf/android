/**
 * WifiApState 定义类，Android源代码中有相同的定义，但对外部隐藏了
 */
package cn.m15.gotransfersimplest.net.wifi;

public enum WifiApState {
    WIFI_AP_STATE_DISABLING, 
    WIFI_AP_STATE_DISABLED, 
    WIFI_AP_STATE_ENABLING, 
    WIFI_AP_STATE_ENABLED, 
    WIFI_AP_STATE_FAILED
}