package cn.m15.gotransfer.sdk.database;

import java.lang.reflect.Field;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Defines a contract between the Transfer content provider and its clients. 
 */ 
public final class Transfer {

	public static String AUTHORITY = initAuthority();
	
	/**
	 * 通过反射获取App中定义的Authority, 实现动态绑定
	 * App中的Authority 需要分别定义在 AndroidManifest.xml, com.jiubang.data.Authority 中
	 * 
	 * @return 返回动态绑定的Authority
	 */
	private static String initAuthority() {
        String authority = "cn.m15.provider.Transfer";

        try {

            ClassLoader loader = Transfer.class.getClassLoader();

            Class<?> clz = loader.loadClass("com.jiubang.data.Authority");
            Field declaredField = clz.getDeclaredField("CONTENT_AUTHORITY");

            authority = declaredField.get(null).toString();
        } catch (ClassNotFoundException e) {} 
        catch (NoSuchFieldException e) {} 
        catch (IllegalArgumentException e) {} 
        catch (IllegalAccessException e) {}

        return authority;
    }	

    // This class cannot be instantiated
    private Transfer() {
    }

	/**
	 * User table
	 */
	public static final class User implements BaseColumns {
		
	    // This class cannot be instantiated
	    private User() {}
	
	    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/user");
	
	    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.cn.m15.provider.Transfer.user";
	
	    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+ "/vnd.cn.m15.provider.Transfer.user";
	
	    public static final String DEFAULT_SORT_ORDER = "name ASC";

	    /**
	     * Column name for user name 用户名称
	     * <P>Type: TEXT</P>
	     */
	    public static final String NAME = "name";
	    
	    /**
	     * Column name for user avatar file path 用户头像path
	     * <P>Type: TEXT</P>
	     */
	    public static final String AVATAR = "avatar";
	    
	    /**
	     * Column name for device mac address 设备MAC地址
	     * <P>Type: TEXT</P>
	     */
	    public static final String MAC_ADDRESS = "mac_address";
	}
	    
    
	/**
	 * Conversation table
	 */
	public static final class Conversation implements BaseColumns {
		
	    // This class cannot be instantiated
	    private Conversation() {}
	
	    /**
	     * The content:// style URL for this table
	     */
	    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/conversation");
	
	    /**
	     * The MIME type of {@link #CONTENT_URI} providing a directory of order.
	     */
	    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.cn.m15.provider.Transfer.conversation";
	
	    /**
	     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single order.
	     */
	    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.cn.m15.provider.Transfer.conversation";
	
	    /**
	     * The default sort order for this table
	     */
	    public static final String DEFAULT_SORT_ORDER = "packet_id DESC";
	    
	    /**
	     * Column name for packet id 传输时生成的Packet ID
	     * <P>Type: INTEGER</P>
	     */
	    public static final String PACKET_ID = "packet_id";

	    /**
	     * Column name for file name 文件名
	     * <P>Type: TEXT</P>
	     */
	    public static final String FILENAME = "filename";

	    /**
	     * Column name for source path 传输文件的源路径
	     * <P>Type: TEXT</P>
	     */
	    public static final String SRCPATH = "srcpath";
	    
	    /**
	     * Column name for local path 接收文件的本地路径
	     * <P>Type: TEXT</P>
	     */
	    public static final String LOCALPATH = "localpath";

	    /**
	     * Column name for send or receive 是否是发送
	     * <P>Type: INTEGER</P>
	     */
	    public static final String IS_SEND = "is_send";
	    
	    /**
	     * Column name for friend name 好友名字
	     * <P>Type: TEXT</P>
	     */
	    public static final String FRIEND = "friend";

	    /**
	     * Column name for partner mac address 设备 MAC address
	     * <P>Type: TEXT</P>
	     */
	    public static final String MAC_ADDRESS = "mac_address";
	    
	    /**
	     * Column name for file type 文件类型
	     * <P>Type: INTEGER</P>
	     */
	    public static final String FILETYPE = "file_type";

	    /**
	     * Column name for transfer status 传输状态״̬
	     * <P>Type: INTEGER</P>
	     */
	    public static final String STATUS = "status";
	    
	    /**
	     * Column name for the whole transfer status 整体传输状态״̬
	     * <P>Type: INTEGER</P>
	     */
	    public static final String WHOLE_STATUS = "whole_status";

	    /**
	     * Column name for file size 文件大小
	     * <P>Type: INTEGER</P>
	     */
	    public static final String FILESIZE = "file_size";

	    /**
	     * Column name for created  传输创建时间
	     * <P>Type: INTEGER </P>
	     */
	    public static final String CREATED = "created";

	    /**
	     * Column name for interrupt position  中断时已传输的字节数，用于断点续传
	     * <P>Type: INTEGER </P>
	     */
	    public static final String POSITION = "position";
	    
	    /**
	     * Column name for remaining paths 文件夹内所有未接收的文件路径
	     * <P>Type: TEXT</P>
	     */
	    public static final String REMAINING_PATHS = "remaining_paths";
	    
	    /**
	     * Column name for last modified 文件最后修改时间
	     * <P>Type: INTEGER </P>
	     */
	    public static final String LAST_MODIFIED = "last_modified";

	}

	
}
