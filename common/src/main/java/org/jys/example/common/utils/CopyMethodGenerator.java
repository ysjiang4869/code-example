package org.jys.example.common.utils;

import javassist.*;
import org.jys.example.common.spring.CopyInDataObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YueSong Jiang
 * @date 2019/12/27
 */
public class CopyMethodGenerator {

    public static void addMethod(String className) throws NotFoundException, ClassNotFoundException, CannotCompileException {
        Class c=Class.forName(className);
        if(!CopyInData.class.isAssignableFrom(c)){
            throw new NullPointerException();
        }
        ClassPool classPool= ClassPool.getDefault();
        classPool.appendClassPath(new LoaderClassPath(CopyMethodGenerator.class.getClassLoader()));
        CtClass ct=classPool.getCtClass(className);
        CtField[] fields=ct.getDeclaredFields();
        //validate CopyOrder has different value
        boolean[] validate=new boolean[fields.length];
        for (CtField f:fields) {
            if(f.hasAnnotation(CopyOrder.class)){
                int order=((CopyOrder)f.getAnnotation(CopyOrder.class)).value();
                if(validate[order]){
                    throw new NullPointerException();
                }
                validate[order]=true;
            }
        }
        List<CtField> sortedFields= Arrays.stream(fields).filter(a->a.hasAnnotation(CopyOrder.class))
                .sorted((a,b)-> {
                    try {
                        return ((CopyOrder)a.getAnnotation(CopyOrder.class)).value()-((CopyOrder)b.getAnnotation(CopyOrder.class)).value();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }).collect(Collectors.toList());
        StringBuilder body=new StringBuilder("return new StringBuilder()");
        for (CtField f: sortedFields ){
            body.append(".append(").append(f.getName()).append(").append(").append("getDelimiter()").append(")");
        }
        body.append(".toString();");
        System.out.println(body);
        ct.getDeclaredMethod("generateCopyString").setBody(body.toString());
        ct.toClass();
        try {
            ct.writeFile("test");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        try {
            addMethod("org.jys.example.common.spring.CopyInDataObject");
            CopyInDataObject p=new CopyInDataObject();
            p.setRecordId(1111111L);
            p.setPersonId("xxx");
            String copyString=p.generateCopyString();
            System.out.println(copyString);
        } catch (NotFoundException | ClassNotFoundException | CannotCompileException e) {
            e.printStackTrace();
        }
    }
}
