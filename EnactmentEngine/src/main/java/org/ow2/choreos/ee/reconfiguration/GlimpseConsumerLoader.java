package org.ow2.choreos.ee.reconfiguration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GlimpseConsumerLoader extends ClassLoader {
    
    public GlimpseConsumerLoader(ClassLoader parent) {
        super(parent);
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        
        if (!name.endsWith("ChorGlimpseConsumer"))
            return super.loadClass(name);
        
        String path =  name.replace(".", "/") + ".class";
        
        System.out.println(path);

        InputStream input = getResourceAsStream(path);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int data = 0;
        try {
            data = input.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(data != -1){
            buffer.write(data);
            try {
                data = input.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            input.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        byte[] classData = buffer.toByteArray();

        return defineClass(name,
                classData, 0, classData.length);
    }
}