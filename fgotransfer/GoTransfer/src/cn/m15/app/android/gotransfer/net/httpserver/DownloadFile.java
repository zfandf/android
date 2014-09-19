package cn.m15.app.android.gotransfer.net.httpserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DownloadFile {
	
	public static String downloadFile(String[] files) {
		Date d = new Date();
		String tmp = System.getProperty("java.io.tmpdir");
		String zipFileName = tmp + File.separator + d.getTime() + ".zip";
		
		try {
			boolean ret = zip(zipFileName, files);
			if (ret) {
				return zipFileName;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static boolean zip(String zipFileName, String[] files) throws IOException {  
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFileName)));
        BufferedOutputStream bo = new BufferedOutputStream(out);
        boolean ret = true;
        for (String file : files) {
        	if (ret) {
        		File inputFile = new File(file);
        		ret = zip(out, inputFile, inputFile.getName(), bo);
        	}
		}
        bo.close();
        out.close();
        return ret;
    }  
  
    private static boolean zip(ZipOutputStream out, File f, String base, BufferedOutputStream bo) throws IOException {
        if (f.isDirectory()) { 
            File[] list = f.listFiles();  
            if (list.length == 0) {  
                out.putNextEntry(new ZipEntry(base + File.separator));
            }  
            for (int i = 0; i < list.length; i++) {  
                boolean ret = zip(out, list[i], base + File.separator + list[i].getName(), bo);
                if (!ret) {
                	return false;
                }
            }
        } else {  
            out.putNextEntry(new ZipEntry(base)); 
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(f));  
            int len;
            byte[] buffer = new byte[1024*2];
            while ((len = input.read(buffer)) != -1) {  
                bo.write(buffer, 0, len);
            }
            bo.flush();
            input.close();
        }
        return true;
    }
}