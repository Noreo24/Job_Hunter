package vn.noreo.jobhunter.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Hoạt động trong quá trình runtime
@Retention(RetentionPolicy.RUNTIME)
// Chỉ dùng cho các phương thức (method) trong controller
@Target(ElementType.METHOD)
public @interface ApiMessage {
    String value();
}
