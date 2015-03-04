/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shellscript2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 *
 * @author jeroen
 */
public class ShellScript2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Properties prop = new Properties();
        InputStream input = null;
        
        try{
            input = new FileInputStream("config.properties");
            
            prop.load(input);
            Enumeration<?> e = prop.propertyNames();
            while(e.hasMoreElements())
            {
                String key = (String)e.nextElement();
                System.out.println(key + " " + prop.getProperty(key));
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        finally{
            if(input != null)
            {
                try 
                {
                    input.close();   
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
