package org.jys.example.common.sql;

import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YueSong Jiang
 * @date 2019/12/27
 */
public class CopyMethodGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CopyMethodGenerator.class);
    private static final String FIRST_FIELD_ORDER_NAME = "NULL";

    private CopyMethodGenerator() {
    }

    public static void modifyCopyMethod(String className) throws NotFoundException, CannotCompileException {
        ClassPool classPool = ClassPool.getDefault();
        //append current class path for search class
        classPool.appendClassPath(new LoaderClassPath(CopyMethodGenerator.class.getClassLoader()));
        //get the class to modify
        CtClass ct = classPool.getCtClass(className);

        CtClass copyInInterface = classPool.getCtClass(CopyInData.class.getName());
        //validate if the class inherit CopyInData interface
        if (!ct.subtypeOf(copyInInterface)) {
            String msg = String.format("class [%s] is not inherited from [%s]", className, copyInInterface.getName());
            throw new NotFoundException(msg);
        }
        //all fields (include inherit from supper class)
        CtField[] allFields = ct.getFields();
        //fields declared in current class
        CtField[] declaredFields = ct.getDeclaredFields();
        //unique field name map ,key is field name , value is CtField
        //first add all declared fields
        Map<String, CtField> uniqueFieldNameMap =
                Arrays.stream(declaredFields).collect(Collectors.toMap(CtField::getName, fx -> fx));
        //add supper class field not defined in current class
        Arrays.stream(allFields).forEach(f -> uniqueFieldNameMap.putIfAbsent(f.getName(), f));

        //filter field has CopyOrder annotation
        List<CtField> fieldHasAnnotations =
                uniqueFieldNameMap.values().stream().filter((a) -> a.hasAnnotation(CopyOrder.class)).collect(Collectors.toList());
        //get map which value is CtFiled, key is field name which is before value field
        Map<String, CtField> annotationInfoMap = fieldHasAnnotations.stream()
                .collect(Collectors.toMap((fx) -> {
                    try {
                        return ((CopyOrder) fx.getAnnotation(CopyOrder.class)).beforeField();
                    } catch (ClassNotFoundException var2) {
                        logger.error(var2.getMessage(), var2);
                        return null;
                    }
                }, (fx) -> fx));
        //validate order info if correct
        if (fieldHasAnnotations.size() != annotationInfoMap.size()) {
            throw new CannotCompileException("field order info is not correct, some fields has same beforeField() value");
        }
        //fields after sort
        LinkedList<CtField> sortedFields = new LinkedList<>();
        if (annotationInfoMap.containsKey(FIRST_FIELD_ORDER_NAME)) {
            sortedFields.add(annotationInfoMap.get(FIRST_FIELD_ORDER_NAME));
            annotationInfoMap.remove(FIRST_FIELD_ORDER_NAME);
        } else {
            throw new NotFoundException("no filed CopyOrder is NULL, no start field can be detect");
        }

        String fieldName;
        int currentOrder = 1;
        logger.debug("field order[{}], field name [{}]", currentOrder, sortedFields.getFirst().getName());
        //sort
        for (int i = 0; i < annotationInfoMap.size(); ++i) {
            fieldName = sortedFields.getLast().getName();
            if (!annotationInfoMap.containsKey(fieldName)) {
                String errorMessage = String.format("can not find field which is before field[%s]", fieldName);
                throw new NotFoundException(errorMessage);
            }
            logger.debug("field order[{}], field name [{}]", ++currentOrder, fieldName);
            sortedFields.add(annotationInfoMap.get(fieldName));
        }

        //join all fields get the body
        StringBuilder methodBody = new StringBuilder("return new StringBuilder()");
        for (CtField f : sortedFields) {
            methodBody.append(".append(transferNullOrEmptyData(").append(f.getName()).append(")).append(").append("getFieldDelimiter()").append(")");

        }

        methodBody.append(".toString();");
        logger.info("copy-in method body for class [{}] is [{}]", className, methodBody);

        //get all methods include inherited from parent
        CtMethod[] allMethods = ct.getMethods();
        CtMethod copyMethod;
        //javassist can call inherited method from parent or interface,
        //but if need to modify use javassist, you must create the method
        //can use CtMethod.copy() create
        try {
            copyMethod = ct.getDeclaredMethod("generateCopyString");
            copyMethod.setBody(methodBody.toString());
        } catch (NotFoundException var17) {
            logger.info("add method to override supper class method");
            CtMethod methodFromSuper = Arrays.stream(allMethods).filter((x) -> x.getName()
                    .contains("generateCopyString")).findFirst().orElse(null);
            //copy the method
            copyMethod = CtNewMethod.copy(methodFromSuper, ct, null);
            copyMethod.setBody(methodBody.toString());
            ct.addMethod(copyMethod);
        }

        ct.toClass();
    }
}
