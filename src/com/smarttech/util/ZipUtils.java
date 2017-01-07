package com.smarttech.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.util.Log;

import com.smarttech.conf.Config;



//import org.apache.commons.lang.StringUtils;

public class ZipUtils {

    private static final int COMPRESSION_LEVEL = 8;

    private static final int BUFFER_SIZE = 1024 * 2;

    /**
     * 지정된 폴더를 Zip 파일로 압축한다
     * @param sourcePath - 압축 대상 디렉토리
     * @param output - 저장 Zip파일 이름
     * @throws Exception
     */
    public static void zip(String sourcePath, String output) throws Exception {

        // 압축대상(sourcePath)이 디렉토리나 파일이 아니라면 리턴한다.
        File sourceFile = new File(sourcePath);
        if (!sourceFile.isFile() && !sourceFile.isDirectory()) {
            throw new Exception("압축 대상싀 파일을 찾을수가 없습니다.");
        }

        // output의 확장자가  zip이 아니면 리턴한다.
//        if (!(StringUtils.substringAfterLast(output, ".")).equalsIgnoreCase("zip")) {
       	if (! output.toLowerCase().endsWith("zip")) {
            throw new Exception("압축후 저장 파일명의 확장자를 확인하세요");
        }

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zos = null;

        try {
            fos = new FileOutputStream(output); // FileOutputStream
            bos = new BufferedOutputStream(fos); // BufferedStream
            zos = new ZipOutputStream(bos); // ZipOutputStream
            zos.setLevel(COMPRESSION_LEVEL); // 압축레벨 - 최대 압축률은 9, 디폴트 8
            zipEntry(sourceFile, sourcePath, zos); // Zip 파일 생성
            zos.finish(); // ZipOutputStream finish
        } finally {
            if (zos != null) {
                zos.close();
            }
            if (bos != null) {
                bos.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * 압축
     * @param sourceFile
     * @param sourcePath
     * @param zos
     * @throws Exception
     */
    private static void zipEntry(File sourceFile, String sourcePath, ZipOutputStream zos) throws Exception {
        // sourceFile 이 디렉토리인 경우 하위 파일 리스트 가져와 재귀호출
        if (sourceFile.isDirectory()) {
            if (sourceFile.getName().equalsIgnoreCase(".metadata")) { // .metadata 디렉토리 return
                return;
            }
            File[] fileArray = sourceFile.listFiles(); // sourceFile 의 하위 파일리스트
            for (int i = 0; i < fileArray.length; i++) {
                zipEntry(fileArray[i], sourcePath, zos); // 재구호출
            }
        } else { // sourcehFile 이 디렉토리가 아닌경우
        	
        	BufferedInputStream bis = null;
            try {
                String sFilePath = sourceFile.getPath();
                String zipEntryName = sFilePath.substring(sourcePath.length() + 1, sFilePath.length());

                bis = new BufferedInputStream(new FileInputStream(sourceFile));
                ZipEntry zentry = new ZipEntry(zipEntryName);
                zentry.setTime(sourceFile.lastModified());
                zos.putNextEntry(zentry);

                byte[] buffer = new byte[BUFFER_SIZE];
                int cnt = 0;
                while ((cnt = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    zos.write(buffer, 0, cnt);
                }
                zos.closeEntry();
            } finally {
                if (bis != null) {
                    bis.close();
                }
            }
        }
    }

    /**
     * Zip 파일의 압축을 푼다.
     *
     * @param zipFile   - 압축 풀 Zip파일
     * @param targetDir - 압축 푼파일이 들어갈 디렉토리
     * @param fileNameToLowerCase - 파일명을 소문자로 변환할지 여부
     * * @throws Exception
     */
    public static void unzip(File zipFile, File targetDir, boolean fileNameToLowerCase) throws Exception {
        FileInputStream fis = null;
        ZipInputStream zis = null;
        ZipEntry zentry = null;

        try {
        	System.out.println(" ################################ ");
        	System.out.println(" zipFile===["+zipFile.getAbsoluteFile()+"]");
        	System.out.println(" ################################ ");
            fis = new FileInputStream(zipFile); // FileInputStream
            zis = new ZipInputStream(fis); // ZipInputStream

            while ((zentry = zis.getNextEntry()) != null) {
                String fileNameToUnzip = zentry.getName();
                
                System.out.println(" fileNameToUnzip === ["+fileNameToUnzip+"]");
                if (fileNameToLowerCase) { // fileName toLowerCase
                    fileNameToUnzip = fileNameToUnzip.toLowerCase();
                }

                File targetFile = new File(targetDir, fileNameToUnzip);

                if (zentry.isDirectory()) {// Directory 인경우
                	
//                    FileUtils.makeDir(targetFile.getAbsolutePath()); //디렉토리 생성
                	if(!zipFile.mkdirs()){} // 디렉토리 생성                
                    
                } else { // File 인경우 
                    // parent Directory 생성
                	//  FileUtils.makeDir(targetFile.getParent());
                	if(!zipFile.mkdirs()){} // 디렉토리 생성                  	
                    unzipEntry(zis, targetFile);
                    
                    if( zipFile.exists() ){
                    	Log.d(Config.TAG," zipFile deleted ... ");
                    	zipFile.delete();
                    }
                }
            }
            
        } finally {
            if (zis != null) {
                zis.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * Zip 파일의 한개 엔트리의 압축을 푼다.
     *
     * @param zis - Zip Input Stream
     * @param filePath -압축이 풀린 파일의 경로
     * @return
     * @throws Exception
     */
    protected static File unzipEntry(ZipInputStream zis, File targetFile) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(targetFile);

            byte[] buffer = new byte[BUFFER_SIZE];
            int len = 0;
            while ((len = zis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        return targetFile;
    }
    
    
}

