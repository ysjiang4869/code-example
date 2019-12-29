package org.jys.example.common.sql;

import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YueSong Jiang
 * @date 2019/12/27
 */
public class CopyMethodGenerator {

    private static final Logger logger= LoggerFactory.getLogger(CopyMethodGenerator.class);

    private CopyMethodGenerator(){};

    /**
     * modify generateCopyString method of CopyInData implements
     * @param className the class name which need to modify,must implement CopyInData interface
     * @throws NotFoundException some thing can not found
     * @throws ClassNotFoundException class not found
     * @throws CannotCompileException compile error
     */
    public static void modifyCopyMethod(String className) throws NotFoundException, ClassNotFoundException, CannotCompileException {
        ClassPool classPool= ClassPool.getDefault();
        classPool.appendClassPath(new LoaderClassPath(CopyMethodGenerator.class.getClassLoader()));
        CtClass ct=classPool.getCtClass(className);
        //validate if the class inherit CopyIn interface
        CtClass[] interfaces=ct.getInterfaces();
        CtClass copyInInterface=classPool.getCtClass("org.jys.example.common.sql.CopyInData");
        if(!Arrays.asList(interfaces).contains(copyInInterface)){
            String msg=String.format("class [%s] not inherited from [%s]",className, copyInInterface.getName());
           throw new NotFoundException(msg);
        }
        CtField[] fields=ct.getDeclaredFields();
        //validate CopyOrder has different value
        //you should use classPool to get interface info and validate
        //not use Class.forName, because it loaded the class and cause loader (instance of  sun/misc/Launcher$AppClassLoader): attempted  duplicate class definition for name
        boolean[] validate=new boolean[fields.length];
        for (CtField f:fields) {
            if(f.hasAnnotation(CopyOrder.class)){
                int order=((CopyOrder)f.getAnnotation(CopyOrder.class)).value();
                if(order>=validate.length||validate[order]){
                    throw new CannotCompileException("copy order must begin from 0 and must be continuous");
                }
                validate[order]=true;
            }
        }
        List<CtField> sortedFields= Arrays.stream(fields).filter(a->a.hasAnnotation(CopyOrder.class))
                .sorted((a,b)-> {
                    try {
                        return ((CopyOrder)a.getAnnotation(CopyOrder.class)).value()-((CopyOrder)b.getAnnotation(CopyOrder.class)).value();
                    } catch (ClassNotFoundException e) {
                        //this can not happen
                        return 0;
                    }
                }).collect(Collectors.toList());
        StringBuilder body=new StringBuilder("return new StringBuilder()");
        for (CtField f: sortedFields ){
            body.append(".append(").append(f.getName()).append(").append(").append("getDelimiter()").append(")");
        }
        body.append(".toString();");
        logger.info("copy in body is[{}]",body.toString());
        ct.getDeclaredMethod("generateCopyString").setBody(body.toString());
        ct.toClass();
    }

    public static void main(String[] args){
        try {
            modifyCopyMethod("org.jys.example.common.sql.CopyInDataObject");
            CopyInDataObject p=new CopyInDataObject();
            p.setRecordId(123456L);
            p.setPersonId("3789k199365541235");
            p.setAgeLowerLimit(10);
            p.setAgeUpLimit(50);
            p.setGender(1);
            p.setImageUrl("http://127.0.0.1/");
            String copyString=p.generateCopyString();
            System.out.println(copyString);
        } catch (NotFoundException | ClassNotFoundException | CannotCompileException e) {
            e.printStackTrace();
        }
    }
}
