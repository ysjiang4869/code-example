package org.jys.example.common.utils;

import javassist.*;
import org.jys.database.dao.PersonStructured;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YueSong Jiang
 * @date 2019/12/27
 */
public class CopyMethodGenerator {

    public static void addMethod(String className) throws NotFoundException, ClassNotFoundException, CannotCompileException {
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
            body.append(".append(").append(f.getName()).append(").append('\\030')");
        }
        body.append(".toString();");
        System.out.println(body);
        ct.getDeclaredMethod("generateCopySql").setBody(body.toString());
        ct.toClass();
    }

    public static void main(String[] args){
        try {
            addMethod("org.jys.database.dao.PersonStructured");
            PersonStructured p=new PersonStructured();
            p.setRecordID(1111111L);
            p.setPersonID("xxx");
            System.out.println('\030');
            String x=p.generateCopySql();
            System.out.println(p.generateCopySql());
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }
}
