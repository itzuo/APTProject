你可能经常在build.gradle文件中看到，这样的字眼，annotationProcessor、android-apt、Provided，它们到底有什么作用？下面就一起来看看吧

## 1、什么是APT？
注解处理器是（Annotation Processor Tool）是javac的一个工具，用来在编译时扫描和编译和处理注解（Annotation）。
你可以自己定义注解和注解处理器去搞一些事情。一个注解处理器它以Java代码或者（编译过的字节码）作为输入，生成文件
（通常是java文件）。这些生成的java文件不能修改，并且会同其手动编写的java代码一样会被javac编译。

注解处理器使用 annotationProcessor 引入。

annotationProcessor 和 apt 都是annotation Processor tool 注解处理器的引入方式，
是使用gradle 的api创建的依赖配置,同样的 compile、implementation、androidTestImplementation等等都是。
他们由创建者定义了不同的行为。
我们也可以成为一个创建者定义任意名字、行为的配置。
有兴趣可以去了解下gradle configurations。

#### APT的处理要素
注解处理器（AbstractProcess）+代码处理（javaPoet）+处理器注册（AutoService）+apt 
#### 使用APT来处理annotation的流程

1. 定义注解（如@automain）
2. 定义注解处理器，自定义需要生成代码
3. 使用处理器
4. APT自动完成如下工作。

![APT流程](https://upload-images.jianshu.io/upload_images/2918620-6559e6b9f5ef46d6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 2、代码实现
#### 2.1、创建一个Android项目APTProject
#### 2.2、新增一个java Library Module 名为apt-annotation， 编写注解类：

```
//元注解
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Route {
    /**
     *路由的路径，标识一个路由节点
     */
    String path();

    /**
     * 将路由节点进行分组，可以实现按组动态加载
     */
    String group() default "";
}
```

1. RetentionPolicy.SOURCE —— 编译之后抛弃，存活的时间在源码和编译时
2. RetentionPolicy.CLASS —— 保留在编译后class文件中,但JVM将会忽略(对于 底层系统开发-开发编译过程 使用)
apt中source和class都一样
3. RetentionPolicy.RUNTIME —— 能够使用反射读取和使用.
注解不只能用于注解处理器来进行处理，也可以运行时反射使用(不推荐使用反射)

#### 2.2、新增一个java Library Module 名为apt-process，编写类来处理注解。以后使用上面的@AutoCreate，就会根据下面这个类生成指定的java文件

```
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
```
上面代码中的process方法的参数set是你使用了Route注解的个数，也就是说你使用了多个`@Route(path = "aaaa")`,这里的set结合就有多个元素
#### 2.2.1、需要使用的lib

```
dependencies {
    implementation 'com.google.auto.service:auto-service:1.0-rc2'
    //帮助我们利用构建则模式生成java文件
    implementation 'com.squareup:javapoet:1.8.0'
}
```
至此一个简单的自定义注解类，就完成了，只是生成了一个HelloWorld.java文件，里面只有一个main()函数

#### 2.3、自定义注解类的使用
使用的话，更简单。在java文件中使用如下：

```
@Route(path = "aaaa")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
```
配置build.gradle文件

```
dependencies {
    //添加下面这句就可以了
    implementation project(':apt-annotation')
    annotationProcessor project(':apt-process')
}
```
