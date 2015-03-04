/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shellscript;
import java.util.Properties;
import java.io.OutputStream;
import java.io.FileOutputStream;

/**
 *
 * @author jeroen
 */
public class ShellScript {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Properties prop = new Properties();
        OutputStream output = null;
        
        try{
            output = new FileOutputStream("config.properties");
            
            for(String env: args)
            {
                String value = System.getenv(env);
                if(value != null)
                {
                    System.out.format("%s=%s%n",env,value);
                    prop.setProperty(env,String.format("%s",value));
                }
                else
                {
                    System.out.format("%s is" + " not assigned.%n",env);
                    prop.setProperty("values",String.format("%s",env));
                }
            }
            prop.store(output,null);
        }
        catch(Exception e)
        {
            System.err.println("error" + e.getMessage());
        }
        finally
        {
            if(output!=null)
            {
                try{
                output.close();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
  }
}


