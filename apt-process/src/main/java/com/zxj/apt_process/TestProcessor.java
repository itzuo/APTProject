package com.zxj.apt_process;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.zxj.apt_annotation.Route;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * 注解处理器
 * 完成注解的处理
 * 1.处理哪个注解
 * 2.如何处理
 */
//注册处理器，告知APT，Route是由TestProcess处理
@AutoService(Processor.class)
//@SupportedAnnotationTypes("com.zxj.apt_annotation.Route")
//@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class TestProcessor extends AbstractProcessor {
    //日志工具
    private Messager mMessager;
    //文件工具
    private Filer mFiler;

    /**
     * 注册给哪些注解的
     * 此方法可以直接使用注解@SupportedAnnotationTypes("com.zxj.apt_annotation.Route")代替
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        //定义需要处理的注解
        return Collections.singleton(Route.class.getCanonicalName());
    }

    /**
     * 指定使用的Java版本
     * 此方法可以直接使用注解@SupportedSourceVersion(SourceVersion.RELEASE_7)代替
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    /**
     * 初始化 从 {@link ProcessingEnvironment} 中获得一系列处理器工具
     * @param processingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnvironment.getFiler();
        mMessager.printMessage(Diagnostic.Kind.NOTE,"TestProcessor------init");
    }

    /**
     * 相当于main函数，正式处理注解
     * @param set 使用了当前注解处理器允许处理注解的节点集合
     * @param roundEnvironment 表示当前或是之前的运行环境,可以通过该对象查找找到的注解。
     * @return true 表示后续处理器不会再处理(已经处理)
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //如何处理该注解
        /*
         * hello.java
          public final class HelloWorld {
             public static void main(String[] args) {
                System.out.println("Hello, JavaPoet!");
            }
          }
         */
        //文件
        //文件内容--java代码生成工具javapoet
        //MethodSpec:定义方法
        //TypeSec:定义类
        //JavaFile：生成.java文件
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("com.songwenju.aptproject", helloWorld)
                .build();
        try {
//            javaFile.writeTo(processingEnv.getFiler());
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
