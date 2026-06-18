package redegs.engine.engine.system.component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentMeta {
    String name();
    String category() default "General";
    String description() default "";
}