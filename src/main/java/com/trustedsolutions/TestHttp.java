/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trustedsolutions;

import ts.ruler.PostUploader;

/**
 *
 * @author loki.chuang
 */
public class TestHttp
{

    public static void main(String args[])
    {
        PostUploader mySqlUploader = new PostUploader();
        mySqlUploader.Start();
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                mySqlUploader.Stop();
            }
        });

    }
}
