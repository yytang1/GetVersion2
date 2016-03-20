package GetVersion;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DealSoftware {

    public ArrayList<String> getFileName(String filepath, String software) {
        filepath = filepath + File.separator + software;
        File file = new File(filepath);
        String[] filelisttemp = file.list();
        ArrayList<String> filelist = new ArrayList<String>();
        String test = "-" + Common.re_version + "$";
        String re = "^" + software + test;
        Pattern pattern = Pattern.compile(re, Pattern.MULTILINE);
        for (int i = 0; i < filelisttemp.length; i++) {
            Matcher matcher = pattern.matcher(filelisttemp[i]);
            if (matcher.find()) {
                filelist.add(filelisttemp[i]);
            }
        }
        ArrayList<String> fileLists = new ArrayList<String>();

        // TODO 选出文件夹
        for (String filestr : filelist) {
            File readfile = new File(filepath + File.separator + filestr);
            if (readfile.isDirectory()) {
                fileLists.add(filestr);
               // System.out.println(filestr);
            }
        }
        System.out.println(fileLists.size());
        return fileLists;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        DealSoftware dealSoftware = new DealSoftware();
        String path = "C:\\Users\\wt\\Desktop\\tyy\\software";
        String software = "ffmpeg";
        String software2 = "libav";
        dealSoftware.getFileName(path, software);
        dealSoftware.getFileName(path, software2);
    }
}
