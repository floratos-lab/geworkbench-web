package org.geworkbenchweb.dataset.geo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * This class is used to fetch the GEO DataSets from the NCBI GEO Database and parse them them to geWorkbench
 * @author Nikhil Reddy
 */
public class GEODataFetch {

    public static final String GEO_URL = "http://www.ncbi.nlm.nih.gov/projects/geo/query/acc.cgi?acc=%s&targ=self&form=%s&view=%s";
    public static final String GDS_FTP = "ftp://ftp.ncbi.nih.gov/pub/geo/DATA/SOFT/GDS/%s.soft.gz";
    public static final String GSE_FTP = "ftp://ftp.ncbi.nih.gov/pub/geo/DATA/SOFT/by_series/%s_family.soft.gz";
    public static final String GSE_FAMILY = "http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=%s&targ=all&form=text&view=%s";

    public static boolean downloadSOFTGZ(String urlStr, File file) {
        final int BUFFER = 2048;
        BufferedInputStream bis = null;
        BufferedOutputStream dest = null;
        GZIPInputStream zis = null;
        try {
            URL url = new URL(urlStr);
            URLConnection urlc = url.openConnection();
            zis = new GZIPInputStream(urlc.getInputStream());
            int count;
            byte data[] = new byte[BUFFER];
            // write the files to the disk
            FileOutputStream fos = new FileOutputStream(file);
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = zis.read(data, 0, BUFFER)) != -1) {
               dest.write(data, 0, count);
            }
            dest.flush();
        } catch (IOException ex) {
            Logger.getLogger(GEODataFetch.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                }
            }
            if (dest != null) {
                try {
                    dest.close();
                } catch (IOException ioe) {
                }
            }
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException ioe) {
                }
            }
        }
        return true;
    }

    public static boolean downloadURL(String urlStr, File file) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            URL url = new URL(urlStr);
            URLConnection urlc = url.openConnection();

            bis = new BufferedInputStream(urlc.getInputStream());
            bos = new BufferedOutputStream(new FileOutputStream(file));

            int i;
            while ((i = bis.read()) != -1) {
                bos.write(i);
            }
        } catch (IOException ex) {
            Logger.getLogger(GEODataFetch.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ioe) {
                }
            }
        }
        return true;
    }
    
    public static synchronized File getGDS(String geoId) throws IOException {
        File tmpFile = new File(System.getProperty("user.home") + "/temp/", geoId + ".soft");
        boolean result = true;
        if (!tmpFile.exists()) {
            result = downloadSOFTGZ(String.format(GDS_FTP, geoId), tmpFile);
        }
        InputStream in = null;
        try {
            if (result) {
                in = new FileInputStream(tmpFile);
            } else {
                // Download failed somehow, maybe not saved to file
                URL url = new URL(String.format(GDS_FTP, geoId));
                in = new GZIPInputStream(url.openConnection().getInputStream());
            }
            
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
        }
        return tmpFile;
    }

    public static synchronized File getGSE(String geoId, String format) throws IOException {
        File tmpFile = new File(System.getProperty("user.home") + "/temp/", geoId + "-family-" + format + ".txt");
        boolean result = true;
        String urlStr = null;
        if (!tmpFile.exists()) {
            urlStr = String.format(GSE_FAMILY, geoId, format);
            result = downloadURL(urlStr, tmpFile);
        }
        InputStream in = null;
        try {
            if (result) {
                in = new FileInputStream(tmpFile);
            } else {
                // Download failed somehow, maybe not saved to file
                URL url = new URL(urlStr);
                in = url.openConnection().getInputStream();
            }
          
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
        }
        return tmpFile;
    }
}