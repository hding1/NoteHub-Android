package com.cs48.g15.notehub;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class BackendUnitTest {
    @Test(expected = FileNotFoundException.class)
    public void test_upload1() throws FileNotFoundException {
        InputStream stream = new FileInputStream("testFileNotFound");
    }

    @Test(expected = FileNotFoundException.class)
    public void test_upload2() throws FileNotFoundException {
        InputStream stream = new FileInputStream("non_dir/xxx.pdf");
    }

    @Test(expected = FileNotFoundException.class)
    public void test_upload3() throws FileNotFoundException {
        InputStream stream = new FileInputStream("/sdcard/Download/??");
    }

    @Test(expected = WrongPostfixException.class)
    public void test_upload4() throws WrongPostfixException {
        String file_name = "test.ppt";
        int i = file_name.indexOf('.');
        if (!file_name.substring(i).equals("pdf")){
            throw new WrongPostfixException("Only PDF files are allowed");
        }    }

    @Test(expected = WrongPostfixException.class)
    public void test_upload5() throws WrongPostfixException {
        String file_name = "test1.docx";
        int i = file_name.indexOf('.');
        if (!file_name.substring(i).equals("pdf")){
            throw new WrongPostfixException("Only PDF files are allowed");
        }    }

    @Test(expected = WrongPostfixException.class)
    public void test_upload6() throws WrongPostfixException {
        String file_name = "test3.pdff";
        int i = file_name.indexOf('.');
        if (!file_name.substring(i).equals("pdf")){
            throw new WrongPostfixException("Only PDF files are allowed");
        }    }
}