/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shellscript3;

import java.io.*;
import java.util.Properties;

/**
 *
 * @author jsf3
 */
public class ShellScript3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        int i = 0;
        int cnt = 0;
        int cnt2 = 1;
        String s1 = "";
        String s2 = "";
        
        Properties prop = new Properties();
        OutputStream output = null;
        
        output = new FileOutputStream("config.properties");
        
        while(i < args.length)
        {
            if(i==0)
            {
                s1 = args[cnt];
                s2 = args[cnt2];
                cnt = cnt + 2;
                cnt2 = cnt2 + 2;
                String out = s1 + s2;
                prop.setProperty(s1,s2);
                i = i + 2;
            }
            else
            {
                s1 = args[cnt];
                s2 = args[cnt2];
                String out = s1 + s2;
                prop.setProperty(s1,s2);
                cnt = cnt + 2;
                cnt2 = cnt2 + 2;
                i = i + 2;
            }
        }
        prop.store(output,null);
    }
    
}
